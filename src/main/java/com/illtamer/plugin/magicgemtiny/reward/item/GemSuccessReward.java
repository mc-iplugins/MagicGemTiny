package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;

/**
 * 宝石成功机率变动
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
        int deltaAdd = 0, deltaMultiple = 0;
        try {
            int oldAdd = nbtItem.getInteger(NBTKey.GEM_SUCCESS_ADD);
            int oldMultiple = nbtItem.getInteger(NBTKey.GEM_SUCCESS_MULTIPLE);

            if (add != null) {
                newAdd = Math.min(oldAdd + add, limit);
                deltaAdd = newAdd - oldAdd; // 记录本次真实增量, 考虑 limit 截断
                nbtItem.setInteger(NBTKey.GEM_SUCCESS_ADD, newAdd);
            }
            if (multiple != null) {
                newMultiple = Math.min(oldMultiple + multiple, limit);
                deltaMultiple = newMultiple - oldMultiple;
                nbtItem.setInteger(NBTKey.GEM_SUCCESS_MULTIPLE, newMultiple);
            }
        } finally {
            JsonObject obj = new JsonObject();
            if (newAdd != 0) {
                obj.addProperty("newAdd", newAdd);
                obj.addProperty("deltaAdd", deltaAdd);
            }
            if (newMultiple != 0) {
                obj.addProperty("newMultiple", newMultiple);
                obj.addProperty("deltaMultiple", deltaMultiple);
            }
            json.add("GemSuccess", obj);
        }
    }

    @Override
    public boolean tryTest(NBTItem nbtItem) {
        if (nbtItem.getItem().getAmount() > 1) {
            throw new ConditionException("一次仅能操作一个宝石");
        }
        int oldAdd = nbtItem.getInteger(NBTKey.GEM_SUCCESS_ADD);
        if (add != null && oldAdd >= limit) {
            throw new ConditionException("该物品的宝石倍率增加机率已达上限");
        }
        int oldMultiple = nbtItem.getInteger(NBTKey.GEM_SUCCESS_MULTIPLE);
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

    /**
     * 拆卸还原：按本次镶嵌的真实增量逆向减回宝石成功率修正
     * @return 恒为 true, 增量回滚总能安全执行
     * @apiNote 优先用记录的 deltaAdd/deltaMultiple(已考虑 limit 截断)精确回滚;
     *      旧数据无该字段时回退到配置的 add/multiple
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get("GemSuccess");
        JsonObject obj = element != null && element.isJsonObject() ? element.getAsJsonObject() : new JsonObject();

        if (add != null) {
            int delta = obj.has("deltaAdd") && !obj.get("deltaAdd").isJsonNull() ? obj.get("deltaAdd").getAsInt() : add;
            int current = nbtItem.getInteger(NBTKey.GEM_SUCCESS_ADD);
            int restored = Math.max(0, current - delta);
            if (restored == 0) {
                nbtItem.removeKey(NBTKey.GEM_SUCCESS_ADD);
            } else {
                nbtItem.setInteger(NBTKey.GEM_SUCCESS_ADD, restored);
            }
        }
        if (multiple != null) {
            int delta = obj.has("deltaMultiple") && !obj.get("deltaMultiple").isJsonNull() ? obj.get("deltaMultiple").getAsInt() : multiple;
            int current = nbtItem.getInteger(NBTKey.GEM_SUCCESS_MULTIPLE);
            int restored = Math.max(0, current - delta);
            if (restored == 0) {
                nbtItem.removeKey(NBTKey.GEM_SUCCESS_MULTIPLE);
            } else {
                nbtItem.setInteger(NBTKey.GEM_SUCCESS_MULTIPLE, restored);
            }
        }
        return true;
    }

}
