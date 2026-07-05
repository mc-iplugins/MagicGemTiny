package com.illtamer.plugin.magicgemtiny;

import com.illtamer.plugin.magicgemtiny.command.CommandHandler;
import com.illtamer.plugin.magicgemtiny.config.DisassembleGuiConfig;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.gui.DisassembleGui;
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
    private DisassembleGuiConfig disassembleGuiConfig;

    @Override
    public void onEnable() {
        instance = this;
        Reward.registerAll();
        (gemLoader = new GemLoader()).load();

        try {
            disassembleGuiConfig = new DisassembleGuiConfig();
        } catch (Exception e) {
            getLogger().warning("拆卸台配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }

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
        // 归还所有打开的拆卸台内的装备, 防止物品丢失
        DisassembleGui.closeAll();
        instance = null;
    }

}
