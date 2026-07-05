package com.illtamer.plugin.magicgemtiny.gui;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.config.DisassembleGuiConfig;
import com.illtamer.plugin.magicgemtiny.util.GemUtil;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiElementGroup;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
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
 * 宝石拆卸台 GUI
 * @apiNote 每个玩家打开时创建独立实例。玩家将镶嵌过可拆卸宝石的装备放入装备槽,
 *      下方自动列出所有可拆卸宝石, 点击即可拆卸并退还宝石实物。
 * */
public class DisassembleGui {

    // 装备槽字符
    private static final char EQUIP_CHAR = 'E';
    // 宝石展示区字符
    private static final char GEM_CHAR = 'G';
    // 说明图标字符
    private static final char INFO_CHAR = 'I';
    // 翻页字符
    private static final char PREV_CHAR = '<';
    private static final char NEXT_CHAR = '>';

    // 追踪各玩家当前打开的拆卸台, 用于插件卸载时归还装备, 防止物品丢失
    private static final Map<UUID, DisassembleGui> OPEN_GUIS = new ConcurrentHashMap<>();

    private final Player player;
    private final DisassembleGuiConfig config;
    private final InventoryGui gui;
    // 装备槽存储 (单格)
    private final Inventory equipStorage;
    private final GuiElementGroup gemGroup;
    // 防止刷新重入
    private boolean refreshing = false;

    private DisassembleGui(Player player, DisassembleGuiConfig config) {
        this.player = player;
        this.config = config;
        this.equipStorage = Bukkit.createInventory(null, 9, "MagicGemDisassembleEquip");

        MagicGemTiny plugin = MagicGemTiny.getInstance();
        this.gui = new InventoryGui(plugin, config.getTitle(), config.getRows());
        this.gui.setFiller(config.getFiller() != null ? new ItemStack(config.getFiller()) : new ItemStack(Material.AIR));

        // 装备槽: 单格存储 (invSlot = 0), 放入/取出后刷新宝石区
        GuiStorageElement equipElement = new GuiStorageElement(
                EQUIP_CHAR, equipStorage, 0,
                this::onEquipChanged,
                info -> validatePlace(info.getItem()), // placeValidator
                info -> true                            // takeValidator: 允许取回装备
        );
        this.gui.addElement(equipElement);

        // 宝石展示区
        this.gemGroup = new GuiElementGroup(GEM_CHAR);
        this.gui.addElement(this.gemGroup);

        // 翻页
        this.gui.addElement(new GuiPageElement(PREV_CHAR, cloneName(config.getPrev()),
                GuiPageElement.PageAction.PREVIOUS, config.getPrev().getItemMeta() != null
                ? config.getPrev().getItemMeta().getDisplayName() : "上一页"));
        this.gui.addElement(new GuiPageElement(NEXT_CHAR, cloneName(config.getNext()),
                GuiPageElement.PageAction.NEXT, config.getNext().getItemMeta() != null
                ? config.getNext().getItemMeta().getDisplayName() : "下一页"));

        // 说明图标
        this.gui.addElement(new StaticGuiElement(INFO_CHAR, config.getInfo(), click -> true));

        // 关闭时归还装备槽内的物品
        this.gui.setCloseAction(close -> {
            OPEN_GUIS.remove(player.getUniqueId(), this);
            returnEquipItem();
            return true;
        });

        // 首次填充 (show() 会触发首次绘制, 此处无需 draw)
        fillGemGroup();
    }

    /**
     * 打开拆卸台
     * */
    public static void open(Player player) {
        DisassembleGuiConfig config = MagicGemTiny.getInstance().getDisassembleGuiConfig();
        if (config == null) {
            player.sendMessage("§c拆卸台配置未加载, 请联系管理员");
            return;
        }
        DisassembleGui instance = new DisassembleGui(player, config);
        OPEN_GUIS.put(player.getUniqueId(), instance);
        instance.gui.show(player);
    }

    /**
     * 插件卸载时归还所有打开的拆卸台内的装备并关闭, 防止物品丢失
     * */
    public static void closeAll() {
        for (DisassembleGui instance : new ArrayList<>(OPEN_GUIS.values())) {
            try {
                instance.returnEquipItem();
                instance.gui.close();
            } catch (Exception ignored) {
            }
        }
        OPEN_GUIS.clear();
    }

    /**
     * 校验放入装备槽的物品: 必须是非宝石, 且含有可拆卸的宝石记录
     * */
    private boolean validatePlace(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return true; // 允许清空
        }
        if (ItemUtil.isGem(item)) {
            player.sendMessage("§c宝石本身不能放入拆卸台");
            return false;
        }
        if (GemUtil.collectRemovableRecords(item).isEmpty()) {
            player.sendMessage("§c该物品没有可拆卸的宝石");
            return false;
        }
        return true;
    }

    /**
     * 装备槽物品变动回调 (放入/取出)
     * */
    private void onEquipChanged() {
        scheduleRefresh();
    }

    /**
     * 延迟到下一 tick 刷新, 避免在点击/物品移动事件处理过程中重入 draw
     * */
    private void scheduleRefresh() {
        Bukkit.getScheduler().runTask(MagicGemTiny.getInstance(), this::rebuildGemGroup);
    }

    /**
     * 根据装备槽当前物品重建宝石展示区并重绘
     * */
    private void rebuildGemGroup() {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            fillGemGroup();
            gui.draw(player);
        } finally {
            refreshing = false;
        }
    }

    /**
     * 仅填充宝石展示区元素 (不触发重绘, 供构造时首次填充)
     * */
    private void fillGemGroup() {
        gemGroup.clearElements();
        ItemStack equip = equipStorage.getItem(0);
        List<GemUtil.RemovableRecord> records = GemUtil.collectRemovableRecords(equip);
        for (GemUtil.RemovableRecord record : records) {
            gemGroup.addElement(buildGemIcon(record));
        }
        // 展示区为空时的占位提示
        if (records.isEmpty()) {
            gemGroup.setFiller(config.getEquipHint());
        } else {
            gemGroup.setFiller(new ItemStack(Material.AIR));
        }
    }

    /**
     * 构建一颗可拆卸宝石的图标
     * */
    private GuiElement buildGemIcon(GemUtil.RemovableRecord record) {
        final int logIndex = record.logIndex;
        final String gemName = record.gem.getName();
        ItemStack icon = record.gem.getDisplayItem().clone();
        icon.setAmount(1);
        appendClickTip(icon);
        // group 内元素的 slotChar 不参与主布局解析, 用固定占位字符即可
        return new StaticGuiElement('g', icon, click -> {
            onGemClick(logIndex, gemName);
            return true;
        });
    }

    /**
     * 点击某颗宝石执行拆卸
     * */
    private void onGemClick(int logIndex, String gemName) {
        ItemStack equip = equipStorage.getItem(0);
        if (equip == null || equip.getType().isAir()) {
            scheduleRefresh();
            return;
        }
        ItemStack restored = GemUtil.disassembleGem(equip, logIndex, gemName, player);
        if (restored != null) {
            // 写回还原后的装备
            equipStorage.setItem(0, restored);
            gui.playClickSound();
        }
        scheduleRefresh();
    }

    /**
     * 在图标 Lore 末尾追加点击提示
     * */
    private void appendClickTip(ItemStack icon) {
        String tip = config.getClickTip();
        if (StringUtil.isBlank(tip)) {
            return;
        }
        ItemMeta meta = icon.getItemMeta();
        if (meta == null) {
            return;
        }
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(tip);
        meta.setLore(lore);
        icon.setItemMeta(meta);
    }

    /**
     * 归还装备槽内的物品给玩家
     * */
    private void returnEquipItem() {
        ItemStack equip = equipStorage.getItem(0);
        if (equip != null && !equip.getType().isAir()) {
            equipStorage.setItem(0, null);
            ItemUtil.giveOrDropItem(player, equip);
        }
    }

    /**
     * 克隆图标并保留显示名 (用于翻页按钮)
     * */
    private ItemStack cloneName(ItemStack source) {
        return source != null ? source.clone() : new ItemStack(Material.ARROW);
    }

}
