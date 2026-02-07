package com.illtamer.plugin.magicgemtiny.listener;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.gem.PlayerGem;
import com.illtamer.plugin.magicgemtiny.gem.RandomGem;
import com.illtamer.plugin.magicgemtiny.util.GemUtil;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * 触发
 * */
public class PlayerGemListener implements Listener {

    private final GemLoader gemLoader = MagicGemTiny.getInstance().getGemLoader();

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        Player player = event.getPlayer();
        // 双持同时触发是正常现象
        if (event.getHand() == EquipmentSlot.HAND) {
            doOnUseOrEat(player.getInventory().getItemInMainHand(), false, player);
        } else if (event.getHand() == EquipmentSlot.OFF_HAND) {
            doOnUseOrEat(player.getInventory().getItemInOffHand(), false, player);
        }
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        doOnUseOrEat(event.getItem(), true, event.getPlayer());
    }

    private void doOnUseOrEat(ItemStack item, boolean eating, Player player) {
        if (!item.hasItemMeta() || item.getType().isAir()) {
            return;
        }

        NBTItem nbtItem = new NBTItem(item);
        String gemUniqueKey = nbtItem.getString(NBTKey.MAGICGEM_NAME);
        if (StringUtil.isBlank(gemUniqueKey)) {
            return;
        }

        Gem gem = gemLoader.getGemMap().get(gemUniqueKey);
        if (gem == null) {
            player.sendMessage("无效的宝石: " + gemUniqueKey);
            return;
        }

        if (gem instanceof PlayerGem playerGem) {
            boolean needEat = playerGem.isEat();
            boolean needShift = playerGem.isShift();
            if ((needEat && !eating) || (needShift && !player.isSneaking())) {
                return;
            }

            GemUtil.triggerPlayerRewardsOnCondition(item, playerGem, player);
        } else if ((gem instanceof RandomGem randomGem)) {
            boolean give = randomGem.isGive();
            Gem newRandomGem = randomGem.randomGem();
            if (newRandomGem == null) {
                player.sendMessage("§c随机宝石奖励为空，请联系管理员核实问题");
                return;
            }
            ItemStack gemItem = newRandomGem.getItem();
            ItemUtil.consume(item);
            if (give) { // 直接给予玩家 / 物品宝石
                ItemUtil.giveOrDropItem(player, gemItem);
            } else if (randomGem.getRandomType() == Gem.Type.PLAYER) {
                doOnUseOrEat(gemItem, eating, player);
            }
        }
    }

}
