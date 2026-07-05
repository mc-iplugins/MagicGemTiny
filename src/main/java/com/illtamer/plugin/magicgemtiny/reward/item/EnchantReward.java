package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.EnumUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * 强化装备附魔
 * */
public class EnchantReward extends ItemReward {

    private String name;
    private Enchantment enchantment;
    private Integer level;
    private Integer limit;

    @Override
    protected void init() {
        name = getParamString("name", null);
        enchantment = EnumUtil.getEnchantment(name);
        level = getParamInteger("level", null);
        limit = getParamInteger("limit", null);
        limit = limit == null ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        int oldLevel = -1;
        int newLevel = -1;
        try {
            ItemStack item = nbtItem.getItem();
            oldLevel = item.getEnchantmentLevel(enchantment);
            newLevel = Math.min(oldLevel + level, limit);
            item.addUnsafeEnchantment(enchantment, newLevel);
        } finally {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", name);
            obj.addProperty("oldLevel", oldLevel);
            obj.addProperty("newLevel", newLevel);
            json.add("Enchant", obj);
        }
    }

    @Override
    public void downgrade(NBTItem nbtItem, Player player, JsonObject json) {
        int oldLevel = 0;
        int newLevel = 0;
        try {
            ItemStack item = nbtItem.getItem();
            oldLevel = item.getEnchantmentLevel(enchantment);
            if (oldLevel == 0) {
                return;
            }
            // 降级固定等级 -1
            newLevel = oldLevel - 1;
            item.addUnsafeEnchantment(enchantment, newLevel);
        } finally {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", name);
            obj.addProperty("oldLevel", oldLevel);
            obj.addProperty("newLevel", newLevel);
            json.add("Enchant", obj);
        }
    }

    @Override
    public boolean tryTest(NBTItem nbtItem) {
        ItemStack item = nbtItem.getItem();
        if (enchantment == null) {
            throw new ConditionException("不存在的附魔类型: " + name + "，请联系管理员");
        }
        if (item.getEnchantmentLevel(enchantment) >= limit) {
            throw new ConditionException("装备附魔等级已达上限");
        }
        if (level == null) {
            throw new ConditionException("要提升的等级不能为空");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 拆卸还原：将附魔等级设回镶嵌前的 oldLevel
     * @return 是否可安全提交拆卸。当前等级已非本次镶嵌结果(被覆盖/篡改)时返回 false 以中止拆卸,
     *      从而对多颗同名附魔宝石强制"后镶嵌先拆卸"的顺序, 防止乱序拆卸刷取
     * @apiNote oldLevel <= 0 表示镶嵌前无此附魔，还原时移除该附魔
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get("Enchant");
        if (element == null || !element.isJsonObject()) {
            return true; // 本奖励未产生记录, 无需还原
        }
        JsonObject obj = element.getAsJsonObject();
        if (enchantment == null) {
            return false; // 附魔配置无效, 无法还原
        }
        int oldLevel = obj.has("oldLevel") && !obj.get("oldLevel").isJsonNull() ? obj.get("oldLevel").getAsInt() : -1;
        int newLevel = obj.has("newLevel") && !obj.get("newLevel").isJsonNull() ? obj.get("newLevel").getAsInt() : Integer.MIN_VALUE;
        ItemStack item = nbtItem.getItem();
        // 防污染: 当前等级已不是本次镶嵌的结果, 说明该修改已被覆盖, 中止拆卸
        if (newLevel != Integer.MIN_VALUE && item.getEnchantmentLevel(enchantment) != newLevel) {
            return false;
        }
        if (oldLevel <= 0) {
            item.removeEnchantment(enchantment);
        } else {
            item.addUnsafeEnchantment(enchantment, oldLevel);
        }
        return true;
    }

}
