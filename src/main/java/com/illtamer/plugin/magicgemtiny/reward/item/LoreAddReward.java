package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 增加 Lore 奖励
 * */
public class LoreAddReward extends ItemReward {

    // mode是模式，有五种可选:
    // - first(第一行),
    // - last(最后一行)
    // - before(在locator前)
    // - after(在locator后)
    // - line(在locator那行追加lore)
    private String mode;
    private String lore;
    private String locator;
    private Integer limit;
    // 在未找到locator时，是否强行添加lore。默认为true
    private boolean force;

    @Override
    protected void init() {
        lore = StringUtil.c(getParamString("lore", null));
        mode = getParamString("mode", null);
        locator = getParamString("locator", null);
        limit = getParamInteger("limit", null);
        limit = limit == null ? Integer.MAX_VALUE : limit;
        String force = getParamString("force", null);
        if (StringUtil.isBlank(force)) {
            this.force = true;
        } else {
            this.force = Boolean.parseBoolean(force);
        }
    }

    // 无需支持拆卸
    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        String withdraw = "delete";
        Integer index = null;
        String oldLore = null;

        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = meta.hasLore() ? meta.getLore() : new ArrayList<>();

        if (mode.equals("first")) {
            loreList.add(0, lore);
        } else if (mode.equals("last")) {
            index = loreList.size();
            loreList.add(lore);
        } else {
            for (int i = 0; i < loreList.size(); i++) {
                if (StringUtil.clearColor(loreList.get(i)).contains(locator)) {
                    index = i;
                }
            }
            if ("before".equals(mode)) {
                // 没找到locator时回退为first
                if (index == null) {
                    index = 0;
                }
                loreList.add(index, lore);
            } else if ("after".equals(mode)) {
                // 没找到locator时回退为last
                if (index == null) {
                    index = loreList.size()-1;
                }
                loreList.add(index+1, lore);
            } else { // line
                oldLore = loreList.get(index);
                loreList.set(index, oldLore + lore);
                withdraw = "replace";
            }
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);

        String countNbtKey = "LoreAdd_" + StringUtil.c(lore);
        int count = nbtItem.getInteger(countNbtKey);
        nbtItem.setInteger(countNbtKey, count+1);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持设置元数据");
        }
        if (StringUtil.isBlank(lore)) {
            throw new ConditionException("lore为空，请联系管理员检查宝石配置");
        }
        List<String> locatorModeList = Arrays.asList("before", "after", "line");
        List<String> modeList = Arrays.asList("first", "last", "before", "after", "line");
        // TODO 需要补充 line 模式下的 loreList 检查（该模式没有默认回退）
        if (!modeList.contains(mode)) {
            throw new ConditionException("无效的lore匹配模式: " + mode + ", 请联系管理员");
        }
        int count = nbtItem.getInteger("LoreAdd_" + StringUtil.c(lore));
        if (count >= limit) {
            throw new ConditionException("该物品的添加次数达上限");
        }
        if (locatorModeList.contains(mode)) {
            if (StringUtil.isBlank(locator)) {
                throw new ConditionException("locator为空，请联系管理员检查宝石配置");
            }

            List<String> loreList = meta.getLore();
            if (loreList == null || loreList.isEmpty()) {
                return false;
            }

            return loreList.stream().anyMatch(e -> StringUtil.clearColor(e).contains(locator));
        }
        // TODO 当支持force=false时，需要补充是否能找到lore检查
        if (!force) {
            throw new ConditionException("不支持force=false配置");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 拆卸还原
     * @return 是否可安全提交拆卸。找不到当次添加的行(已被篡改/移除)时返回 false 以中止拆卸
     * @apiNote delete 模式移除当次添加的 lore 行（就近内容匹配，容忍 index 记录偏差）；
     *      replace 模式（line）将该行设回追加前的 oldLore
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get("LoreAdd");
        if (element == null || !element.isJsonObject()) {
            return true; // 本奖励未产生记录, 无需还原
        }
        JsonObject obj = element.getAsJsonObject();
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        List<String> loreList = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        String withdraw = obj.has("withdraw") && !obj.get("withdraw").isJsonNull()
                ? obj.get("withdraw").getAsString() : "delete";
        int index = obj.has("index") && !obj.get("index").isJsonNull() ? obj.get("index").getAsInt() : -1;
        String oldLore = obj.has("oldLore") && !obj.get("oldLore").isJsonNull() ? obj.get("oldLore").getAsString() : null;

        if ("replace".equals(withdraw)) {
            // line 模式：该行是 oldLore + lore，还原为 oldLore
            if (oldLore != null && index >= 0 && index < loreList.size()
                    && loreList.get(index).equals(oldLore + lore)) {
                loreList.set(index, oldLore);
            } else {
                return false; // 目标行已被篡改, 无法还原
            }
        } else { // delete：移除当次添加的整行
            int removeAt = findAddedLoreIndex(loreList, index);
            if (removeAt != -1) {
                loreList.remove(removeAt);
            } else {
                return false; // 找不到当次添加的行, 无法还原
            }
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);
        return true;
    }

    /**
     * 查找当次添加的 lore 行索引
     * @apiNote 优先精确匹配记录的 index，否则就近向后再全表查找等于 this.lore 的行
     * */
    private int findAddedLoreIndex(List<String> loreList, int recordedIndex) {
        if (recordedIndex >= 0 && recordedIndex < loreList.size() && loreList.get(recordedIndex).equals(lore)) {
            return recordedIndex;
        }
        // after 模式记录的是 locator 行，实际插入在其后一行，做一次就近探测
        if (recordedIndex + 1 >= 0 && recordedIndex + 1 < loreList.size() && loreList.get(recordedIndex + 1).equals(lore)) {
            return recordedIndex + 1;
        }
        for (int i = loreList.size() - 1; i >= 0; i--) {
            if (loreList.get(i).equals(lore)) {
                return i;
            }
        }
        return -1;
    }

}
