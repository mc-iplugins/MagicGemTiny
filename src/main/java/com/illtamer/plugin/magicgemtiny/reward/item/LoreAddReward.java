package com.illtamer.plugin.magicgemtiny.reward.item;

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

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        String withdraw = "delete";
        int index = 0;
        String oldLore = null;
        try {
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
                    loreList.add(index, lore);
                } else if ("after".equals(mode)) {
                    loreList.add(index+1, lore);
                } else { // line
                    oldLore = loreList.get(index);
                    loreList.set(index, oldLore + lore);
                    withdraw = "replace";
                }
            }

            meta.setLore(loreList);
            item.setItemMeta(meta);
        } finally {
            JsonObject obj = new JsonObject();
            // 撤回类型，replace 或 delete
            obj.addProperty("withdraw", withdraw);
            // 如果为replace，原lore
            obj.addProperty("oldLore", oldLore);
            // 撤回的行
            obj.addProperty("index", index);
            json.add("LoreAdd", obj);
        }
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

}
