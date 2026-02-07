package com.illtamer.plugin.magicgemtiny.listener;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.gem.ItemGem;
import com.illtamer.plugin.magicgemtiny.gem.RandomGem;
import com.illtamer.plugin.magicgemtiny.util.GemUtil;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * 镶嵌监听
 * */
public class ItemGemListener implements Listener {

    private final GemLoader gemLoader = MagicGemTiny.getInstance().getGemLoader();

    // 仅支持在工作台触发
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEmbed(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        InventoryType type = event.getInventory().getType();
        if (type != InventoryType.WORKBENCH) {
            return;
        }

        ItemStack gemItem = event.getCursor();
        ItemStack clickedItem = event.getCurrentItem();
        if (gemItem == null || gemItem.getType() == Material.AIR) return;
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        ItemStack newClickedItem = doOnEmbed(gemItem, clickedItem, player, () -> event.setCancelled(true));
        if (newClickedItem != null) {
            event.setCurrentItem(newClickedItem);
        }
    }

    @Nullable
    private ItemStack doOnEmbed(ItemStack gemItem, ItemStack clickedItem, Player player, Runnable cancel) {
        NBTItem gemItemNBT = new NBTItem(gemItem);
        NBTItem clickedItemNBT = new NBTItem(clickedItem);
        String gemUniqueKey = gemItemNBT.getString(NBTKey.MAGICGEM_NAME);
        if (StringUtil.isBlank(gemUniqueKey)) {
            return null;
        }
        cancel.run();
        Gem gem = gemLoader.getGemMap().get(gemUniqueKey);
        if (gem == null) {
            player.sendMessage("无效的宝石: " + gemUniqueKey);
            return null;
        }

        if (gem instanceof ItemGem itemGem) {
            GemUtil.triggerItemRewardsOnCondition(gemItem, itemGem, clickedItemNBT, player);
            return clickedItemNBT.getItem();
        } else if ((gem instanceof RandomGem randomGem) && randomGem.getRandomType() == Gem.Type.ITEM) {
            boolean give = randomGem.isGive();
            if (give) { // 右键随机给予物品宝石 PlayerGemListener
                return null;
            }

            Gem newRandomGem = randomGem.randomGem();
            if (newRandomGem == null) {
                player.sendMessage("§c随机物品宝石奖励为空，请联系管理员核实问题");
                return null;
            }
            ItemUtil.consume(gemItem);
            return doOnEmbed(newRandomGem.getItem(), clickedItem, player, cancel);
        }
        // 随机玩家、玩家宝石
        return null;
    }

}
