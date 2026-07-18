package com.illtamer.plugin.magicgemtiny.config;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.util.CommandExecuteUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * 当前服务器本地保存的方块绑定数据。
 */
@Getter
public class BoundBlockManager {

    private final Logger logger = MagicGemTiny.getInstance().getLogger();
    private final File file;
    private final Map<String, String> commandMap = new HashMap<>();

    public BoundBlockManager() {
        this.file = new File(MagicGemTiny.getInstance().getDataFolder(), "bound-blocks.yml");
        load();
    }

    public void reload() {
        load();
    }

    public void bind(Block block, String command) {
        if (block == null || command == null) {
            return;
        }
        commandMap.put(key(block.getLocation()), CommandExecuteUtil.normalizeCommand(command));
        save();
    }

    public String unbind(Block block) {
        if (block == null) {
            return null;
        }
        String removed = commandMap.remove(key(block.getLocation()));
        if (removed != null) {
            save();
        }
        return removed;
    }

    public String getCommand(Block block) {
        if (block == null) {
            return null;
        }
        return commandMap.get(key(block.getLocation()));
    }

    public boolean isBound(Block block) {
        return getCommand(block) != null;
    }

    private void load() {
        commandMap.clear();
        if (!file.exists()) {
            save();
            return;
        }
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        if (yaml.isConfigurationSection("Blocks")) {
            for (String key : yaml.getConfigurationSection("Blocks").getKeys(false)) {
                String command = yaml.getString("Blocks." + key);
                if (command != null && !command.isBlank()) {
                    commandMap.put(key, CommandExecuteUtil.normalizeCommand(command));
                }
            }
        }
        logger.info("加载 " + commandMap.size() + " 个方块绑定配置");
    }

    public void save() {
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, String> entry : commandMap.entrySet()) {
            yaml.set("Blocks." + entry.getKey(), entry.getValue());
        }
        try {
            yaml.save(file);
        } catch (IOException e) {
            logger.warning("保存方块绑定配置失败: " + e.getMessage());
        }
    }

    private String key(Location location) {
        UUID worldId = location.getWorld() != null ? location.getWorld().getUID() : new UUID(0L, 0L);
        return worldId + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    }

}
