package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


/**
 * 为装备添加某些原版标志
 * */
public class ItemFlagReward extends ItemReward {

    private String flag;
    private ItemFlag itemFlag;

    @Override
    protected void init() {
        flag = getParamString("flag", null);
        if (StringUtil.isNotBlank(flag)) {
            itemFlag = ItemFlag.valueOf(flag.toUpperCase());
        }
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            ItemStack item = nbtItem.getItem();
            ItemMeta meta = item.getItemMeta();
            meta.addItemFlags(itemFlag);
            item.setItemMeta(meta);
        } finally {
            JsonArray array = json.getAsJsonArray("ItemFlag");
            if (array == null) {
                array = new JsonArray();
                json.add("ItemFlag", array);
            }
            array.add(flag);
        }
    }

    @Override
    public boolean tryTest(NBTItem nbtItem) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持设置元数据");
        }
        if (itemFlag == null) {
            throw new ConditionException("不存在的物品标志 " + flag + " ，请联系管理员");
        }
        if (meta.hasItemFlag(itemFlag)) {
            throw new ConditionException("这个物品已经设置过标志: " + flag);
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

}
