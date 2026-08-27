package com.illtamer.plugin.magicgemtiny.gui;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.config.BreakDownGuiConfig;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.hook.PlaceholderApiHook;
import com.illtamer.plugin.magicgemtiny.util.CommandExecuteUtil;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 宝石分解 GUI。
 */
public class BreakDownGui {

    private static final char GEM_CHAR = 'G';
    private static final char BUTTON_CHAR = 'B';
    private static final char INFO_CHAR = 'I';

    private static final Map<UUID, BreakDownGui> OPEN_GUIS = new ConcurrentHashMap<>();

    private final Player player;
    private final BreakDownGuiConfig config;
    private final InventoryGui gui;
    private final Inventory storage;

    private BreakDownGui(Player player, BreakDownGuiConfig config) {
        this.player = player;
        this.config = config;
        this.storage = Bukkit.createInventory(null, 9, "MagicGemBreakDownStorage");

        MagicGemTiny plugin = MagicGemTiny.getInstance();
        this.gui = new InventoryGui(plugin, PlaceholderApiHook.setPlaceholders(player, config.getTitle()), config.getRows());
        this.gui.setFiller(config.getFiller() != null ? new ItemStack(config.getFiller()) : new ItemStack(Material.AIR));

        this.gui.addElement(new GuiStorageElement(
                GEM_CHAR, storage, 0,
                this::scheduleRefresh,
                info -> validateInput(info.getItem()),
                info -> true
        ));
        this.gui.addElement(new StaticGuiElement(BUTTON_CHAR, applyPlaceholders(config.getButton()), click -> {
            onBreakDownClick();
            return true;
        }));
        this.gui.addElement(new StaticGuiElement(INFO_CHAR, applyPlaceholders(config.getInfo()), click -> true));

        this.gui.setCloseAction(close -> {
            OPEN_GUIS.remove(player.getUniqueId(), this);
            returnStorageItem();
            return true;
        });
    }

    public static void open(Player player) {
        BreakDownGuiConfig config = MagicGemTiny.getInstance().getBreakDownGuiConfig();
        if (config == null) {
            player.sendMessage("§c分解台配置未加载, 请联系管理员");
            return;
        }
        BreakDownGui instance = new BreakDownGui(player, config);
        OPEN_GUIS.put(player.getUniqueId(), instance);
        instance.gui.show(player);
    }

    public static void closeAll() {
        for (BreakDownGui instance : new ArrayList<>(OPEN_GUIS.values())) {
            try {
                instance.returnStorageItem();
                instance.gui.close();
            } catch (Exception ignored) {
            }
        }
        OPEN_GUIS.clear();
    }

    private boolean validateInput(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }
        if (item.getAmount() != 1) {
            player.sendMessage("§c分解台一次只能放入1颗宝石");
            return false;
        }
        Gem gem = getGem(item);
        if (gem == null || gem.getBreakDown() == null || gem.getBreakDown().isEmpty()) {
            player.sendMessage(PlaceholderApiHook.setPlaceholders(player, config.getInvalidGemTip()));
            return false;
        }
        return true;
    }

    private void onBreakDownClick() {
        ItemStack item = storage.getItem(0);
        Gem gem = getGem(item);
        if (item == null || item.getType().isAir() || item.getAmount() != 1 || gem == null || gem.getBreakDown() == null || gem.getBreakDown().isEmpty()) {
            player.sendMessage(PlaceholderApiHook.setPlaceholders(player, config.getInvalidGemTip()));
            return;
        }

        for (String line : gem.getBreakDown()) {
            CommandExecuteUtil.executeBreakDownCommand(player, line);
        }
        storage.setItem(0, null);
        gui.playClickSound();
        player.sendMessage(PlaceholderApiHook.setPlaceholders(player, config.getSuccessTip()));
        scheduleRefresh();
    }

    /**
     * 克隆配置中的 GUI 物品并为当前玩家解析其中的 PAPI 变量,
     * 避免直接修改配置内共享实例导致变量被固定为首次解析结果。
     */
    private ItemStack applyPlaceholders(ItemStack source) {
        ItemStack item = source.clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }
        if (meta.hasDisplayName()) {
            meta.setDisplayName(PlaceholderApiHook.setPlaceholders(player, meta.getDisplayName()));
        }
        if (meta.hasLore()) {
            meta.setLore(PlaceholderApiHook.setPlaceholders(player, meta.getLore()));
        }
        item.setItemMeta(meta);
        return item;
    }

    private Gem getGem(ItemStack item) {
        if (!ItemUtil.isGem(item)) {
            return null;
        }
        NBTItem nbtItem = new NBTItem(item);
        String gemUniqueKey = nbtItem.getString(NBTKey.MAGICGEM_NAME);
        if (StringUtil.isBlank(gemUniqueKey)) {
            return null;
        }
        return MagicGemTiny.getInstance().getGemLoader().getGemMap().get(gemUniqueKey);
    }

    private void scheduleRefresh() {
        Bukkit.getScheduler().runTask(MagicGemTiny.getInstance(), () -> gui.draw(player));
    }

    private void returnStorageItem() {
        ItemStack item = storage.getItem(0);
        if (item != null && !item.getType().isAir()) {
            storage.setItem(0, null);
            ItemUtil.giveOrDropItem(player, item);
        }
    }

}
