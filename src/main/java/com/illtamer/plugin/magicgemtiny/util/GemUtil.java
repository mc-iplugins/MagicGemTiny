package com.illtamer.plugin.magicgemtiny.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.condition.RequireCondition;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.gem.ItemGem;
import com.illtamer.plugin.magicgemtiny.gem.PlayerGem;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GemUtil {

    // 变更日志中标记该条记录"镶嵌成功且可拆卸"的字段名, 防止失败记录被拆卸刷取宝石
    public static final String REMOVABLE_FLAG = "_removable";

    /**
     * @param itemGem 物品宝石
     * @param targetItemNBT 被镶嵌的物品NBT
     * @apiNote 不满足条件(Require, Reward#test) 不触发宝石效果也不消耗宝石
     * */
    public static void triggerItemRewardsOnCondition(ItemStack consumed, ItemGem itemGem, NBTItem targetItemNBT, Player player) {
        RequireCondition require = itemGem.getRequireCondition();
        // Require 满足任意条件即可
        if (!require.checkWithDefaultRequires(targetItemNBT.getItem(), false)) {
            player.sendMessage("§c目标物品不符合条件");
            return;
        }

        // 记录变更日志，方便后续拆卸
        ItemUtil.computeJsonArray(targetItemNBT, NBTKey.MAGICGEM_CHANGE_LOG, array -> {
            long embedCount = array.asList().stream().map(JsonElement::getAsJsonObject)
                    .filter(e -> e.get("Name").getAsString().equals(itemGem.getName())).count();
            if (itemGem.getEmbed() > 0 && embedCount >= itemGem.getEmbed()) {
                player.sendMessage("§c该宝石镶嵌数量达上限");
                return;
            }

            JsonObject logObject = new JsonObject();
            // 记录镶嵌的宝石类型
            logObject.addProperty("Name", itemGem.getName());
            int logLevel;
            if (itemGem.isRemoveAble()) {
                logLevel = triggerCommonRewardsOnCondition(consumed, itemGem, player, targetItemNBT, logObject);
            } else { // 如果不可拆卸，不记录日志，只记录宝石类型
                logLevel = triggerCommonRewardsOnCondition(consumed, itemGem, player, targetItemNBT, new JsonObject());
            }
            // 仅"可拆卸宝石且镶嵌成功"的记录允许拆卸, 失败记录(未改变装备)不可拆卸, 防止刷宝石
            if (itemGem.isRemoveAble() && logLevel == 1) {
                logObject.addProperty(REMOVABLE_FLAG, true);
            }
            if (logLevel >= 0) { // 成功/失败都记录(失败记录仅用于镶嵌计数)
                array.add(logObject);
            }
        });
    }

    /**
     * 触发玩家宝石奖励
     * @param playerGem 玩家宝石
     * @apiNote 不满足条件(Require, Reward#test) 不触发宝石效果也不消耗宝石
     * */
    public static void triggerPlayerRewardsOnCondition(ItemStack consumed, PlayerGem playerGem, Player player) {
        triggerCommonRewardsOnCondition(consumed, playerGem, player, null, null);
    }

    // @return logLevel
    private static int triggerCommonRewardsOnCondition(ItemStack consumed, Gem gem, Player player, @Nullable NBTItem targetItemNBT, @Nullable JsonObject json) {
        List<Reward> rewardList = gem.getRewards();

        List<Reward> onSuccessList = new ArrayList<>();
        List<Reward> onFailList = new ArrayList<>();
        List<Reward> anywayList = new ArrayList<>();
        for (Reward reward : rewardList) {
            if (reward instanceof PlayerReward playerReward) {
                // onRemove 奖励只在拆卸时执行, 镶嵌阶段既不校验也不执行
                if (playerReward.isOnRemove()) {
                    continue;
                }
                if (!playerReward.test(player)) {
                    return -1;
                }
                // 只有玩家奖励有条件区分
                if (playerReward.isOnSuccess()) {
                    onSuccessList.add(reward);
                } else if (playerReward.isOnFail()) {
                    onFailList.add(reward);
                } else {
                    anywayList.add(reward);
                }
            } else { // ItemReward
                ItemReward itemReward = (ItemReward) reward;
                try {
                    if (!itemReward.test(targetItemNBT)) {
                        return -1;
                    }
                } catch (ConditionException e) {
                    player.sendMessage("§c" + e.getMessage());
                    return -1;
                }
                // 物品奖励成功才执行
                onSuccessList.add(reward);
            }
        }

        try {
            // 无论怎样都会执行的奖励
            doRewardList(anywayList, player, targetItemNBT, json);
            // 为空是玩家宝石 一定执行
            double successValue = Optional.ofNullable(targetItemNBT).map(e -> gem.getSuccess().getDouble(player, e.getItem())).orElse(100D);
            NBTItem consumedNBT = new NBTItem(consumed);
            // 先乘后加 相互独立
            if (consumedNBT.hasTag(NBTKey.GEM_SUCCESS_MULTIPLE)) {
                successValue = successValue * (1 + consumedNBT.getInteger(NBTKey.GEM_SUCCESS_MULTIPLE) / 100.0);
            }
            if (consumedNBT.hasTag(NBTKey.GEM_SUCCESS_ADD)) {
                successValue = successValue + consumedNBT.getInteger(NBTKey.GEM_SUCCESS_ADD);
            }

            // 判断Success
            if (!RandomUtil.success(successValue)) {
                String failTip = StringUtil.isBlank(gem.getFailTip()) ? "物品使用失败" : gem.getFailTip();
                player.sendMessage(StringUtil.c(failTip));

                if (gem instanceof ItemGem itemGem) {
                    // 判断降级
                    double downgradeValue = itemGem.getDowngrade().getDouble(player, targetItemNBT.getItem());
                    if (RandomUtil.success(downgradeValue)) {
                        for (Reward reward : rewardList) {
                            if (reward instanceof ItemReward itemReward) {
                                itemReward.downgrade(targetItemNBT, player, json);
                            }
                        }
                        if (StringUtil.isNotBlank(itemGem.getDowngradeTip())) {
                            player.sendMessage(StringUtil.c(itemGem.getDowngradeTip()));
                        }
                    }
                }
                // 触发失败奖励
                doRewardList(onFailList, player, targetItemNBT, json);
                return 0;
            }
        } finally { // 最后消耗宝石（因为要用到宝石nbt
            ItemUtil.consume(consumed);
        }

        if (StringUtil.isNotBlank(gem.getSuccessTip())) {
            player.sendMessage(StringUtil.c(gem.getSuccessTip()));
        }
        // 触发成功奖励
        doRewardList(onSuccessList, player, targetItemNBT, json);
        return 1;
    }

    private static void doRewardList(List<Reward> rewardList, Player player, @Nullable NBTItem targetItemNBT, @Nullable JsonObject json) {
        for (Reward reward : rewardList) {
            if (reward instanceof ItemReward itemReward) {
                itemReward.execute(targetItemNBT, player, json);
            } else if (reward instanceof PlayerReward playerReward) {
                playerReward.execute(player);
            }
        }
    }

    // ===================== 宝石拆卸 =====================

    /**
     * 一条可拆卸的宝石记录（供拆卸 GUI 展示）
     * */
    public static class RemovableRecord {
        // 该记录在 MAGICGEM_CHANGE_LOG 数组中的索引
        public final int logIndex;
        public final ItemGem gem;
        public final JsonObject logObject;

        public RemovableRecord(int logIndex, ItemGem gem, JsonObject logObject) {
            this.logIndex = logIndex;
            this.gem = gem;
            this.logObject = logObject;
        }
    }

    /**
     * 读取物品的变更日志数组
     * */
    public static JsonArray getChangeLogArray(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return new JsonArray();
        }
        NBTItem nbtItem = new NBTItem(item);
        return ItemUtil.computeJsonArray(nbtItem, NBTKey.MAGICGEM_CHANGE_LOG, null);
    }

    /**
     * 收集物品上所有可拆卸的宝石记录
     * @apiNote 逐条对应 changelog 中的一条记录，顺序与数组一致（含同名多颗）
     * */
    public static List<RemovableRecord> collectRemovableRecords(ItemStack item) {
        List<RemovableRecord> result = new ArrayList<>();
        JsonArray array = getChangeLogArray(item);
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject logObject = element.getAsJsonObject();
            JsonElement nameEle = logObject.get("Name");
            if (nameEle == null || nameEle.isJsonNull()) {
                continue;
            }
            Gem gem = MagicGemTiny.getInstance().getGemLoader().getGemMap().get(nameEle.getAsString());
            if (!(gem instanceof ItemGem itemGem)) {
                continue;
            }
            if (isRemovable(itemGem, logObject)) {
                result.add(new RemovableRecord(i, itemGem, logObject));
            }
        }
        return result;
    }

    /**
     * 判断某条 changelog 记录对应的宝石是否可拆卸
     * @apiNote 需同时满足:
     *      1. 宝石 RemoveAble=true 且其所有物品奖励都支持拆卸(disassemble()==true)
     *      2. 该条记录是"镶嵌成功"记录(带成功标志), 失败记录不可拆卸, 防止刷宝石
     * */
    public static boolean isRemovable(@Nullable ItemGem gem, JsonObject logObject) {
        if (gem == null || !gem.isRemoveAble()) {
            return false;
        }
        if (!isSuccessRecord(logObject)) {
            return false;
        }
        for (Reward reward : gem.getRewards()) {
            if (reward instanceof ItemReward itemReward) {
                itemReward.ensureInit();
                if (!itemReward.disassemble()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 判断变更日志记录是否为"镶嵌成功"记录
     * @apiNote 新数据带 {@link #REMOVABLE_FLAG} 标志; 为兼容旧数据(无标志),
     *      若除 Name 外还留有奖励还原数据键则视为成功记录, 仅剩 Name 的失败记录不可拆
     * */
    private static boolean isSuccessRecord(JsonObject logObject) {
        if (logObject.has(REMOVABLE_FLAG)) {
            JsonElement flag = logObject.get(REMOVABLE_FLAG);
            return !flag.isJsonNull() && flag.getAsBoolean();
        }
        // 向后兼容: 旧记录无标志, 存在 Name 以外的键说明镶嵌成功并留下了还原数据
        for (Map.Entry<String, JsonElement> entry : logObject.entrySet()) {
            if (!"Name".equals(entry.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 执行拆卸：校验费用 -> 逆序还原装备 -> 扣除 onRemove 费用 -> 移除记录 -> 退还宝石
     * @param targetItem 被拆卸的装备（含变更日志）
     * @param logIndex 要拆卸的记录在 changelog 数组中的索引
     * @param expectedGemName GUI 点击时该图标对应的宝石名, 用于校验索引未因并发/连点而错位
     * @param player 操作玩家
     * @return 还原后的装备物品；拆卸失败返回 null（此时不产生任何副作用）
     * */
    @Nullable
    public static ItemStack disassembleGem(ItemStack targetItem, int logIndex, String expectedGemName, Player player) {
        if (targetItem == null || targetItem.getType().isAir()) {
            return null;
        }
        // 在克隆物品上执行完整拆卸流程，只有全部校验/还原成功后才由调用方写回。
        // 这样即使 restore 途中失败，也不会污染 GUI 装备槽里的原物品。
        NBTItem nbtItem = new NBTItem(targetItem.clone());
        // 读取 changelog 时不写回 NBT；后续移除记录时再显式写回。
        // deepCopy 进一步隔离 restore 过程中可能产生的对象引用副作用，确保只删除本次指定记录。
        JsonArray array = ItemUtil.computeJsonArray(nbtItem, NBTKey.MAGICGEM_CHANGE_LOG, null).deepCopy();
        if (logIndex < 0 || logIndex >= array.size() || !array.get(logIndex).isJsonObject()) {
            player.sendMessage("§c该拆卸记录已失效，请重新打开拆卸台");
            return null;
        }
        JsonObject logObject = array.get(logIndex).getAsJsonObject();
        JsonElement nameEle = logObject.get("Name");
        if (nameEle == null || nameEle.isJsonNull()) {
            player.sendMessage("§c无效的宝石记录");
            return null;
        }
        String gemName = nameEle.getAsString();
        // 校验索引指向的记录与点击的图标一致, 防止快速连点导致拆错宝石
        if (expectedGemName != null && !expectedGemName.equals(gemName)) {
            player.sendMessage("§c拆卸记录已变化，请重试");
            return null;
        }
        Gem gem = MagicGemTiny.getInstance().getGemLoader().getGemMap().get(gemName);
        if (!(gem instanceof ItemGem itemGem) || !isRemovable(itemGem, logObject)) {
            player.sendMessage("§c该宝石不可拆卸: " + gemName);
            return null;
        }

        // 1. 收集 onRemove 玩家奖励并预校验（如点券是否足够），全部通过才继续，保证原子性
        List<PlayerReward> onRemoveRewards = new ArrayList<>();
        for (Reward reward : itemGem.getRewards()) {
            if (reward instanceof PlayerReward playerReward && playerReward.isOnRemove()) {
                onRemoveRewards.add(playerReward);
            }
        }
        for (PlayerReward playerReward : onRemoveRewards) {
            if (!playerReward.test(player)) {
                // test 内部会给出失败提示（如点券不足）
                return null;
            }
        }

        // 2. 逆序还原装备的所有物品奖励修改
        //    在 clone(nbtItem) 上操作; 任一还原抛异常或返回 false(装备状态与记录不符/无法还原)
        //    则整体中止, 不扣费/不删记录/不退宝石, 既保证原子性又防止刷取宝石
        List<Reward> rewards = itemGem.getRewards();
        for (int i = rewards.size() - 1; i >= 0; i--) {
            if (rewards.get(i) instanceof ItemReward itemReward) {
                itemReward.ensureInit();
                boolean ok;
                try {
                    ok = itemReward.restore(nbtItem, player, logObject);
                } catch (Exception e) {
                    MagicGemTiny.getInstance().getLogger().warning(
                            "拆卸宝石 " + gemName + " 时还原奖励失败, 已中止拆卸: " + e.getMessage());
                    player.sendMessage("§c拆卸失败: 宝石效果还原出错, 请联系管理员");
                    return null;
                }
                if (!ok) {
                    player.sendMessage("§c拆卸失败: 该宝石的效果已被覆盖或改动, 请先拆卸后镶嵌的宝石");
                    return null;
                }
            }
        }

        // 3. 执行 onRemove 费用扣除（校验已通过）
        for (PlayerReward playerReward : onRemoveRewards) {
            playerReward.execute(player);
        }

        // 4. 移除该条 changelog 记录并写回
        array.remove(logIndex);
        nbtItem.setString(NBTKey.MAGICGEM_CHANGE_LOG, array.toString());
        ItemStack restored = nbtItem.getItem();

        // 5. 退还 1 颗原始宝石
        ItemUtil.giveOrDropItem(player, itemGem.getItem(1));

        // 6. 提示
        String removeTip = itemGem.getRemoveTip();
        if (StringUtil.isNotBlank(removeTip)) {
            player.sendMessage(StringUtil.c(removeTip));
        } else {
            player.sendMessage("§a成功拆卸宝石: " + gemName);
        }
        return restored;
    }

}
