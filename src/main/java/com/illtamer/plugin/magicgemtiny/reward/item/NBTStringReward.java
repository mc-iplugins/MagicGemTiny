package com.illtamer.plugin.magicgemtiny.reward.item;

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

}
