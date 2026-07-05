package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;

/**
 * 修改物品的NBT字符串
 * */
public class NBTStringReward extends ItemReward {

    // 字符串名称
    private String name;
    // 字符串内容，如果不写就是删除整个字符串（支持拆卸）
    private String var;
    // false=追加字符串,true=新值覆盖旧值
    private boolean force;

    @Override
    protected void init() {
        name = getParamString("name", "key", null);
        var =  getParamString("var", null);
        force = getParamBoolean("force", null);
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        String oldValue = nbtItem.getString(name);
        String newValue = null;
        try {
            String value = oldValue;
            value = StringUtil.isBlank(value) ? "" : value;
            if (StringUtil.isBlank(var)) {
                nbtItem.removeKey(name);
                newValue = null;
            } else {
                newValue = value = force ? var : value+var;
                nbtItem.setString(name, value);
            }
        } finally {
            JsonObject obj = new JsonObject();
            obj.addProperty("oldValue", oldValue);
            obj.addProperty("newValue", newValue);
            json.add("NBTString", obj);
        }
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) {
        if (StringUtil.isBlank(name)) {
            throw new ConditionException("待修改的NBT名称为空，请联系管理员检查配置");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 拆卸还原：将 NBT 字符串设回镶嵌前的 oldValue
     * @return 是否可安全提交拆卸。当前值已非本次镶嵌结果(被覆盖/篡改)时返回 false 以中止拆卸,
     *      对多颗修改同一 NBT 的宝石强制"后镶嵌先拆卸", 防止乱序拆卸刷取
     * @apiNote oldValue 为空表示镶嵌前无此字段，还原时移除该字段
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get("NBTString");
        if (element == null || !element.isJsonObject()) {
            return true; // 本奖励未产生记录, 无需还原
        }
        JsonObject obj = element.getAsJsonObject();
        if (StringUtil.isBlank(name)) {
            return false; // 字段名无效, 无法还原
        }
        String oldValue = obj.has("oldValue") && !obj.get("oldValue").isJsonNull() ? obj.get("oldValue").getAsString() : null;
        String newValue = obj.has("newValue") && !obj.get("newValue").isJsonNull() ? obj.get("newValue").getAsString() : null;
        // 防污染: 当前值已不是本次镶嵌的结果, 说明该修改已被覆盖, 中止拆卸
        String current = nbtItem.getString(name);
        boolean matchNew = StringUtil.isBlank(newValue) ? StringUtil.isBlank(current) : newValue.equals(current);
        if (!matchNew) {
            return false;
        }
        if (StringUtil.isBlank(oldValue)) {
            nbtItem.removeKey(name);
        } else {
            nbtItem.setString(name, oldValue);
        }
        return true;
    }

}
