package com.illtamer.plugin.magicgemtiny.config;

import com.illtamer.lib.config.ConfigFile;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.gem.Gem;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 * 插件主配置 config.yml。
 */
public class PluginConfig {

    private static final String FILE_NAME = "config.yml";

    private final Logger logger = MagicGemTiny.getInstance().getLogger();
    private final ConfigFile configFile;
    private @Getter Map<String, List<WeightedGem>> reforgeMap = Collections.emptyMap();

    public PluginConfig() {
        this.configFile = new ConfigFile(FILE_NAME, MagicGemTiny.getInstance());
        parse();
    }

    public void reload() {
        configFile.reload();
        parse();
    }

    private void parse() {
        FileConfiguration config = configFile.getConfig();
        ConfigurationSection reforgeSection = config.getConfigurationSection("Reforge");
        if (reforgeSection == null) {
            this.reforgeMap = Collections.emptyMap();
            return;
        }

        Map<String, Gem> gemMap = MagicGemTiny.getInstance().getGemLoader().getGemMap();
        java.util.Map<String, List<WeightedGem>> result = new java.util.HashMap<>();
        for (String reforge : reforgeSection.getKeys(false)) {
            List<WeightedGem> gems = parseReforgePool(config, reforgeSection, reforge, gemMap);
            if (!gems.isEmpty()) {
                result.put(reforge, gems);
            }
        }
        this.reforgeMap = result;
        logger.info("加载 " + result.size() + " 个重铸产出池");
    }

    private List<WeightedGem> parseReforgePool(FileConfiguration config, ConfigurationSection reforgeSection,
                                               String reforge, Map<String, Gem> gemMap) {
        String path = reforgeSection.getCurrentPath() + "." + reforge;
        List<WeightedGem> result = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Object value = section.get(key);
                String gemName = key;
                int weight = 0;
                if (value instanceof Number number) {
                    weight = number.intValue();
                } else {
                    ConfigurationSection itemSection = section.getConfigurationSection(key);
                    if (itemSection != null) {
                        gemName = itemSection.getString("Name", key);
                        weight = itemSection.getInt("Weight", 0);
                    }
                }
                addWeightedGem(result, reforge, gemName, weight, gemMap);
            }
            return result;
        }

        for (Map<?, ?> map : config.getMapList(path)) {
            String gemName = Optional.ofNullable(map.get("Name")).map(String::valueOf).orElse(null);
            int weight = parseWeight(map.get("Weight"));
            addWeightedGem(result, reforge, gemName, weight, gemMap);
        }
        return result;
    }

    private int parseWeight(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private void addWeightedGem(List<WeightedGem> result, String reforge, String gemName, int weight, Map<String, Gem> gemMap) {
        if (StringUtil.isBlank(gemName)) {
            logger.warning("重铸产出池 " + reforge + " 存在空宝石名，已跳过");
            return;
        }
        Gem gem = gemMap.get(gemName);
        if (gem == null) {
            logger.warning("重铸产出池 " + reforge + " 配置了不存在的宝石: " + gemName);
            return;
        }
        if (weight <= 0) {
            logger.warning("重铸产出池 " + reforge + " 宝石 " + gemName + " 权重必须大于0，已跳过");
            return;
        }
        result.add(new WeightedGem(gem, weight));
    }

    public Gem randomReforgeGem(String reforge) {
        List<WeightedGem> gems = reforgeMap.get(reforge);
        if (gems == null || gems.isEmpty()) {
            return null;
        }
        int totalWeight = gems.stream().mapToInt(WeightedGem::weight).filter(weight -> weight > 0).sum();
        if (totalWeight <= 0) {
            return null;
        }
        int random = ThreadLocalRandom.current().nextInt(totalWeight);
        int cursor = 0;
        for (WeightedGem weightedGem : gems) {
            if (weightedGem.weight() <= 0) {
                continue;
            }
            cursor += weightedGem.weight();
            if (random < cursor) {
                return weightedGem.gem();
            }
        }
        return null;
    }

    public record WeightedGem(Gem gem, int weight) {
    }

}
