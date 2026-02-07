package com.illtamer.plugin.magicgemtiny.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import de.tr7zw.nbtapi.NBTItem;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemUtil {

    public static JsonArray computeJsonArray(NBTItem nbt, String key, @Nullable Consumer<JsonArray> consumer) {
        String changeLog = nbt.getString(key);
        JsonArray array = StringUtil.isNotBlank(changeLog) ?
                new Gson().fromJson(changeLog, JsonArray.class) : new JsonArray();
        if (consumer != null) {
            consumer.accept(array);
        }
        nbt.setString(key, array.toString());
        return array;
    }

    public static boolean isGem(@Nullable ItemStack item) {
        if (item == null || !item.hasItemMeta() || item.getType().isAir()) {
            return false;
        }

        NBTItem nbtItem = new NBTItem(item);
        String gemUniqueKey = nbtItem.getString(NBTKey.MAGICGEM_NAME);
        return StringUtil.isNotBlank(gemUniqueKey);
    }

    /**
     * 获取一个属性的所有数值
     * */
    public static double getTotalAttribute(@NotNull ItemMeta meta, Attribute attribute) {
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers == null) {
            return 0;
        }
        return modifiers.stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    public static void modifyAttribute(
            @NotNull ItemMeta meta,
            Attribute attribute,
            String keyName,
            AttributeModifier.Operation operation,
            EquipmentSlot slot,
            Function<Double, Double> amountFunc
    ) {
        NamespacedKey targetKey = new NamespacedKey(MagicGemTiny.getInstance(), keyName);
        Collection<AttributeModifier> modifiers = meta.getAttributeModifiers(attribute);
        if (modifiers != null) {
            // 查找匹配的 Modifier
            for (AttributeModifier mod : modifiers) {
                if (mod.getKey().equals(targetKey)) {
                    double oldAmount = mod.getAmount();
                    meta.removeAttributeModifier(attribute, mod);

                    // 创建并添加更新后的 Modifier
                    AttributeModifier newMod = new AttributeModifier(targetKey,
                            amountFunc.apply(oldAmount), operation, slot.getGroup());
                    meta.addAttributeModifier(attribute, newMod);
                    return;
                }
            }
        }
        // 没有历史数据，新增
        AttributeModifier modifier = new AttributeModifier(targetKey,
                amountFunc.apply(0.0), operation, slot.getGroup());
        meta.addAttributeModifier(attribute, modifier);
    }

    /**
     * @param colorStr 颜色枚举名称 / rgb int值
     * */
    public static Color parsePotionColorFromRGB(String colorStr) {
        Color color = null;
        if (StringUtil.isBlank(colorStr)) {
            return color;
        }

        try {
            int rgbValue = Integer.parseInt(colorStr);
            color = Color.fromRGB(rgbValue);
        } catch (NumberFormatException ignored) {}

        if (color == null) {
            switch (colorStr) {
                case "WHITE" -> color = Color.WHITE;
                case "SILVER" -> color = Color.SILVER;
                case "GRAY" -> color = Color.GRAY;
                case "BLACK" -> color = Color.BLACK;
                case "RED" -> color = Color.RED;
                case "MAROON" -> color = Color.MAROON;
                case "YELLOW" -> color = Color.YELLOW;
                case "OLIVE" -> color = Color.OLIVE;
                case "LIME" -> color = Color.LIME;
                case "GREEN" -> color = Color.GREEN;
                case "AQUA" -> color = Color.AQUA;
                case "TEAL" -> color = Color.TEAL;
                case "BLUE" -> color = Color.BLUE;
                case "NAVY" -> color = Color.NAVY;
                case "FUCHSIA" -> color = Color.FUCHSIA;
                case "PURPLE" -> color = Color.PURPLE;
                case "ORANGE" -> color = Color.ORANGE;
                case "GOLD" -> color = Color.fromRGB(255, 170, 0);
            }
        }

        if (color == null) {
            Bukkit.getLogger().warning("不支持的颜色符号: " + colorStr);
        }
        return color;
    }

    /**
     * 根据Base64编码的纹理生成自定义头颅物品
     * @param base64Texture Base64编码的纹理字符串，例如你提供的那个
     * @return 生成的头颅ItemStack
     */
    @SneakyThrows
    public static ItemStack newHead(String base64Texture) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        if (base64Texture == null || base64Texture.isBlank()) {
            return head;
        }
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) {
            return head;
        }

        PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), "CustomHead");
        PlayerTextures textures = profile.getTextures();
        URL url = extractSkinUrlFromBase64(base64Texture);
        if (url == null) {
            head.setItemMeta(meta);
            return head;
        }
        textures.setSkin(url);
        meta.setOwnerProfile(profile);
        head.setItemMeta(meta);
        return head;
    }

    /**
     * 从 Base64 纹理字符串中提取皮肤 URL
     * @param base64 Base64 编码的 {"textures":{"SKIN":{"url":"..."}}} 字符串
     * @return 解析出的皮肤 URL，失败返回 null
     */
    @SneakyThrows
    private static URL extractSkinUrlFromBase64(String base64) {
        byte[] decoded = Base64.getDecoder().decode(base64);
        String json = new String(decoded, java.nio.charset.StandardCharsets.UTF_8);

        // 简单字符串查找 "url":"http..." 的位置
        // Minecraft 的纹理 Base64 格式非常固定，所以可以用这种轻量方式
        String urlMarker = "\"url\":\"";
        int startIndex = json.indexOf(urlMarker);
        if (startIndex == -1) return null;

        startIndex += urlMarker.length();
        int endIndex = json.indexOf('"', startIndex);
        if (endIndex == -1) return null;

        String urlString = json.substring(startIndex, endIndex);

        // 替换转义的反斜杠（某些 Base64 中会有 \\）
        urlString = urlString.replace("\\", "");
        return new URL(urlString);
    }

    public static ItemStack buildGem(String gemUniqueName, Material material, String texture, String display, List<String> tips, int customModelData) {
        ItemStack item;
        if (material != null) {
            item = new ItemStack(material, 1);
        } else {
            try {
                item = newHead(texture);
            } catch (Exception e) {
                item = new ItemStack(Material.PLAYER_HEAD, 1);
                Bukkit.getLogger().warning("宝石 " + gemUniqueName + " 头颅皮肤设置失败");
                e.printStackTrace();
            }
        }
        ItemMeta meta = item.getItemMeta();
        if (StringUtil.isNotBlank(display)) {
            meta.setDisplayName(StringUtil.c(display));
        }
        if (!tips.isEmpty()) {
            meta.setLore(StringUtil.c(tips));
        }
        if (customModelData != 0) {
            meta.setCustomModelData(customModelData);
        }
        item.setItemMeta(meta);

        NBTItem nbtItem = new NBTItem(item);
        nbtItem.setString("Name", gemUniqueName);
        return nbtItem.getItem();
    }

    public static void giveOrDropItem(Player player, ItemStack... items) {
        // addItem 会自动处理堆叠，并返回无法放入的物品（如果有的话）
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(items);

        if (!leftovers.isEmpty()) {
            Location location = player.getLocation();
            for (ItemStack remaining : leftovers.values()) {
                player.getWorld().dropItemNaturally(location, remaining);
            }
            player.sendMessage("§c你的背包已满，物品已掉落在脚下！");
        }
    }

    /**
     * 消耗一个单位的物品
     * */
    public static void consume(ItemStack item) {
        item.setAmount(item.getAmount()-1);
//        // 如果数量变为0或更少，Bukkit 有时不会自动移除物品，显式设为 null
//        if (item.getAmount() <= 0) {
//            player.getInventory().setItem(i, null);
//        }
    }

}
