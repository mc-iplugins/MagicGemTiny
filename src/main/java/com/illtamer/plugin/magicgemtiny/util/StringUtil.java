package com.illtamer.plugin.magicgemtiny.util;

import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class StringUtil {

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    public static String c(String text) {
        if (text == null) {
            return null;
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> c(List<String> texts) {
        return texts.stream().map(StringUtil::c).collect(Collectors.toList());
    }

    public static String clearColor(String lore) {
        return ChatColor.stripColor(c(lore));
    }

}
