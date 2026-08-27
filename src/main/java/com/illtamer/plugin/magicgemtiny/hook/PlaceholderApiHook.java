package com.illtamer.plugin.magicgemtiny.hook;

import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * PlaceholderAPI 软依赖桥接。
 * 服务器未安装 PlaceholderAPI 时所有解析方法原样返回, 避免类加载错误。
 */
@UtilityClass
public class PlaceholderApiHook {

    private static final boolean AVAILABLE = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;

    public boolean isAvailable() {
        return AVAILABLE;
    }

    public String setPlaceholders(Player player, String text) {
        if (!AVAILABLE || StringUtil.isBlank(text)) {
            return text;
        }
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    public List<String> setPlaceholders(Player player, List<String> texts) {
        if (!AVAILABLE || texts == null || texts.isEmpty()) {
            return texts;
        }
        return PlaceholderAPI.setPlaceholders(player, texts);
    }

}
