package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonArray;
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
 * 将物品上指定的Lore替换为新的lore，支持颜色代码&
 * */
public class LoreReplaceReward extends ItemReward {

    // 要替换的旧lore
    private String old;
    // 要添加的新lore
    private String lore;
    // 默认 all
    private String mode;
    // locator只对行内替换有效，识别无视颜色
    private String locator;
    // 替换的限制次数
    private Integer limit;

    @Override
    protected void init() {
        old = StringUtil.c(getParamString("old", null));
        lore = StringUtil.c(getParamString("lore", null));
        mode = getParamString("mode", null);
        if (StringUtil.isBlank(mode)) {
            mode = "all";
        }
        locator = StringUtil.clearColor(getParamString("locator", null));
        limit = getParamInteger("limit", null);
        limit = limit == null || limit <= 0 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        JsonArray history = new JsonArray();
        try {
            ItemStack item = nbtItem.getItem();
            ItemMeta meta = item.getItemMeta();

            List<String> loreList = new ArrayList<>(meta.getLore());
            int currentChanges = 0;
            switch (mode.toLowerCase()) {
                case "first" -> {
                    for (int i = 0; i < loreList.size(); i++) {
                        if (StringUtil.c(loreList.get(i)).equals(old)) {
                            recordAndReplace(loreList, i, lore, history);
                            break; // 只替换第一个
                        }
                    }
                }
                case "last" -> {
                    for (int i = loreList.size() - 1; i >= 0; i--) {
                        if (StringUtil.c(loreList.get(i)).equals(old)) {
                            recordAndReplace(loreList, i, lore, history);
                            break; // 只替换最后一个
                        }
                    }
                }
                case "all" -> {
                    for (int i = 0; i < loreList.size() && currentChanges < limit; i++) {
                        if (StringUtil.c(loreList.get(i)).equals(old)) {
                            recordAndReplace(loreList, i, lore, history);
                            currentChanges++;
                        }
                    }
                }
                case "line" -> {
                    for (int i = 0; i < loreList.size() && currentChanges < limit; i++) {
                        String currentLine = StringUtil.c(loreList.get(i));
                        // 识别无视颜色：清除当前行的颜色后判断是否包含 locator
                        if (StringUtil.clearColor(currentLine).contains(locator)) {
                            // 在该行内执行替换 (支持颜色代码匹配)
                            if (currentLine.contains(old)) {
                                String updatedLine = currentLine.replace(old, lore);
                                recordAndReplace(loreList, i, updatedLine, history);
                                currentChanges++;
                            }
                        }
                    }
                }
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        } finally {
            json.add("LoreReplace", history);
        }
    }

    /**
     * 记录修改并执行替换的辅助方法
     * @param list 当前最新状态的lore列表
     * @param index 替换行的索引
     * @param newValue 新lore
     */
    private void recordAndReplace(List<String> list, int index, String newValue, JsonArray history) {
        String originalValue = list.get(index);

        // 构造回溯用的 Json 对象
        JsonObject change = new JsonObject();
        change.addProperty("index", index);
        change.addProperty("before", originalValue);
        change.addProperty("after", newValue);
        history.add(change);

        // 执行替换
        list.set(index, newValue);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore().isEmpty()) {
            throw new ConditionException("物品不支持替换lore");
        }
        List<String> loreList = meta.getLore();
        if (StringUtil.isBlank(old)) {
            throw new ConditionException("要替换的旧lore为空");
        }
        if (StringUtil.isBlank(mode) || !Arrays.asList("first", "last", "all", "line").contains(mode)) {
            throw new ConditionException("不支持的替换模式: " + mode);
        }
        if ("line".equals(mode) && StringUtil.isBlank(locator)) {
            throw new ConditionException("当前处于行内替换模式，但定位器为空");
        }
        boolean match;
        if (StringUtil.isBlank(locator)) { // 定位器为空，依据old匹配
            match = loreList.stream().map(StringUtil::c).anyMatch(e -> e.equals(old));
        } else { // 依据定位器匹配 识别无视颜色
            match = loreList.stream().map(StringUtil::c).anyMatch(e ->
                StringUtil.clearColor(e).contains(locator) && e.contains(old));
        }
        if (!match) {
            throw new ConditionException("没有匹配的lore");
        }
        return match;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

}
