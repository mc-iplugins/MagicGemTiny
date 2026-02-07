package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 恢复装备耐久
 * */
public class DurabilityReward extends ItemReward {

    private Integer amount;

    @Override
    protected void init() {
        amount = getParamInteger("amount", null);
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            ItemStack item = nbtItem.getItem();
            Damageable damageable = (Damageable) item.getItemMeta();
            int damage = damageable.getDamage();
            damageable.setDamage(Math.min(damage+amount, damageable.getMaxDamage()));
            item.setItemMeta(damageable);
        } finally {
            json.addProperty("Durability", true);
        }
    }

    @Override
    public boolean tryTest(NBTItem nbtItem) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            throw new ConditionException("该物品不支持设置耐久度");
        }
        if (amount == null) {
            throw new ConditionException("待恢复的耐久度不能为空");
        }
        return true;
    }

    /**
     * 此奖励拆卸不会引发物品耐久度下降
     * */
    @Override
    public boolean disassemble() {
        return true;
    }

}
