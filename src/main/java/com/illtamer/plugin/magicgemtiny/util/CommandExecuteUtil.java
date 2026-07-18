package com.illtamer.plugin.magicgemtiny.util;

import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 指令执行工具。
 */
@UtilityClass
public class CommandExecuteUtil {

    public enum ExecuteMode {
        PLAYER,
        OP,
        CONSOLE
    }

    public void executeBreakDownCommand(Player player, String line) {
        if (StringUtil.isBlank(line)) {
            return;
        }
        String command = line.trim();
        ExecuteMode mode = ExecuteMode.CONSOLE;

        int splitIndex = command.indexOf(':');
        if (splitIndex > 0) {
            String prefix = command.substring(0, splitIndex).trim();
            ExecuteMode parsed = parseMode(prefix);
            if (parsed != null) {
                mode = parsed;
                command = command.substring(splitIndex + 1).trim();
            }
        }

        execute(player, mode, command);
    }

    public void executeAsPlayer(Player player, String command) {
        execute(player, ExecuteMode.PLAYER, command);
    }

    public void execute(Player player, ExecuteMode mode, String command) {
        command = normalizeCommand(PlaceholderAPI.setPlaceholders(player, command));
        if (StringUtil.isBlank(command)) {
            return;
        }

        switch (mode) {
            case OP -> executeAsOp(player, command);
            case CONSOLE -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            case PLAYER -> player.performCommand(command);
        }
    }

    public String normalizeCommand(String command) {
        if (command == null) {
            return null;
        }
        command = command.trim();
        while (command.startsWith("/")) {
            command = command.substring(1).trim();
        }
        return command;
    }

    private ExecuteMode parseMode(String prefix) {
        if ("player".equalsIgnoreCase(prefix) || "玩家".equals(prefix)) {
            return ExecuteMode.PLAYER;
        }
        if ("op".equalsIgnoreCase(prefix)) {
            return ExecuteMode.OP;
        }
        if ("console".equalsIgnoreCase(prefix) || "server".equalsIgnoreCase(prefix) || "服务器".equals(prefix)) {
            return ExecuteMode.CONSOLE;
        }
        return null;
    }

    private void executeAsOp(Player player, String command) {
        boolean wasOp = player.isOp();
        try {
            player.setOp(true);
            player.performCommand(command);
        } finally {
            player.setOp(wasOp);
        }
    }

}
