package com.illtamer.plugin.magicgemtiny.entity;

import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.PreparedExpression;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 动态数值处理器
 * 用于解析包含变量、PAPI占位符和NBT标签的表达式
 */
public class DynamicValue {
    // 匹配变量名，$关键词:要读取的内容?默认值$
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([^$]+)\\$");

    private final String originalExpression;
    private PreparedExpression preparedExpression;
    private Object constantValue; // 如果是纯数字，直接缓存结果
    // 变量表达式, ikexpression通用变量名(var_x)
    private final Map<String, String> detectedVariables = new HashMap<>();

    public DynamicValue(String expression) {
        this.originalExpression = expression;
        parse(expression);
    }

    public DynamicValue(double value) {
        this.originalExpression = String.valueOf(value);
        this.constantValue = value;
    }

    /**
     * 解析表达式并预编译
     * @apiNote $func() 能直接解析，需要提取所有 $$ 变量
     */
    private void parse(String expression) {
        try {
            // 尝试直接解析为数字
            this.constantValue = Double.parseDouble(expression);
            return;
        } catch (NumberFormatException ignored) {}

        // 识别表达式中的所有变量
        Matcher matcher = VARIABLE_PATTERN.matcher(expression);
        String processedExpr = expression;

        while (matcher.find()) {
            String varExpr = matcher.group(1);
            String savedCommonVarName = detectedVariables.computeIfAbsent(varExpr,
                    k -> "var_" + detectedVariables.size());
            // 将变量替换为 ikexpression 可识别的变量名
            processedExpr = processedExpr.replace(matcher.group(0), savedCommonVarName);
        }

        // 预编译表达式以提高后续执行效率
        List<Variable> variables = new ArrayList<>();
        for (String savedCommonVarName : detectedVariables.values()) {
            variables.add(Variable.createVariable(savedCommonVarName, 0.0));
        }
        this.preparedExpression = ExpressionEvaluator.preparedCompile(processedExpr, variables);
    }

    public double getDouble(Player player, ItemStack item) {
        Object val = getValue(player, item);
        if (val instanceof Number) return ((Number) val).doubleValue();
        return Double.parseDouble(val.toString());
    }

    public boolean getBoolean(Player player, ItemStack item) {
        Object val = getValue(player, item);
        if (val instanceof Boolean) return (Boolean) val;
        return Boolean.parseBoolean(val.toString());
    }

    /**
     * 获取最终的计算结果
     * @param player 关联玩家（用于PAPI）
     * @param item 关联物品（用于NBT变量）
     * @return 计算结果对象
     */
    public Object getValue(Player player, ItemStack item) {
        if (constantValue != null) return constantValue;
        if (preparedExpression == null) return originalExpression;

        // 注入变量值
        for (Map.Entry<String, String> entry : detectedVariables.entrySet()) {
            String varExpr = entry.getKey();
            String varName = entry.getValue();

            Object value = resolveVariable(varExpr, player, item);
            preparedExpression.setArgument(varName, value);
        }

        return preparedExpression.execute();
    }

    /**
     * 变量解析逻辑
     * TODO LORE、WORD、ENCHANT 待支持
     */
    private Object resolveVariable(String varExpr, Player player, ItemStack item) {
        // 解析物品表达式
        if (item != null && item.getType() != Material.AIR) {
            Object result;
            if ((result = resolveFunc("LORE:", varExpr, (expr, defaultValue) -> {
                throw new UnsupportedOperationException();
            })) != null) {
                return result;
            }

            if ((result = resolveFunc("WORD:", varExpr, (expr, defaultValue) -> {
                throw new UnsupportedOperationException();
            })) != null) {
                return result;
            }

            if ((result = resolveFunc("ENCHANT:", varExpr, (expr, defaultValue) -> {
                throw new UnsupportedOperationException();
            })) != null) {
                return result;
            }

            if ((result = resolveFunc("NBT:", varExpr, (expr, defaultValue) -> {
                NBTItem nbtItem = new NBTItem(item);
                if (nbtItem.hasTag(varExpr)) {
                    // 根据需要可以扩展更多 NBT 类型支持
                    Double d = nbtItem.getDouble(varExpr);
                    if (d != null) return d;
                }
                return defaultValue;
            })) != null) {
                return result;
            }
        }

        // 尝试解析 PAPI 占位符
        if (player != null) {
            String papiValue = PlaceholderAPI.setPlaceholders(player, varExpr);
            try {
                return Double.parseDouble(papiValue);
            } catch (NumberFormatException e) {
                return papiValue;
            }
        }

        return 0.0;
    }

    private <T> T resolveFunc(String prefix, String expr, BiFunction<String, Double, T> exprConsumer) {
        if (!expr.startsWith(prefix)) {
            return null;
        }
        // 带匹配字符串?默认值
        expr = expr.substring(prefix.length());
        int defaultIndex = expr.lastIndexOf('?');
        double defaultValue = 0.0;
        if (defaultIndex != -1) {
            String defaultStr = expr.substring(defaultIndex+1);
            if (StringUtil.isNotBlank(defaultStr)) {
                defaultValue = Double.parseDouble(defaultStr);
            }
            expr = expr.substring(0, defaultIndex);
        }
        return exprConsumer.apply(expr, defaultValue);
    }

}
