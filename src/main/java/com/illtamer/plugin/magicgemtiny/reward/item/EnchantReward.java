package com.illtamer.plugin.magicgemtiny.reward.item;

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



}
