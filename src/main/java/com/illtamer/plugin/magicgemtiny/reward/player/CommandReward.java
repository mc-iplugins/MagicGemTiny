package com.illtamer.plugin.magicgemtiny.reward.player;

import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 指令奖励
 * */
public class CommandReward extends PlayerReward {

    @Override
    public void execute(Player player) {
        String command = getParamString("command", "c", player);
        if (command == null) {
            return;
        }

        // console为true或false，控制是否由后台执行，默认为true
        boolean console = getParamBoolean("console", player);
        // op为true或false，仅当console为false时生效。默认为false
        // - 为true将临时给予玩家op权限执行此指令
        // - 完全同步执行，并使用try-finally保证不让玩家卡op
        // - 若为false则仅仅以玩家身份执行
        boolean op = getParamBoolean("op", player);

        if (op && !console) { // op
            boolean isOp = player.isOp();
            try {
                player.setOp(isOp);
                player.performCommand(command);
            } finally {
                player.setOp(isOp);
            }
        } else if (console) { // console
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } else { // player
            player.performCommand(command);
        }
    }

}
