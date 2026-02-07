package com.illtamer.plugin.magicgemtiny.util;

import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@UtilityClass
public class EnumUtil {

    @Nullable
    public static AttributeModifier.Operation getAttributeOperation(Integer index) {
        AttributeModifier.Operation[] values = AttributeModifier.Operation.values();
        if (index == null || index < 0 || index >= values.length) {
            return null;
        }
        return values[index];
    }

    @Nullable
    public static EquipmentSlot getSlot(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        return EquipmentSlot.valueOf(name.toUpperCase(Locale.ROOT));
    }

    @Nullable
    public static Attribute getAttribute(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        return Registry.ATTRIBUTE.get(NamespacedKey.minecraft(name));
    }

    @Nullable
    public static Enchantment getEnchantment(String name) {
        if (StringUtil.isBlank(name)) {
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(name.toLowerCase(Locale.ROOT));
        if (key == null) {
            return null;
        }
        return Registry.ENCHANTMENT.get(key);
    }

}
