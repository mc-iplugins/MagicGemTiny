package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.illtamer.lib.Pair;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑物品Lore数字
 * */
public class LoreVarReward extends ItemReward {

    // 变量名：要编辑的数字前面的识别标志。如"物理伤害"。
    // - 插件将会读取其后的第一个整数或者浮点数参与计算
    // - 识别变量名和读取数字均忽略颜色代码
    private String lore;
    private String var;
    private @Nullable String inv;
    private String limit;
    // 格式;(允许新增才填)
    private String format;
    // 模式
    private String mode;
    // 定位lore
    private String locator;
    // 是否把数字加到宝石前面
    private boolean prefix;
    // 第几个数字
    private Integer index;

    @Override
    protected void init() {
        lore = StringUtil.clearColor(StringUtil.clearColor(getParamString("lore", null)));
        var = getParamString("var", null);
        inv = getParamString("inv", null);
        limit = getParamString("limit", null);
        format = getParamString("format", null);
        format = StringUtil.isBlank(format) ? "%.0f" : format;
        mode = getParamString("mode", null);
        locator = StringUtil.clearColor(getParamString("locator", null));
        prefix = getParamBoolean("prefix", null);
        index = getParamInteger("index", null);
        index = index == null ? 1 : index;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();

        List<String> loreList = new ArrayList<>(meta.getLore());
        JsonObject record = new JsonObject();

        // 查找是否存在现有的变量名 Pair<lore所在索引, oldValue>
        Pair<Integer, Double> pair = anyMatchInLoreList(loreList);

        try {
            // 计算新值
            double newValue = Double.parseDouble(ExpressionEvaluator.evaluate(var, Collections.singleton(
                    Variable.createVariable("v", pair != null ? pair.getValue() : 0.0))).toString());

            if (pair != null) { // 替换逻辑
                int index = pair.getKey();
                String oldLore = loreList.get(index);
                double oldValue = pair.getValue();
                String newLore = oldLore.replace(String.valueOf(oldValue), String.valueOf(newValue));
                loreList.set(index, newLore);
                // record
                record(index, oldValue, oldLore, newValue, newLore, true, record);
            } else if (StringUtil.isNotBlank(mode)) { // 变量名没有找到时，根据locator添加
                // 处理 C 语言风格格式化，替换 %% 为 %
                String formatNewValue = String.format(format.replace("%%", "%"), newValue);
                String newLoreSub = prefix ? (formatNewValue + lore) : (lore + formatNewValue);
                int insertPos = loreList.size(); // 默认末尾
                boolean isReplace = false;

                switch (mode.toLowerCase()) {
                    case "first" -> insertPos = 0;
                    case "last" -> insertPos = loreList.size();
                    case "before", "after", "line" -> {
                        int locIdx = -1;
                        for (int i = 0; i < loreList.size(); i++) {
                            if (StringUtil.clearColor(loreList.get(i)).contains(locator)) {
                                locIdx = i;
                                break;
                            }
                        }
                        if (locIdx != -1) {
                            if ("before".equals(mode)) insertPos = locIdx;
                            else if ("after".equals(mode)) insertPos = locIdx + 1;
                            else {
                                insertPos = locIdx;
                                isReplace = true;
                            }
                        }
                    }
                }

                if (isReplace && insertPos < loreList.size()) { // 将locator替换为变量名
                    String oldLore = loreList.get(insertPos);
                    String newLore = oldLore.replace(locator, newLoreSub);
                    loreList.set(insertPos, newLore);
                    // record
                    record(insertPos, null, oldLore, newValue, newLore, true, record);
                } else { // 新增
                    loreList.add(insertPos, newLoreSub);
                    // record
                    record(insertPos, null, null, newValue, newLoreSub, false, record);
                }
            }

            meta.setLore(loreList);
            item.setItemMeta(meta);
        } finally {
            json.add("LoreVar", record);
        }
    }

    /**
     * @param oldValue 变量名没有找到时，根据locator添加时，始终为null
     * @param oldLore 变量名没有找到时，根据locator添加时，找不到locator时，为null
     * */
    private void record(
            int pos,
            @Nullable Double oldValue,
            @Nullable String oldLore,
            double newValue,
            String newLore,
            boolean replace,
            JsonObject json
    ) {
        json.addProperty("index", pos);
        json.addProperty("oldValue", oldValue);
        json.addProperty("oldLore", oldLore);
        json.addProperty("newValue", newValue);
        json.addProperty("newLore", newLore);
        json.addProperty("replace", replace);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null || !meta.hasLore() || meta.getLore().isEmpty()) {
            throw new ConditionException("该物品不支持编辑Lore");
        }
        List<String> loreList = meta.getLore();
        if (StringUtil.isBlank(lore)) {
            throw new ConditionException("要修改的lore不能为空");
        }
        if (StringUtil.isBlank(var)) {
            throw new ConditionException("强化表达公式为空");
        }
        // 结果包含index筛选
        Pair<Integer, Double> pair = anyMatchInLoreList(loreList);
        if (pair == null) { // 变量名没有找到时，需要设置模式
            if (StringUtil.isBlank(mode) || !Arrays.asList("first", "last", "before", "after", "line").contains(mode)) {
                throw new ConditionException("变量名没有找到，不支持的替换模式: " + mode);
            }
            if (StringUtil.isBlank(locator) && Arrays.asList("before", "after", "line").contains(mode)) {
                throw new ConditionException("变量名没有找到，当前处于定位器模式，但定位器为空");
            }
            if ("line".equals(mode)) { // line模式下将locator替换为变量名
                int locIdx = -1;
                for (int i = 0; i < loreList.size(); i++) {
                    if (StringUtil.clearColor(loreList.get(i)).contains(locator)) {
                        locIdx = i;
                        break;
                    }
                }
                if (locIdx == -1) {
                    throw new ConditionException("变量名没有找到，line模式下找不到locator");
                }
            }
        }
        if (format.contains("ROMAN")) {
            throw new ConditionException("暂不支持提取罗马数字");
        }

        // 判断装备属性是否达上限
        return StringUtil.isNotBlank(limit) && checkLimit(pair != null ? pair.getValue() : 0.0);
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 查找是否存在现有的变量名
     * @return lore所在索引, oldValue
     * */
    @Nullable
    protected Pair<Integer, Double> anyMatchInLoreList(List<String> loreList) {
        // 正则：匹配整数或浮点数
        Pattern numberPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

        for (int i = 0; i < loreList.size(); i++) {
            String plainLine = StringUtil.clearColor(loreList.get(i));
            if (plainLine.contains(lore)) {
                Matcher m = numberPattern.matcher(plainLine);
                int count = 0;
                while (m.find()) {
                    count++;
                    if (count == index) {
                        return new Pair<>(i, Double.parseDouble(m.group()));
                    }
                }
                // TODO 处理罗马数字逻辑 (如果 format 是 ROMAN)
            }
        }
        return null;
    }

    /**
     * 校验逻辑表达式 (如 v<=10)
     */
    private boolean checkLimit(double v) {
        List<Variable> variables = new ArrayList<>();
        variables.add(Variable.createVariable("v", v));
        Object result = ExpressionEvaluator.evaluate(limit, variables);
        return result instanceof Boolean ? (Boolean) result : false;
    }

}
