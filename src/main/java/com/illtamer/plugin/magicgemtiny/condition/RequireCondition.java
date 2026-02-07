package com.illtamer.plugin.magicgemtiny.condition;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import de.tr7zw.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class RequireCondition implements Condition {

    private static final Logger logger = MagicGemTiny.getInstance().getLogger();

    public static final Set<Material> WEAPON = new HashSet<>();
    public static final Set<Material> ARMOR = new HashSet<>();
    public static final Set<Material> TOOL = new HashSet<>();

    private final List<String> requires;

    @Override
    public boolean checkWithDefaultRequires(ItemStack item, boolean andMode) {
        return check(item, andMode, this.requires);
    }

    @Override
    public boolean check(ItemStack item, boolean andMode, @Nullable List<String> requires) {
        if (requires == null || requires.isEmpty()) {
            return true;
        }

        for (String require : requires) {
            boolean match = checkSingleRequire(require, item);
            if (andMode) { // and 模式
                if (!match) {
                    return false;
                }
            } else { // or 模式
                if (match) {
                    return true;
                }
            }
        }
        return andMode ? true : false;
    }

    private boolean checkSingleRequire(String require, ItemStack item) {
        Material material = item.getType();
        if ("WEAPON".equalsIgnoreCase(require)) {
            return WEAPON.contains(material);
        } else if ("ARMOR".equalsIgnoreCase(require)) {
            return ARMOR.contains(material);
        } else if ("TOOL".equalsIgnoreCase(require)) {
            return TOOL.contains(material);
        }
        if (!require.contains(":")) {
            return material.name().endsWith(require.toUpperCase(Locale.ROOT));
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        // 特殊识别：支持显示名/Lore/NBT/NBT字符串/宝石识别。格式为关键词+冒号+识别内容
        String[] splits = require.split(":");
        String key = splits[0];
        String value1 = splits[1];
        if ("NAME".equalsIgnoreCase(key)) {
            return meta.getDisplayName().contains(value1);
        } else if ("LORE".equalsIgnoreCase(key)) {
            return Optional.ofNullable(meta.getLore()).orElse(Collections.emptyList()).stream()
                    .anyMatch(l -> l.contains(value1));
        } else if ("NBT".equalsIgnoreCase(key)) {
            NBTItem nbtItem = new NBTItem(item);
            if (splits.length == 2) { // NBT:要检测存在的NBT名称
                return nbtItem.hasTag(value1);
            } else { // NBT:要检测的NBT名称:要检测的NBT内容
                String value2 = splits[2];
                return Objects.equals(nbtItem.getString(value1), value2);
            }
        } else if ("GEM".equalsIgnoreCase(key)) {
            NBTItem nbtItem = new NBTItem(item);
            String magicgemName = nbtItem.getString("MAGICGEM_NAME");
            return Objects.equals(magicgemName, value1);
        } else {
            logger.warning("不识别的特殊识别 Require: " + require);
            return false;
        }
    }

    static {
        List<Material> axeList = Arrays.asList(
            Material.WOODEN_AXE,
            Material.STONE_AXE,
            Material.GOLDEN_AXE,
            Material.IRON_AXE,
            Material.DIAMOND_AXE,
            Material.NETHERITE_AXE,
            Material.LEGACY_WOOD_AXE,
            Material.LEGACY_STONE_AXE,
            Material.LEGACY_GOLD_AXE,
            Material.LEGACY_IRON_AXE,
            Material.LEGACY_DIAMOND_AXE
        );

        // Weapon: 含剑/斧/弓/弩/三叉戟
        WEAPON.addAll(axeList);
        WEAPON.addAll(Arrays.asList(
                Material.WOODEN_SWORD,
                Material.STONE_SWORD,
                Material.GOLDEN_SWORD,
                Material.IRON_SWORD,
                Material.DIAMOND_SWORD,
                Material.NETHERITE_SWORD,
                Material.LEGACY_WOOD_SWORD,
                Material.LEGACY_STONE_SWORD,
                Material.LEGACY_GOLD_SWORD,
                Material.LEGACY_IRON_SWORD,
                Material.LEGACY_DIAMOND_SWORD,

                Material.BOW,
                Material.CROSSBOW,
                Material.TRIDENT
        ));

        // Armor: 包括头盔/胸甲/护腿/靴子/鞘翅。注意不包括盾牌
        ARMOR.addAll(Arrays.asList(
            Material.TURTLE_HELMET,
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            Material.DIAMOND_HELMET,
            Material.GOLDEN_HELMET,
            Material.NETHERITE_HELMET,
            Material.LEGACY_LEATHER_HELMET,
            Material.LEGACY_CHAINMAIL_HELMET,
            Material.LEGACY_IRON_HELMET,
            Material.LEGACY_DIAMOND_HELMET,
            Material.LEGACY_GOLD_HELMET,

            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE,
            Material.LEGACY_LEATHER_CHESTPLATE,
            Material.LEGACY_CHAINMAIL_CHESTPLATE,
            Material.LEGACY_IRON_CHESTPLATE,
            Material.LEGACY_DIAMOND_CHESTPLATE,
            Material.LEGACY_GOLD_CHESTPLATE,

            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.NETHERITE_LEGGINGS,
            Material.LEGACY_LEATHER_LEGGINGS,
            Material.LEGACY_CHAINMAIL_LEGGINGS,
            Material.LEGACY_IRON_LEGGINGS,
            Material.LEGACY_DIAMOND_LEGGINGS,
            Material.LEGACY_GOLD_LEGGINGS,

            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.NETHERITE_BOOTS,
            Material.LEGACY_LEATHER_BOOTS,
            Material.LEGACY_CHAINMAIL_BOOTS,
            Material.LEGACY_IRON_BOOTS,
            Material.LEGACY_DIAMOND_BOOTS,
            Material.LEGACY_GOLD_BOOTS,

            Material.ELYTRA,
            Material.LEGACY_ELYTRA
        ));

        // 包括斧/镐/铲/锄
        TOOL.addAll(axeList);
        TOOL.addAll(Arrays.asList(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.IRON_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE,
            Material.LEGACY_WOOD_PICKAXE,
            Material.LEGACY_STONE_PICKAXE,
            Material.LEGACY_GOLD_PICKAXE,
            Material.LEGACY_IRON_PICKAXE,
            Material.LEGACY_DIAMOND_PICKAXE,

            Material.WOODEN_SHOVEL,
            Material.STONE_SHOVEL,
            Material.GOLDEN_SHOVEL,
            Material.IRON_SHOVEL,
            Material.DIAMOND_SHOVEL,
            Material.NETHERITE_SHOVEL,

            Material.WOODEN_HOE,
            Material.STONE_HOE,
            Material.GOLDEN_HOE,
            Material.IRON_HOE,
            Material.DIAMOND_HOE,
            Material.NETHERITE_HOE,
            Material.LEGACY_WOOD_HOE,
            Material.LEGACY_STONE_HOE,
            Material.LEGACY_GOLD_HOE,
            Material.LEGACY_IRON_HOE,
            Material.LEGACY_DIAMOND_HOE
        ));
    }

}
