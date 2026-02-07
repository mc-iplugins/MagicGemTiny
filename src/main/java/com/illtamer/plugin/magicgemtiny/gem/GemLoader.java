package com.illtamer.plugin.magicgemtiny.gem;

import com.illtamer.lib.config.ConfigFile;
import com.illtamer.plugin.magicgemtiny.entity.DynamicValue;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.reward.PlayerReward;
import com.illtamer.plugin.magicgemtiny.reward.Reward;
import com.illtamer.plugin.magicgemtiny.util.ItemUtil;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GemLoader {

    private final Logger logger = MagicGemTiny.getInstance().getLogger();
    private List<ConfigFile> gemConfigFiles;
    // name, gem
    private @Getter Map<String, Gem> gemMap;

    public void reload() {
        gemMap = null;
        load();
    }

    public void load() {
        MagicGemTiny instance = MagicGemTiny.getInstance();
        File gemsFolder = new File(instance.getDataFolder(), "gems");
        initGemsFolder(gemsFolder, instance);
        this.gemConfigFiles = Optional.ofNullable(gemsFolder.listFiles())
                .map(Arrays::asList).orElse(Collections.emptyList()).stream()
                .map(File::getName)
                .filter(e -> e.endsWith(".yml") || e.endsWith(".yaml"))
                .map(e -> new ConfigFile(gemsFolder, e, instance))
                .collect(Collectors.toList());
        Map<String, Gem> gemMap = new HashMap<>();
        for (ConfigFile gemConfigFile : gemConfigFiles) {
            gemMap.putAll(loadGemFromConfig(gemConfigFile.getConfig()));
        }
        // 所有宝石加载完成后再加载随机宝石
        loadRandomGem(gemMap);
        this.gemMap = gemMap;
        logger.info("加载 " + gemConfigFiles.size() + " 个宝石配置文件，共 " + gemMap.size() + " 宝石");
    }

    private Map<String, Gem> loadGemFromConfig(FileConfiguration config) {
        Map<String, Gem> gemMap = new HashMap<>();
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String gemUniqueName = section.getString("Name");
            if (StringUtil.isBlank(gemUniqueName)) {
                logger.warning("宝石配置项 " + key + " 未配置 .Name");
                continue;
            }
            if (gemMap.containsKey(key) || (this.gemMap != null && this.gemMap.containsKey(key))) {
                logger.warning("发现重复的宝石 " + gemUniqueName);
                continue;
            }
            String typeStr = section.getString("Type", Gem.Type.ITEM.getName());
            Gem.Type type = Gem.Type.parse(typeStr);
            if (type == null) {
                logger.warning("未知的宝石类型 " + typeStr);
                continue;
            }
            DynamicValue success = new DynamicValue(section.getString("Success", "100"));
            String successTip = section.getString("SuccessTip", "none");
            successTip = "none".equalsIgnoreCase(successTip) ? null : successTip;
            List<Reward> rewards = Reward.build(section.getStringList("Rewards"));

            // 通用外观
            String texture = section.getString("Texture");
            String materialStr = Optional.ofNullable(section.getString("Material")).orElse("");
            Material material = Material.getMaterial(materialStr);
            if (material == null && StringUtil.isBlank(texture)) {
                logger.warning("未知的材质类型 " + materialStr);
                continue;
            }
            String display = section.getString("Display");
            List<String> tips = section.getStringList("Tips");
            int customModelData = section.getInt("CustomModelData", 0);
            ItemStack displayItem = ItemUtil.buildGem(gemUniqueName, material, texture, display, tips, customModelData);
            if (material == Material.POTION) {
                Color color = ItemUtil.parsePotionColorFromRGB(section.getString("Color"));
                PotionMeta meta = (PotionMeta) displayItem.getItemMeta();
                meta.setBasePotionType(PotionType.WATER);
                if (color != null) {
                    meta.setColor(color);
                }
                displayItem.setItemMeta(meta);
            }

            Gem gem;
            if (type == Gem.Type.PLAYER) {
                if (rewards.stream().anyMatch(e -> !(e instanceof PlayerReward))) {
                    logger.warning("玩家宝石 " + gemUniqueName + " 中错误的配置了其他类型奖励");
                    continue;
                }

                PlayerGem playerGem = new PlayerGem(gemUniqueName, displayItem, success, successTip, rewards);
                playerGem.setEat(section.getBoolean("Eat", false));
                playerGem.setShift(section.getBoolean("Shift", false));
                gemMap.put(gemUniqueName, playerGem);
                checkSupportKeys(gem = playerGem, section.getKeys(false));
            } else if (type == Gem.Type.RANDOM) {
                RandomGem randomGem = new RandomGem(gemUniqueName, displayItem, success, successTip, rewards);
                randomGem.setGemsSection(section.getConfigurationSection("Gems"));
                randomGem.setGive(section.getBoolean("Give", false));
                gemMap.put(gemUniqueName, randomGem);
                checkSupportKeys(gem = randomGem, section.getKeys(false));
            } else { // Item
                // 物品宝石同时支持玩家奖励和物品奖励
//                if (rewards.stream().anyMatch(e -> !(e instanceof ItemReward))) {
//                    logger.warning("物品宝石 " + gemUniqueName + " 中错误的配置了其他类型奖励");
//                    continue;
//                }
                DynamicValue downgrade = new DynamicValue(section.getString("Downgrade", "0"));
                DynamicValue breakValue = new DynamicValue(section.getString("Break", "0"));
                ItemGem itemGem = new ItemGem(gemUniqueName, displayItem, success, downgrade, breakValue,
                        successTip, rewards, section.getStringList("Require"));
                String guiStr = section.getString("Gui", "workbench");
                ItemGem.Gui gui = ItemGem.Gui.parse(guiStr);
                if (gui == null) {
                    logger.warning("暂不支持物品宝石在 " + guiStr + " 类型GUI上使用");
                    continue;
                }
                itemGem.setGui(gui);
                itemGem.setEmbed(section.getInt("Embed", 0));
                itemGem.setDowngradeTip(section.getString("DowngradeTip"));
                itemGem.setRemoveTip(section.getString("RemoveTip"));
                itemGem.setBreakTip(section.getString("BreakTip"));
                gemMap.put(gemUniqueName, itemGem);
                checkSupportKeys(gem = itemGem, section.getKeys(false));
            }

            gem.setFailTip(section.getString("FailTip"));
        }
        return gemMap;
    }

    // 暂时不考虑随机宝石套随机宝石的场景
    private void loadRandomGem(Map<String, Gem> globalGemMap) {
        for (Gem gem : globalGemMap.values()) {
            if (!(gem instanceof RandomGem randomGem)) {
                continue;
            }
            Map<Gem, Integer> gemsWeightMap = new HashMap<>();
            ConfigurationSection section = randomGem.getGemsSection();
            Gem.Type type = null;
            boolean sameType = true;
            for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                String gemUniqueName = entry.getKey();
                int weight = (int) entry.getValue();
                Gem rewardGem = globalGemMap.get(gemUniqueName);
                if (rewardGem == null) {
                    logger.warning("随机宝石 " + gem.getName() + " 中配置的宝石项不存在: " + gemUniqueName);
                    continue;
                }
                if (type == null) {
                    type = rewardGem.getType();
                } else {
                    if (rewardGem.getType() != type) {
                        sameType = false;
                        break;
                    }
                }
                gemsWeightMap.put(rewardGem, weight);
            }

            if (!sameType) {
                logger.warning("随机宝石 " + gem.getName() + " 中配置的宝石项类型不唯一，已跳过加载");
                continue;
            }

            randomGem.setRandomType(type);
            randomGem.setGemsWeightMap(gemsWeightMap);
        }
    }

    // 检查当前版本宝石支持的配置项并告警
    private void checkSupportKeys(Gem gem, Set<String> configKeys) {
        Set<String> cloneSet = new HashSet<>(configKeys);
        Gem.Type type = gem.getType();
        if (type == Gem.Type.PLAYER) {
            cloneSet.removeAll(PlayerGem.supportKeys());
        } else if (type == Gem.Type.RANDOM) {
            cloneSet.removeAll(RandomGem.supportKeys());
        } else { // Item
            cloneSet.removeAll(ItemGem.supportKeys());
        }
        if (!cloneSet.isEmpty()) {
            if (type == null) {
                System.out.println("!!! 空type在" + gem.getName());
            }
            logger.warning(type.getDescription() + " " + gem.getName() + " 中存在不支持的配置项: " + cloneSet);
        }
    }

    private void initGemsFolder(File gemsFolder, Plugin plugin) {
        if (!gemsFolder.exists()) {
            logger.info("检测到插件目录不存在，正在初始化宝石配置");
            gemsFolder.mkdirs();

            for (String gemConfigFileName : Arrays.asList(
                    "FunctionGem.yml",
                    "MythicGem.yml",
                    "NewCraftEnchantGem.yml",
                    "NewInfinitePrizeGem.yml",
                    "PlayerGem.yml",
                    "PotionGem.yml",
                    "SuccessMultipleGem.yml"
            )) {
                try (InputStream in = plugin.getResource("gems/" + gemConfigFileName)) {
                    if (in == null) {
                        logger.warning("未找到宝石配置: gems/" + gemConfigFileName);
                        continue;
                    }
                    Files.copy(in, new File(gemsFolder, gemConfigFileName).toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    logger.warning(("保存资源失败: gems/" + gemConfigFileName));
                    e.printStackTrace();
                }
            }
        }
    }

}
