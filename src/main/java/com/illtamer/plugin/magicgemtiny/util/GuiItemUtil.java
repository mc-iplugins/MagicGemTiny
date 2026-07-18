package com.illtamer.plugin.magicgemtiny.util;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI 物品配置解析工具。
 */
@UtilityClass
public class GuiItemUtil {

    public ItemStack parseIcon(ConfigurationSection section, Material defMaterial, String defName) {
        Material material = defMaterial;
        String name = defName;
        List<String> lore = new ArrayList<>();
        int customModelData = 0;
        ConfigurationSection customData = null;

        if (section != null) {
            material = parseMaterial(section.getString("Material"), defMaterial);
            name = section.getString("Name", defName);
            lore = section.getStringList("Lore");
            customModelData = section.getInt("CustomModelData", 0);
            customData = getCustomDataSection(section);
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
            if (customModelData != 0) {
                meta.setCustomModelData(customModelData);
            }
            item.setItemMeta(meta);
        }
        applyCustomData(item, customData);
        return item;
    }

    public Material parseMaterial(String name, Material def) {
        if (StringUtil.isBlank(name)) {
            return def;
        }
        Material material = Material.getMaterial(name.toUpperCase());
        return material != null ? material : def;
    }

    private ConfigurationSection getCustomDataSection(ConfigurationSection section) {
        for (String key : new String[]{"custom-data", "CustomData", "custom_data", "Custom-Data"}) {
            ConfigurationSection data = section.getConfigurationSection(key);
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    /**
     * 将配置段写入 1.20.5+ 物品组件 minecraft:custom_data。
     */
    public void applyCustomData(ItemStack item, ConfigurationSection customData) {
        if (item == null || item.getType().isAir() || customData == null) {
            return;
        }
        try {
            NBT.modifyComponents(item, nbt -> {
                ReadWriteNBT data = nbt.getOrCreateCompound("minecraft:custom_data");
                writeSection(data, customData);
            });
        } catch (Throwable throwable) {
            MagicGemTiny.getInstance().getLogger().warning("GUI物品custom-data写入失败: " + throwable.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void writeSection(ReadWriteNBT nbt, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection child) {
                writeSection(nbt.getOrCreateCompound(key), child);
            } else if (value instanceof Boolean bool) {
                nbt.setBoolean(key, bool);
            } else if (value instanceof Integer integer) {
                nbt.setInteger(key, integer);
            } else if (value instanceof Long longValue) {
                nbt.setLong(key, longValue);
            } else if (value instanceof Float floatValue) {
                nbt.setFloat(key, floatValue);
            } else if (value instanceof Double doubleValue) {
                nbt.setDouble(key, doubleValue);
            } else if (value instanceof Number number) {
                nbt.setDouble(key, number.doubleValue());
            } else if (value instanceof List<?> list) {
                writeList(nbt, key, list);
            } else if (value != null) {
                nbt.setString(key, String.valueOf(value));
            }
        }
    }

    private void writeList(ReadWriteNBT nbt, String key, List<?> list) {
        if (list.isEmpty()) {
            nbt.getStringList(key).clear();
            return;
        }
        boolean allInteger = list.stream().allMatch(value -> value instanceof Integer);
        if (allInteger) {
            nbt.getIntegerList(key).clear();
            nbt.getIntegerList(key).addAll((List<Integer>) list);
            return;
        }
        List<String> values = list.stream().map(String::valueOf).toList();
        nbt.getStringList(key).clear();
        nbt.getStringList(key).addAll(values);
    }

}
