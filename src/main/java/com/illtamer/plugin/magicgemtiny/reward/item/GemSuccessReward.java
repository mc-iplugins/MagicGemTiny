package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;

/**
 * 宝石成功机率变动
 * TODO 物品宝石支持倍增成功机率
 * */
public class GemSuccessReward extends ItemReward {

    private Integer add;
    private Integer multiple;
    private Integer limit;

    @Override
    protected void init() {
        add = getParamInteger("add", null);
        multiple = getParamInteger("multiple", null);
        limit = getParamInteger("limit", null);
        limit = limit == null ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        int newAdd = 0, newMultiple = 0;
        try {
            int oldAdd = nbtItem.getInteger("GEM_SUCCESS_ADD");
            int oldMultiple = nbtItem.getInteger("GEM_SUCCESS_MULTIPLE");

            if (add != null) {
                newAdd = Math.min(oldAdd + add, limit);
                nbtItem.setInteger("GEM_SUCCESS_ADD", newAdd);
            }
            if (multiple != null) {
                newMultiple = Math.min(oldMultiple + multiple, limit);
                nbtItem.setInteger("GEM_SUCCESS_MULTIPLE", newMultiple);
            }
        } finally {
            JsonObject obj = new JsonObject();
            if (newAdd != 0) {
                obj.addProperty("newAdd", newAdd);
            }
            if (newMultiple != 0) {
                obj.addProperty("newMultiple", newMultiple);
            }
            json.add("GemSuccess", obj);
        }
    }

    @Override
    public boolean tryTest(NBTItem nbtItem) {
        int oldAdd = nbtItem.getInteger("GEM_SUCCESS_ADD");
        if (add != null && oldAdd >= limit) {
            throw new ConditionException("该物品的宝石倍率增加机率已达上限");
        }
        int oldMultiple = nbtItem.getInteger("GEM_SUCCESS_MULTIPLE");
        if (multiple != null && oldMultiple >= limit) {
            throw new ConditionException("该物品的宝石倍率倍增机率已达上限");
        }
        if (add == null && multiple == null) {
            throw new ConditionException("宝石倍率增加/倍增机率为空");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

}
