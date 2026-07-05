package com.illtamer.plugin.magicgemtiny.config;

import com.illtamer.lib.config.ConfigFile;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 宝石拆卸台 GUI 配置
 * @apiNote 读取 DisassembleGui.yml, 首次加载时自动从 jar 释放
 * */
@Getter
public class DisassembleGuiConfig {

    private static final String FILE_NAME = "DisassembleGui.yml";

    private final ConfigFile configFile;

    // GUI 标题
    private String title;
    // GUI 布局, 每个元素代表一行 (最多 6 行)
    private String[] rows;
    // 边框填充材质
    private Material filler;
    // 空装备槽占位图标
    private ItemStack equipHint;
    // 说明图标
    private ItemStack info;
    // 上一页 / 下一页图标
    private ItemStack prev;
    private ItemStack next;
    // 宝石图标追加的点击提示
    private String clickTip;

    public DisassembleGuiConfig() {
        this.configFile = new ConfigFile(FILE_NAME, MagicGemTiny.getInstance());
        parse();
    }

    public void reload() {
        configFile.reload();
        parse();
    }

    private void parse() {
        FileConfiguration config = configFile.getConfig();
        this.title = StringUtil.c(config.getString("Title", "&8宝石拆卸台"));

        List<String> slots = config.getStringList("Slots");
        if (slots.isEmpty()) {
            slots = new ArrayList<>();
            slots.add("####E####");
            slots.add("#GGGGGGG#");
            slots.add("#GGGGGGG#");
            slots.add("#GGGGGGG#");
            slots.add("#GGGGGGG#");
            slots.add("<###I###>");
        }
        // 最多 6 行
        if (slots.size() > 6) {
            slots = new ArrayList<>(slots.subList(0, 6));
        }
        this.rows = slots.toArray(new String[0]);

        this.filler = parseMaterial(config.getString("Filler"), Material.BLACK_STAINED_GLASS_PANE);
        this.equipHint = parseIcon(config.getConfigurationSection("EquipHint"), Material.ITEM_FRAME, "&e放入待拆卸装备");
        this.info = parseIcon(config.getConfigurationSection("Info"), Material.BOOK, "&b拆卸说明");
        this.prev = parseIcon(config.getConfigurationSection("Prev"), Material.ARROW, "&8上一页");
        this.next = parseIcon(config.getConfigurationSection("Next"), Material.ARROW, "&8下一页");
        this.clickTip = StringUtil.c(config.getString("ClickTip", "&e▶ 点击拆卸此宝石"));
    }

    private Material parseMaterial(String name, Material def) {
        if (StringUtil.isBlank(name)) {
            return def;
        }
        Material material = Material.getMaterial(name.toUpperCase());
        return material != null ? material : def;
    }

    /**
     * 解析图标配置段为 ItemStack
     * @param section 图标配置段, 支持 Material / Name / Lore
     * */
    private ItemStack parseIcon(ConfigurationSection section, Material defMaterial, String defName) {
        Material material = defMaterial;
        String name = defName;
        List<String> lore = new ArrayList<>();
        if (section != null) {
            material = parseMaterial(section.getString("Material"), defMaterial);
            name = section.getString("Name", defName);
            lore = section.getStringList("Lore");
        }
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (StringUtil.isNotBlank(name)) {
                meta.setDisplayName(StringUtil.c(name));
            }
            if (!lore.isEmpty()) {
                meta.setLore(StringUtil.c(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

}
