package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * 物品NBT浮点数修改
 * */
public class NBTDoubleReward extends ItemReward {

    private String name;
    // 强化公式
    private String var;
    // 拆卸公式，没有则不可拆卸
    private @Nullable String inv;
    private Integer limit;

    @Override
    protected void init() {
        name = getParamString("name", "key", null);
        var = getParamString("var", null);
        inv = getParamString("inv", null);
        limit = getParamInteger("limit", null);
        limit = limit == null ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            Double oldValue = nbtItem.getDouble(name);
            Double newValue = (Double) ExpressionEvaluator.preparedCompile(var, Collections.singleton(
                    Variable.createVariable("v", oldValue))).execute();
            nbtItem.setDouble(name, newValue);
        } finally {
            // 无需写入记录，要拆卸时现场根据inv计算
        }
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持修改NBT");
        }
        if (name == null) {
            throw new ConditionException("要修改的NBT字段不能为空");
        }
        if (StringUtil.isBlank(var)) {
            throw new ConditionException("强化表达公式为空");
        }
        if (nbtItem.getInteger(name) >= limit) {
            throw new ConditionException("该装备属性已达上限");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return StringUtil.isNotBlank(inv);
    }
}
