package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 重命名物品
 * */
public class NameReward extends ItemReward {

    private String name;

    @Override
    protected void init() {
        name = StringUtil.c(getParamString("name", "n", null));
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            ItemStack item = nbtItem.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(name);
            item.setItemMeta(meta);
        } finally {
            json.addProperty("Name", true);
        }
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持改名");
        }
        return true;
    }

    // 拆卸不会还原名字
    @Override
    public boolean disassemble() {
        return true;
    }

}
