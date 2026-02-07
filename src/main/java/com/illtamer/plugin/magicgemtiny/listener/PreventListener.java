package com.illtamer.plugin.magicgemtiny.listener;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.entity.NBTKey;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.gem.PlayerGem;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import static com.illtamer.plugin.magicgemtiny.util.ItemUtil.isGem;

/**
 * 插件会阻止用户
 * - 将宝石放在地上
 * - 右键触发原版使用
 * - 用作原版合成材料
 * - 用作燃料
 * */
public class PreventListener implements Listener {

    private final GemLoader gemLoader = MagicGemTiny.getInstance().getGemLoader();

    /**
     * 1. 阻止将宝石当作方块放置
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isGem(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    /**
     * 2. 阻止右键触发原版功能 (如：吃掉、右键点击地面效果等)
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta() || item.getType().isAir()) {
            return;
        }

        NBTItem nbtItem = new NBTItem(item);
        String gemUniqueKey = nbtItem.getString(NBTKey.MAGICGEM_NAME);
        if (StringUtil.isBlank(gemUniqueKey)) {
            return;
        }

        Gem gem = gemLoader.getGemMap().get(gemUniqueKey);
        if (gem instanceof PlayerGem playerGem && playerGem.isEat()) {
            return;
        }
        event.setCancelled(true);
    }

    /**
     * 3. 阻止用作原版合成材料
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack[] matrix = event.getInventory().getMatrix();
        for (ItemStack item : matrix) {
            if (isGem(item)) {
                // 如果合成槽中有宝石，直接将结果设为 null (空气)
                event.getInventory().setResult(null);
                break;
            }
        }
    }

    /**
     * 4. 阻止用作燃料
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        if (isGem(event.getFuel())) {
            event.setCancelled(true);
        }
    }

}
