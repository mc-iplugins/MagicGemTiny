package com.illtamer.plugin.magicgemtiny.hook;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

public class VaultHook {

    private static final Economy economy = Optional.ofNullable(Bukkit.getServer().getServicesManager()
            .getRegistration(Economy.class)).map(RegisteredServiceProvider::getProvider).orElse(null);

    public static boolean give(double count, OfflinePlayer player) {
        EconomyResponse response = economy.depositPlayer(player, count);
        return response.transactionSuccess();
    }

    public static boolean take(double count, Player player) {
        EconomyResponse response = economy.withdrawPlayer(player, count);
        return response.transactionSuccess();
    }

    public static boolean check(double count, Player player) {
        return economy.has(player, count);
    }

}