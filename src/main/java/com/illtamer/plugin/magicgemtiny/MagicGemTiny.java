package com.illtamer.plugin.magicgemtiny;

import com.illtamer.plugin.magicgemtiny.command.CommandHandler;
import com.illtamer.plugin.magicgemtiny.config.BoundBlockManager;
import com.illtamer.plugin.magicgemtiny.config.BreakDownGuiConfig;
import com.illtamer.plugin.magicgemtiny.config.DisassembleGuiConfig;
import com.illtamer.plugin.magicgemtiny.config.PluginConfig;
import com.illtamer.plugin.magicgemtiny.config.ReforgeGuiConfig;
import com.illtamer.plugin.magicgemtiny.gem.GemLoader;
import com.illtamer.plugin.magicgemtiny.gui.BreakDownGui;
import com.illtamer.plugin.magicgemtiny.gui.DisassembleGui;
import com.illtamer.plugin.magicgemtiny.gui.ReforgeGui;
import com.illtamer.plugin.magicgemtiny.listener.BoundBlockListener;
import com.illtamer.plugin.magicgemtiny.listener.ItemGemListener;
import com.illtamer.plugin.magicgemtiny.listener.PlayerGemListener;
import com.illtamer.plugin.magicgemtiny.listener.PreventListener;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import com.illtamer.plugin.magicgemtiny.task.EquipmentPotionEffectTask;
import lombok.Getter;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class MagicGemTiny extends JavaPlugin {

    private @Getter static MagicGemTiny instance;
    private GemLoader gemLoader;
    private PluginConfig pluginConfig;
    private DisassembleGuiConfig disassembleGuiConfig;
    private ReforgeGuiConfig reforgeGuiConfig;
    private BreakDownGuiConfig breakDownGuiConfig;
    private BoundBlockManager boundBlockManager;

    @Override
    public void onEnable() {
        instance = this;
        Reward.registerAll();
        (gemLoader = new GemLoader()).load();

        try {
            pluginConfig = new PluginConfig();
        } catch (Exception e) {
            getLogger().warning("主配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            disassembleGuiConfig = new DisassembleGuiConfig();
        } catch (Exception e) {
            getLogger().warning("拆卸台配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            reforgeGuiConfig = new ReforgeGuiConfig();
        } catch (Exception e) {
            getLogger().warning("重铸台配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            breakDownGuiConfig = new BreakDownGuiConfig();
        } catch (Exception e) {
            getLogger().warning("分解台配置加载失败: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            boundBlockManager = new BoundBlockManager();
        } catch (Exception e) {
            getLogger().warning("方块绑定配置加载失败: " + e.getMessage());
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
        manager.registerEvents(new BoundBlockListener(), this);

        EquipmentPotionEffectTask.start();
    }

    @Override
    public void onDisable() {
        EquipmentPotionEffectTask.stop();
        // 归还所有打开的 GUI 内的物品, 防止物品丢失
        DisassembleGui.closeAll();
        ReforgeGui.closeAll();
        BreakDownGui.closeAll();
        if (boundBlockManager != null) {
            boundBlockManager.save();
        }
        instance = null;
    }

}
