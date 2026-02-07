package com.illtamer.plugin.magicgemtiny.command;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }
        MagicGemTiny instance = MagicGemTiny.getInstance();
        // /mgem reload 重载配置文件
        // # 备注: 下列功能可以用Tab读到输入栏直接复制(颜色代码会转换成&)"
        // /mgem enchant 列出主手物品的附魔(包括其它插件附魔)
        // /mgem lore 列出主手物品的Lore
        // /mgem material 显示主手物品的名称和子ID
        // /mgem nbt 列出主手中物品的NBT标签和类型
        if (args.length == 1) {
            if (!sender.hasPermission("magicgem.command.admin")) {
                sender.sendMessage("你缺少权限 magicgem.command.admin");
                return true;
            }

            if ("reload".equals(args[0])) {
                instance.getGemLoader().reload();
                return true;
            } else if ("enchant".equals(args[0]) || "lore".equals(args[0]) || "material".equals(args[0]) || "nbt".equals(args[0])) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("仅玩家可用");
                    return true;
                }
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().isAir()) {
                    player.sendMessage("手上物品不能为空");
                    return true;
                }
                if ("enchant".equals(args[0])) {
                    String[] message = item.getEnchantments().entrySet().stream()
                            .map(e -> String.format("附魔: %s, 等级: %d", e.getKey().getKey(), e.getValue()))
                            .collect(Collectors.toList()).toArray(new String[0]);
                    player.sendMessage(message);
                } else if ("lore".equals(args[0])) {
                    ItemMeta meta = item.getItemMeta();
                    if (meta == null || !meta.hasLore() || meta.getLore().isEmpty()) {
                        player.sendMessage("主手物品不存在Lore");
                        return true;
                    }
                    player.sendMessage(meta.getLore().toArray(new String[0]));
                } else if ("material".equals(args[0])) {
                    Material material = item.getType();
                    player.sendMessage("名称: " + material.name() + ", 子ID: 高版本不存在");
                } else { // nbt
                    NBTItem nbtItem = new NBTItem(item);
                    player.sendMessage(nbtItem.asNBTString());
                }
                return true;
            }
        }

        // # 备注: 下列功能可以用Tab读到输入栏直接复制(颜色代码会转换成&)"
        // /mgem nbt <属性名> 读取主手物品的指定属性
        if (args.length == 2) {
            if (!sender.hasPermission("magicgem.command.admin")) {
                sender.sendMessage("你缺少权限 magicgem.command.admin");
                return true;
            }

            if ("nbt".equals(args[0])) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("仅玩家可用");
                    return true;
                }
                String nbtKey = args[1];
                ItemStack item = player.getInventory().getItemInMainHand();
                NBTItem nbtItem = new NBTItem(item);
                player.sendMessage(nbtItem.getOrDefault(nbtKey, "不存在该属性"));
                return true;
            }
        }

        // /mgem give <玩家> <宝石名> (数量) 给玩家指定数量的某种宝石(默认1个)
        if (args.length >= 3) {
            if (!sender.hasPermission("magicgem.command.admin")) {
                sender.sendMessage("你缺少权限 magicgem.command.admin");
                return true;
            }

            if ("give".equals(args[0])) {
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage("玩家 " + args[1] + " 不在线");
                    return true;
                }
                Gem gem = instance.getGemLoader().getGemMap().get(args[2]);
                if (gem == null) {
                    sender.sendMessage("不存在的宝石: " + args[2]);
                    return true;
                }
                int amount = 1;
                if (args.length >= 4) {
                    try {
                        amount = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage("请输入合法的数字");
                        return true;
                    }
                }

                ItemUtil.giveOrDropItem(player, gem.getItem(amount));
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(Arrays.asList("reload", "enchant", "lore", "material", "nbt", "give"));
        }
        if (args.length == 2) {
            if ("nbt".equals(args[0])) {
                result.add("<属性名>");
            } else if ("give".equals(args[0])) {
                result.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            }
        }
        if (args.length == 3) {
            result.addAll(MagicGemTiny.getInstance().getGemLoader().getGemMap().keySet());
        }
        if (args.length == 4) {
            result.add("(数量)");
        }
        return result;
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(Arrays.asList(
                "/mgem reload 重载配置文件",
                "/mgem enchant 列出主手物品的附魔(包括其它插件附魔)",
                "/mgem lore 列出主手物品的Lore",
                "/mgem material 显示主手物品的名称和子ID",
                "/mgem nbt 列出主手中物品的NBT标签和类型",
                "/mgem nbt <属性名> 读取主手物品的指定属性",
                "/mgem give <玩家> <宝石名> (数量) 给玩家指定数量的某种宝石(默认1个)"
        ).toArray(new String[0]));
        return true;
    }

}
