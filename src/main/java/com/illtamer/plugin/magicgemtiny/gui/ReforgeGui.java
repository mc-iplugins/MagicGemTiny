package com.illtamer.plugin.magicgemtiny.gui;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.config.ReforgeGuiConfig;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
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
 * 宝石重铸 GUI。
 */
public class ReforgeGui {

    private static final char INPUT_ONE_CHAR = '1';
    private static final char INPUT_TWO_CHAR = '2';
    private static final char INPUT_THREE_CHAR = '3';
    private static final char OUTPUT_CHAR = 'O';
    private static final char BUTTON_CHAR = 'R';
    private static final char INFO_CHAR = 'I';

    private static final Map<UUID, ReforgeGui> OPEN_GUIS = new ConcurrentHashMap<>();

    private final Player player;
    private final ReforgeGuiConfig config;
    private final InventoryGui gui;
    private final Inventory storage;

    private ReforgeGui(Player player, ReforgeGuiConfig config) {
        this.player = player;
        this.config = config;
        this.storage = Bukkit.createInventory(null, 9, "MagicGemReforgeStorage");

        MagicGemTiny plugin = MagicGemTiny.getInstance();
        this.gui = new InventoryGui(plugin, config.getTitle(), config.getRows());
        this.gui.setFiller(config.getFiller() != null ? new ItemStack(config.getFiller()) : new ItemStack(Material.AIR));

        this.gui.addElement(buildInputElement(INPUT_ONE_CHAR, 0));
        this.gui.addElement(buildInputElement(INPUT_TWO_CHAR, 1));
        this.gui.addElement(buildInputElement(INPUT_THREE_CHAR, 2));
        this.gui.addElement(new GuiStorageElement(
                OUTPUT_CHAR, storage, 3,
                this::scheduleRefresh,
                info -> info.getItem() == null || info.getItem().getType().isAir(),
                info -> true
        ));
        this.gui.addElement(new StaticGuiElement(BUTTON_CHAR, config.getButton(), click -> {
            onReforgeClick();
            return true;
        }));
        this.gui.addElement(new StaticGuiElement(INFO_CHAR, config.getInfo(), click -> true));

        this.gui.setCloseAction(close -> {
            OPEN_GUIS.remove(player.getUniqueId(), this);
            returnStorageItems();
            return true;
        });
    }

    public static void open(Player player) {
        MagicGemTiny plugin = MagicGemTiny.getInstance();
        ReforgeGuiConfig config = plugin.getReforgeGuiConfig();
        if (config == null || plugin.getPluginConfig() == null) {
            player.sendMessage("§c重铸台配置未加载, 请联系管理员");
            return;
        }
        ReforgeGui instance = new ReforgeGui(player, config);
        OPEN_GUIS.put(player.getUniqueId(), instance);
        instance.gui.show(player);
    }

    public static void closeAll() {
        for (ReforgeGui instance : new ArrayList<>(OPEN_GUIS.values())) {
            try {
                instance.returnStorageItems();
                instance.gui.close();
            } catch (Exception ignored) {
            }
        }
        OPEN_GUIS.clear();
    }

    private GuiStorageElement buildInputElement(char slotChar, int storageSlot) {
        return new GuiStorageElement(
                slotChar, storage, storageSlot,
                this::scheduleRefresh,
                info -> validateInput(info.getItem()),
                info -> true
        );
    }

    private boolean validateInput(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true;
        }
        if (item.getAmount() != 1) {
            player.sendMessage("§c每个重铸槽只能放入1颗宝石");
            return false;
        }
        Gem gem = getGem(item);
        if (gem == null) {
            player.sendMessage("§c只能放入有效宝石");
            return false;
        }
        if (StringUtil.isBlank(gem.getReforge())) {
            player.sendMessage("§c该宝石未配置Reforge, 不能重铸");
            return false;
        }
        return true;
    }

    private void onReforgeClick() {
        ItemStack output = storage.getItem(3);
        if (output != null && !output.getType().isAir()) {
            player.sendMessage(config.getOutputNotEmptyTip());
            return;
        }

        List<Gem> inputGems = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ItemStack item = storage.getItem(i);
            Gem gem = getGem(item);
            if (item == null || item.getType().isAir() || item.getAmount() != 1 || gem == null || StringUtil.isBlank(gem.getReforge())) {
                player.sendMessage(config.getNeedThreeTip());
                return;
            }
            inputGems.add(gem);
        }

        String reforge = inputGems.get(0).getReforge();
        boolean sameReforge = inputGems.stream().allMatch(gem -> reforge.equals(gem.getReforge()));
        if (!sameReforge) {
            player.sendMessage(config.getSameReforgeTip());
            return;
        }

        MagicGemTiny plugin = MagicGemTiny.getInstance();
        if (plugin.getPluginConfig() == null) {
            player.sendMessage("§c重铸配置未加载, 请联系管理员");
            return;
        }
        Gem resultGem = plugin.getPluginConfig().randomReforgeGem(reforge);
        if (resultGem == null) {
            player.sendMessage(config.getNoPoolTip());
            return;
        }

        ItemStack resultItem = resultGem.getItem(1);
        for (int i = 0; i < 3; i++) {
            storage.setItem(i, null);
        }
        storage.setItem(3, resultItem);
        gui.playClickSound();
        sendSuccessRewardTip(resultItem, resultGem);
        scheduleRefresh();
    }

    private void sendSuccessRewardTip(ItemStack resultItem, Gem resultGem) {
        String tip = config.getSuccessRewardTip();
        if (StringUtil.isBlank(tip)) {
            return;
        }
        String displayName = resultGem.getName();
        ItemMeta meta = resultItem.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            displayName = meta.getDisplayName();
        }
        player.sendMessage(tip.replace("{gem}", displayName));
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

    private void returnStorageItems() {
        for (int i = 0; i < 4; i++) {
            ItemStack item = storage.getItem(i);
            if (item != null && !item.getType().isAir()) {
                storage.setItem(i, null);
                ItemUtil.giveOrDropItem(player, item);
            }
        }
    }

}
