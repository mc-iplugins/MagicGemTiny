package com.illtamer.plugin.magicgemtiny.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public class GemUtil {

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
            int logLevel = triggerCommonRewardsOnCondition(consumed, itemGem, player, targetItemNBT, logObject);
            if (logLevel >= 0) { // 成功/失败都记录
                array.add(logObject);
            }
        });
    }

    /**
     * 触发玩家/物品宝石奖励
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

        ItemUtil.consume(consumed);
        // 无论怎样都会执行的奖励
        doRewardList(anywayList, player, targetItemNBT, json);

        double successValue = gem.getSuccess().getDouble(player, targetItemNBT.getItem());
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

}
