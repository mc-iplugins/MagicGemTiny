package com.illtamer.plugin.magicgemtiny.config;

import com.illtamer.lib.config.ConfigFile;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.util.GuiItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 宝石分解台 GUI 配置。
 */
@Getter
public class BreakDownGuiConfig {

    private static final String FILE_NAME = "BreakDownGui.yml";

    private final ConfigFile configFile;

    private String title;
    private String[] rows;
    private Material filler;
    private ItemStack button;
    private ItemStack info;
    private String invalidGemTip;
    private String successTip;

    public BreakDownGuiConfig() {
        this.configFile = new ConfigFile(FILE_NAME, MagicGemTiny.getInstance());
        parse();
    }

    public void reload() {
        configFile.reload();
        parse();
    }

    private void parse() {
        FileConfiguration config = configFile.getConfig();
        this.title = StringUtil.c(config.getString("Title", "&8宝石分解台"));

        List<String> slots = config.getStringList("Slots");
        if (slots.isEmpty()) {
            slots = new ArrayList<>();
            slots.add("#########");
            slots.add("###G#B###");
            slots.add("####I####");
        }
        if (slots.size() > 6) {
            slots = new ArrayList<>(slots.subList(0, 6));
        }
        this.rows = slots.toArray(new String[0]);

        this.filler = GuiItemUtil.parseMaterial(config.getString("Filler"), Material.BLACK_STAINED_GLASS_PANE);
        this.button = GuiItemUtil.parseIcon(config.getConfigurationSection("Button"), Material.GRINDSTONE, "&a开始分解");
        this.info = GuiItemUtil.parseIcon(config.getConfigurationSection("Info"), Material.BOOK, "&b分解说明");
        this.invalidGemTip = StringUtil.c(config.getString("Messages.InvalidGem", "&c该宝石不可分解"));
        this.successTip = StringUtil.c(config.getString("Messages.Success", "&a分解成功"));
    }

}
