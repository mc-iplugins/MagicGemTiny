package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.EnumUtil;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * 为装备增加指定的原版属性
 * */
public class AttributeReward extends ItemReward {

    private Attribute name;
    // 操作有三种：0（加和）1（增加一个比例）2（增加总系数）
    private AttributeModifier.Operation operation;
    // 根据装备类型自动推断生效部位
    private boolean autoSlot = false;
    // 生效部位
    private EquipmentSlot slot;
    // 表达式，用于计算强化后的属性，但此处不能使用物品或PAPI变量，且必须包含字母v
    private String var;
    // 表达式，用于拆卸时还原属性，不能使用物品或PAPI变量，且必须包含字母v
    // - 没有inv则宝石不可拆卸也不可降级。
    // - 请将inv写成var的逆计算式子, 如var=v+1，则inv=v-1
    private @Nullable String inv;
    // 填任意大于零整数，则属性超过上限时不能再打宝石
    private Integer limit;

    @Override
    protected void init() {
        name = EnumUtil.getAttribute(getParamString("name", null));
        operation = EnumUtil.getAttributeOperation(getParamInteger("operation", "op", null));
        String slot = getParamString("slot", null);
        if ("auto".equalsIgnoreCase(slot)) {
            autoSlot = true;
        } else {
            this.slot = EnumUtil.getSlot(slot);
        }
        var = getParamString("var", null);
        inv = getParamString("inv", null);
        limit = getParamInteger("limit", null);
        limit = limit == null || limit <= 0 ? Integer.MAX_VALUE : limit;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        try {
            ItemStack item = nbtItem.getItem();
            ItemMeta meta = item.getItemMeta();

            EquipmentSlot modifySlot = slot;
            if (autoSlot) {
                modifySlot = item.getType().getEquipmentSlot();
            }

            ItemUtil.modifyAttribute(meta, name, "AttributeReward", operation, modifySlot, oldAmount ->
                    (Double) ExpressionEvaluator.preparedCompile(var, Collections.singleton(Variable.createVariable("v", oldAmount))).execute());
            item.setItemMeta(meta);
        } finally {
            // 无需写入记录，要拆卸时现场根据inv计算
        }
    }

    // 拆卸根据inv表达式实现

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持修改属性");
        }
        if (name == null) {
            throw new ConditionException("找不到对应的属性: " + getParamString("name", null));
        }
        double total = ItemUtil.getTotalAttribute(meta, name);
        if (total >= limit) {
            throw new ConditionException("该装备属性已达上限");
        }
        if (operation == null) {
            throw new ConditionException("未知的操作类型: " + getParamString("operation", "op", null));
        }
        if (!autoSlot && slot == null) {
            throw new ConditionException("未知的生效部位: " + getParamString("slot", null));
        }
        if (StringUtil.isBlank(var)) {
            throw new ConditionException("强化表达公式为空");
        }
        return true;
    }

    @Override
    public boolean disassemble() {
        return StringUtil.isNotBlank(inv);
    }

}
