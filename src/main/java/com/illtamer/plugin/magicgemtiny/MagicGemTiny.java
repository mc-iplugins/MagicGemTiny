package com.illtamer.plugin.magicgemtiny;

import com.illtamer.plugin.magicgemtiny.command.CommandHandler;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.listener.ItemGemListener;
import com.illtamer.plugin.magicgemtiny.listener.PlayerGemListener;
import com.illtamer.plugin.magicgemtiny.listener.PreventListener;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MagicGemTiny extends JavaPlugin {

    private @Getter static MagicGemTiny instance;
    private GemLoader gemLoader;

    @Override
    public void onEnable() {
        instance = this;
        Reward.registerAll();
        (gemLoader = new GemLoader()).load();

        CommandHandler handler = new CommandHandler();
        PluginCommand cmd = getCommand("mgem");
        cmd.setExecutor(handler);
        cmd.setTabCompleter(handler);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new PreventListener(), this);
        manager.registerEvents(new PlayerGemListener(), this);
        manager.registerEvents(new ItemGemListener(), this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

}
