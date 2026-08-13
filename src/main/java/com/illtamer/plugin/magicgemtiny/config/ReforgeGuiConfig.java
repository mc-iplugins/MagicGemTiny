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
 * 宝石重铸 GUI 配置。
 */
@Getter
public class ReforgeGuiConfig {

    private static final String FILE_NAME = "ReforgeGui.yml";

    private final ConfigFile configFile;

    private String title;
    private String[] rows;
    private Material filler;
    private ItemStack button;
    private ItemStack info;
    private String needThreeTip;
    private String sameReforgeTip;
    private String outputNotEmptyTip;
    private String noPoolTip;
    private String successRewardTip;

    public ReforgeGuiConfig() {
        this.configFile = new ConfigFile(FILE_NAME, MagicGemTiny.getInstance());
        parse();
    }

    public void reload() {
        configFile.reload();
        parse();
    }

    private void parse() {
        FileConfiguration config = configFile.getConfig();
        this.title = StringUtil.c(config.getString("Title", "&8宝石重铸"));

        List<String> slots = config.getStringList("Slots");
        if (slots.isEmpty()) {
            slots = new ArrayList<>();
            slots.add("#########");
            slots.add("##1#2#3##");
            slots.add("####R####");
            slots.add("####O####");
            slots.add("####I####");
        }
        if (slots.size() > 6) {
            slots = new ArrayList<>(slots.subList(0, 6));
        }
        this.rows = slots.toArray(new String[0]);

        this.filler = GuiItemUtil.parseMaterial(config.getString("Filler"), Material.BLACK_STAINED_GLASS_PANE);
        this.button = GuiItemUtil.parseIcon(config.getConfigurationSection("Button"), Material.ANVIL, "&a开始重铸");
        this.info = GuiItemUtil.parseIcon(config.getConfigurationSection("Info"), Material.BOOK, "&b重铸说明");
        this.needThreeTip = StringUtil.c(config.getString("Messages.NeedThree", "&c请放入3颗可重铸宝石"));
        this.sameReforgeTip = StringUtil.c(config.getString("Messages.SameReforge", "&c3颗宝石必须属于同一Reforge类型"));
        this.outputNotEmptyTip = StringUtil.c(config.getString("Messages.OutputNotEmpty", "&c请先取走重铸产物"));
        this.noPoolTip = StringUtil.c(config.getString("Messages.NoPool", "&c该Reforge类型未配置重铸产出"));
        this.successRewardTip = StringUtil.c(config.getString("Messages.SuccessReward", "&a重铸成功，恭喜你获得{gem}"));
    }

}
