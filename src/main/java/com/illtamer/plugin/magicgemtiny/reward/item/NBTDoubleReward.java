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

    /**
     * 拆卸还原：用 inv 逆表达式回滚 NBT 浮点数
     * @return 是否可安全提交拆卸。inv/字段名缺失时返回 false
     * @apiNote 与 execute 中的 var 对称，读取当前值代入 inv 计算还原值。
     *      多颗宝石叠加时, 仅当各宝石使用相同线性表达式才严格守恒(设计约束)。
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        if (StringUtil.isBlank(inv) || StringUtil.isBlank(name)) {
            return false;
        }
        Double current = nbtItem.getDouble(name);
        Double restored = (Double) ExpressionEvaluator.preparedCompile(inv, Collections.singleton(
                Variable.createVariable("v", current))).execute();
        nbtItem.setDouble(name, restored);
        return true;
    }
}
