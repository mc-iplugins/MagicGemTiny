package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 使装备变得无法破坏
 * */
public class UnbreakableReward extends ItemReward {

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            ItemStack item = nbtItem.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.setUnbreakable(true);
            item.setItemMeta(meta);
        } finally {
            json.addProperty("Unbreakable", true);
        }
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持设置耐久度");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

}
