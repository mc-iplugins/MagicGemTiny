package com.illtamer.plugin.magicgemtiny.listener;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.config.BoundBlockManager;
import com.illtamer.plugin.magicgemtiny.util.CommandExecuteUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * 特殊方块点击监听。
 */
public class BoundBlockListener implements Listener {

    private final BoundBlockManager boundBlockManager = MagicGemTiny.getInstance().getBoundBlockManager();

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK && action != Action.LEFT_CLICK_BLOCK) {
            return;
        }
        if (event.getClickedBlock() == null) {
            return;
        }

        if (boundBlockManager == null) {
            return;
        }
        String command = boundBlockManager.getCommand(event.getClickedBlock());
        if (command == null) {
            return;
        }
        event.setCancelled(true);
        CommandExecuteUtil.executeAsPlayer(event.getPlayer(), command);
    }

}
