package com.illtamer.plugin.magicgemtiny.command;

import com.google.gson.JsonElement;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.config.BoundBlockManager;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.gui.BreakDownGui;
import com.illtamer.plugin.magicgemtiny.gui.DisassembleGui;
import com.illtamer.plugin.magicgemtiny.gui.ReforgeGui;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandHandler implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return showHelp(sender);
        }

        MagicGemTiny instance = MagicGemTiny.getInstance();
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "success" -> {
                return handleSuccess(sender);
            }
            case "disassemble" -> {
                return handleOpenGui(sender, "magicgem.command.disassemble", DisassembleGui::open);
            }
            case "reforge" -> {
                return handleOpenGui(sender, "magicgem.command.reforge", ReforgeGui::open);
            }
            case "breakdown" -> {
                return handleOpenGui(sender, "magicgem.command.breakdown", BreakDownGui::open);
            }
            case "reload" -> {
                return handleReload(sender, instance);
            }
            case "bindblock" -> {
                return handleBindBlock(sender, args);
            }
            case "unbindblock" -> {
                return handleUnbindBlock(sender);
            }
            case "enchant", "lore", "material", "nbt", "clearChangeLogValue" -> {
                return handleItemInspect(sender, sub, args);
            }
            case "give" -> {
                return handleGive(sender, args);
            }
            default -> {
                return showHelp(sender);
            }
        }
    }

    private boolean handleSuccess(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage("手上物品不能为空");
            return true;
        }
        if (!ItemUtil.isGem(item)) {
            player.sendMessage("主手物品非宝石");
            return true;
        }
        NBTItem nbtItem = new NBTItem(item);
        String gemUniqueKey = nbtItem.getString(NBTKey.MAGICGEM_NAME);
        Gem gem = MagicGemTiny.getInstance().getGemLoader().getGemMap().get(gemUniqueKey);
        if (gem == null) {
            player.sendMessage("无效的宝石: " + gemUniqueKey);
            return true;
        }

        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem.getType() == Material.AIR) {
            player.sendMessage("§7检测到副手未持有物品，基础成功率计算表达式部分取默认值");
        } else {
            player.sendMessage("§7检测到副手持有物品，基础成功率计算以副手物品为基准");
        }
        double successValue = gem.getSuccess().getDouble(player, offHandItem);
        Integer multiple = nbtItem.getInteger(NBTKey.GEM_SUCCESS_MULTIPLE);
        Integer add = nbtItem.getInteger(NBTKey.GEM_SUCCESS_ADD);
        int multipleValue = multiple == null ? 0 : multiple;
        int addValue = add == null ? 0 : add;
        String oriSuccessStr = String.format("§a基础成功率: %.2f%%; 倍率倍增: %d%%; 倍率增加: %d%%", successValue, multipleValue, addValue);
        if (multiple != null) {
            successValue = successValue * (1 + multiple / 100.0);
        }
        if (add != null) {
            successValue = successValue + add;
        }
        player.sendMessage(String.format("%s\n§6总成功率: %.2f%%", oriSuccessStr, successValue));
        return true;
    }

    private boolean handleOpenGui(CommandSender sender, String permission, java.util.function.Consumer<Player> opener) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        if (!player.hasPermission(permission)) {
            player.sendMessage("你缺少权限 " + permission);
            return true;
        }
        opener.accept(player);
        return true;
    }

    private boolean handleReload(CommandSender sender, MagicGemTiny instance) {
        if (!sender.hasPermission("magicgem.command.admin")) {
            sender.sendMessage("你缺少权限 magicgem.command.admin");
            return true;
        }
        instance.getGemLoader().reload();
        if (instance.getPluginConfig() != null) {
            instance.getPluginConfig().reload();
        }
        if (instance.getDisassembleGuiConfig() != null) {
            instance.getDisassembleGuiConfig().reload();
        }
        if (instance.getReforgeGuiConfig() != null) {
            instance.getReforgeGuiConfig().reload();
        }
        if (instance.getBreakDownGuiConfig() != null) {
            instance.getBreakDownGuiConfig().reload();
        }
        if (instance.getBoundBlockManager() != null) {
            instance.getBoundBlockManager().reload();
        }
        sender.sendMessage("§a配置已重载");
        return true;
    }

    private boolean handleBindBlock(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        if (!sender.hasPermission("magicgem.command.admin")) {
            sender.sendMessage("你缺少权限 magicgem.command.admin");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("§c用法: /mgem bindblock <具体指令>");
            return true;
        }
        Block block = player.getTargetBlockExact(8);
        if (block == null || block.getType().isAir()) {
            player.sendMessage("§c请先将准星对准一个方块");
            return true;
        }

        String command = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        BoundBlockManager manager = MagicGemTiny.getInstance().getBoundBlockManager();
        if (manager == null) {
            player.sendMessage("§c方块绑定配置未加载");
            return true;
        }
        manager.bind(block, command);
        player.sendMessage("§a已将该方块绑定为特殊方块，点击时执行: §f" + command);
        return true;
    }

    private boolean handleUnbindBlock(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        if (!sender.hasPermission("magicgem.command.admin")) {
            sender.sendMessage("你缺少权限 magicgem.command.admin");
            return true;
        }
        Block block = player.getTargetBlockExact(8);
        if (block == null || block.getType().isAir()) {
            player.sendMessage("§c请先将准星对准一个方块");
            return true;
        }
        BoundBlockManager manager = MagicGemTiny.getInstance().getBoundBlockManager();
        if (manager == null) {
            player.sendMessage("§c方块绑定配置未加载");
            return true;
        }
        String removed = manager.unbind(block);
        if (removed == null) {
            player.sendMessage("§c该方块没有绑定任何指令");
        } else {
            player.sendMessage("§a已解除该方块绑定");
        }
        return true;
    }

    private boolean handleItemInspect(CommandSender sender, String sub, String[] args) {
        if (!sender.hasPermission("magicgem.command.admin")) {
            sender.sendMessage("你缺少权限 magicgem.command.admin");
            return true;
        }
        if (args.length == 1 && !"clearChangeLogValue".equals(sub)) {
            return handleSingleItemInspect(sender, sub);
        }
        if (args.length == 2 && "nbt".equals(sub)) {
            return handleReadNbt(sender, args[1]);
        }
        if (args.length == 1 && "clearChangeLogValue".equals(sub)) {
            return handleClearChangeLog(sender);
        }
        return true;
    }

    private boolean handleSingleItemInspect(CommandSender sender, String sub) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            player.sendMessage("手上物品不能为空");
            return true;
        }

        switch (sub) {
            case "enchant" -> {
                String[] message = item.getEnchantments().entrySet().stream()
                        .map(e -> String.format("附魔: %s, 等级: %d", e.getKey().getKey(), e.getValue()))
                        .toArray(String[]::new);
                player.sendMessage(message);
            }
            case "lore" -> {
                ItemMeta meta = item.getItemMeta();
                if (meta == null || !meta.hasLore() || meta.getLore() == null || meta.getLore().isEmpty()) {
                    player.sendMessage("主手物品不存在Lore");
                    return true;
                }
                player.sendMessage(meta.getLore().toArray(new String[0]));
            }
            case "material" -> player.sendMessage("名称: " + item.getType().name() + ", 子ID: 高版本不存在");
            case "nbt" -> player.sendMessage(new NBTItem(item).asNBTString());
            default -> {
            }
        }
        return true;
    }

    private boolean handleReadNbt(CommandSender sender, String nbtKey) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        NBTItem nbtItem = new NBTItem(item);
        player.sendMessage(nbtItem.getOrDefault(nbtKey, "不存在该属性"));
        return true;
    }

    private boolean handleClearChangeLog(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("仅玩家可用");
            return true;
        }
        ItemStack item = player.getInventory().getItemInMainHand();
        NBTItem nbtItem = new NBTItem(item);
        ItemUtil.computeJsonArray(nbtItem, NBTKey.MAGICGEM_CHANGE_LOG, array -> {
            array.asList().stream().map(JsonElement::getAsJsonObject).forEach(e ->
                    e.entrySet().removeIf(entry -> !"Name".equals(entry.getKey())));
        });
        player.getInventory().setItemInMainHand(nbtItem.getItem());
        ItemUtil.computeJsonArray(nbtItem, NBTKey.MAGICGEM_CHANGE_LOG, array ->
                player.sendMessage("清除changeLog成功，当前MAGICGEM_CHANGE_LOG:" + array.toString()));
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("magicgem.command.admin")) {
            sender.sendMessage("你缺少权限 magicgem.command.admin");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§c用法: /mgem give <玩家> <宝石名> (数量)");
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("玩家 " + args[1] + " 不在线");
            return true;
        }
        Gem gem = MagicGemTiny.getInstance().getGemLoader().getGemMap().get(args[2]);
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
        ItemUtil.giveOrDropItem(target, gem.getItem(amount));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        List<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.addAll(Arrays.asList("reload", "enchant", "lore", "material", "nbt", "success", "give", "disassemble", "reforge", "breakdown", "bindblock", "unbindblock", "clearChangeLogValue"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "nbt" -> result.add("<属性名>");
                case "give" -> result.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                case "bindblock" -> result.add("<具体指令>");
                default -> {
                }
            }
        } else if (args.length == 3 && "give".equalsIgnoreCase(args[0])) {
            result.addAll(MagicGemTiny.getInstance().getGemLoader().getGemMap().keySet());
        } else if (args.length == 4 && "give".equalsIgnoreCase(args[0])) {
            result.add("(数量)");
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return result.stream().filter(text -> text.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
    }

    private boolean showHelp(CommandSender sender) {
        sender.sendMessage(Arrays.asList(
                "/mgem reload 重载配置文件",
                "/mgem disassemble 打开宝石拆卸台",
                "/mgem reforge 打开宝石重铸台",
                "/mgem breakdown 打开宝石分解台",
                "/mgem bindblock <具体指令> 绑定准星方块为特殊方块",
                "/mgem unbindblock 解除准星方块绑定",
                "/mgem enchant 列出主手物品的附魔(包括其它插件附魔)",
                "/mgem lore 列出主手物品的Lore",
                "/mgem material 显示主手物品的名称和子ID",
                "/mgem nbt 列出主手中物品的NBT标签和类型",
                "/mgem success 列出主手宝石对副手物品的成功机率",
                "/mgem nbt <属性名> 读取主手物品的指定属性",
                "/mgem give <玩家> <宝石名> (数量) 给玩家指定数量的某种宝石(默认1个)"
        ).toArray(new String[0]));
        return true;
    }

}
