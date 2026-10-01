package com.illtamer.plugin.magicgemtiny.util;

import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unicode 上标工具, 用于物品名末尾等级后缀(如 ⁺¹ ⁺² ⁺¹⁰)的读取与改写
 * */
public final class SuperscriptUtil {

    private static final String SUPERSCRIPT_DIGITS = "⁰¹²³⁴⁵⁶⁷⁸⁹";

    // 匹配名称末尾以 ⁺ 开头、后随一个或多个上标数字的后缀, 第 1 组为数字部分
    private static final Pattern SUFFIX_PATTERN = Pattern.compile("⁺([⁰¹²³⁴⁵⁶⁷⁸⁹]+)$");

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
        Matcher matcher = SUFFIX_PATTERN.matcher(name);
        if (!matcher.find()) {
            return 0;
        }
        int value = 0;
        for (char c : matcher.group(1).toCharArray()) {
            value = Math.addExact(Math.multiplyExact(value, 10), SUPERSCRIPT_DIGITS.indexOf(c));
        }
        return value;
    }

    /**
     * 将名称末尾的上标设置为指定值
     * @param level <= 0 时移除后缀
     * @return name 为 null 时返回 null
     * */
    @Nullable
    public static String applyLevel(@Nullable String name, int level) {
        if (name == null) {
            return null;
        }
        String base = SUFFIX_PATTERN.matcher(name).replaceFirst("");
        return level <= 0 ? base : base + "⁺" + toSuperscript(level);
    }

    /**
     * 在名称末尾上标基础上增加 delta(可为负), 无后缀视为 0
     * */
    @Nullable
    public static String increase(@Nullable String name, int delta) {
        return applyLevel(name, getLevel(name) + delta);
    }

}
