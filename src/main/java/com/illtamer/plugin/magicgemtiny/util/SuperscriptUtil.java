package com.illtamer.plugin.magicgemtiny.util;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unicode 上标工具, 用于物品名末尾等级后缀(如 ⁺¹ ⁺² ⁺¹⁰)的读取与改写
 * @apiNote 末尾后缀定义为「可选 prefix + 上标数字」整体, 读取/摘除时两者一并处理
 * */
public final class SuperscriptUtil {

    private static final String SUPERSCRIPT_DIGITS = "⁰¹²³⁴⁵⁶⁷⁸⁹";

    // 匹配名称末尾的上标数字, 第 1 组为数字部分
    private static final Pattern LEVEL_PATTERN = Pattern.compile("⁺([⁰¹²³⁴⁵⁶⁷⁸⁹]+)$");
    // 名称末尾的格式码段(§x / &x, 含 hex), 用于摘除「前缀 + 上标」整块
    private static final Pattern FORMAT_CODE_RUN_PATTERN =
            Pattern.compile("(?:[§&](?:[0-9a-fk-orx]|#[0-9a-fA-F]{6}))*$");

    private SuperscriptUtil() {}

    /**
     * 将非负整数转换为上标字符串, 如 1 -> ¹, 12 -> ¹²
     * */
    public static String toSuperscript(int number) {
        if (number < 0) {
            throw new IllegalArgumentException("number must be >= 0: " + number);
        }
        String digits = String.valueOf(number);
        StringBuilder sb = new StringBuilder(digits.length());
        for (char c : digits.toCharArray()) {
            sb.append(SUPERSCRIPT_DIGITS.charAt(c - '0'));
        }
        return sb.toString();
    }

    /**
     * 读取名称末尾的上标数值
     * @return 无后缀(或 name 为 null)时返回 0
     * */
    public static int getLevel(@Nullable String name) {
        if (name == null) {
            return 0;
        }
        Matcher matcher = LEVEL_PATTERN.matcher(name);
        if (!matcher.find()) {
            return 0;
        }
        return parseSuperscriptDigits(matcher.group(1));
    }

    /**
     * 将名称末尾的上标设置为指定值(不带 prefix)
     * */
    @Nullable
    public static String applyLevel(@Nullable String name, int level) {
        return applyLevel(name, level, null);
    }

    /**
     * 将名称末尾的上标设置为指定值, 并按需写入 prefix
     * @param level <= 0 时移除整块后缀(prefix + 上标)
     * @param prefix 上标数字前的前缀(如 "§c"), 可为 null/空
     * @return name 为 null 时返回 null
     * */
    @Nullable
    public static String applyLevel(@Nullable String name, int level, @Nullable String prefix) {
        if (name == null) {
            return null;
        }
        String base = stripSuffix(name, prefix);
        return level <= 0 ? base : base + (prefix == null ? "" : prefix) + "⁺" + toSuperscript(level);
    }

    /**
     * 在名称末尾上标基础上增加 delta(可为负), 无后缀视为 0(不带 prefix)
     * */
    @Nullable
    public static String increase(@Nullable String name, int delta) {
        return increase(name, delta, null);
    }

    /**
     * 在名称末尾上标基础上增加 delta(可为负), 无后缀视为 0
     * @param prefix 上标数字前的前缀, 已存在同名前缀则复用, 缺失则补上
     * */
    @Nullable
    public static String increase(@Nullable String name, int delta, @Nullable String prefix) {
        return applyLevel(name, getLevel(name) + delta, prefix);
    }

    /**
     * 摘除名称末尾「可选 prefix + 上标数字」整块
     * @apiNote 未配置 prefix 时只摘上标数字; 配置 prefix 时把紧邻上标的整个格式码段一并摘除,
     *      再按配置前缀重写。该约定保证升阶不叠前缀(§c§c⁺²)、换前缀不留残渣(§b§c⁺²),
     *      代价是名称正文末尾若正好以格式码收尾, 首次追加前缀时会被一并吃掉(如 §c宝剑001§l
     *      -> §c宝剑001§c⁺¹)。
     * */
    private static String stripSuffix(String name, @Nullable String prefix) {
        Matcher matcher = LEVEL_PATTERN.matcher(name);
        if (!matcher.find()) {
            return name;
        }
        String base = name.substring(0, matcher.start());
        if (prefix == null || prefix.isEmpty()) {
            return base;
        }
        return FORMAT_CODE_RUN_PATTERN.matcher(base).replaceAll("");
    }

    private static int parseSuperscriptDigits(String digits) {
        int value = 0;
        for (char c : digits.toCharArray()) {
            value = Math.addExact(Math.multiplyExact(value, 10), SUPERSCRIPT_DIGITS.indexOf(c));
        }
        return value;
    }

}
