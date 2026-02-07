package com.illtamer.plugin.magicgemtiny.reward;

import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.reward.item.*;
import com.illtamer.plugin.magicgemtiny.reward.player.*;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @apiNote 奖励会严格按照顺序执行。使用正序，拆卸逆序
 * */
@ToString
@Getter
@RequiredArgsConstructor
public abstract class Reward {

    private static final Map<String, Supplier<Reward>> REWARDS_MAP = new HashMap<>();

    // {} 里配置的键值对
    protected final Map<String, String> args = new HashMap<>();

    /**
     * 初始化解析参数
     * */
    protected void init() {}

    @Nullable
    protected String getParamString(String param, Player player) {
        return getParamString(param, null, player);
    }

    @Nullable
    protected String getParamString(String param, String alias, Player player) {
        String paramValue = args.get(param);
        if (StringUtil.isNotBlank(paramValue)) {
            return PlaceholderAPI.setPlaceholders(player, paramValue.trim());
        }
        return alias != null ? PlaceholderAPI.setPlaceholders(player, args.get(alias).trim()) : null;
    }

    @Nullable
    protected Integer getParamInteger(String param, Player player) {
        return getParamInteger(param, null, player);
    }

    @Nullable
    protected Integer getParamInteger(String param, String alias, Player player) {
        String value = getParamString(param, alias, player);
        if (StringUtil.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("奖励参数项 " + param + " 数值配置错误: " + value);
        }
    }

    protected boolean getParamBoolean(String param, Player player) {
        return getParamBoolean(param, null, player);
    }

    protected boolean getParamBoolean(String param, String alias, Player player) {
        String value = getParamString(param, alias, player);
        if (StringUtil.isBlank(value)) {
            return false;
        }
        return "true".equalsIgnoreCase(value);
    }

    public static void registerAll() {
        // item
        REWARDS_MAP.put("Attribute", AttributeReward::new);
        REWARDS_MAP.put("Durability", DurabilityReward::new);
        REWARDS_MAP.put("Enchant", EnchantReward::new);
        REWARDS_MAP.put("GemSuccess", GemSuccessReward::new);
        REWARDS_MAP.put("ItemFlag", ItemFlagReward::new);
        REWARDS_MAP.put("LoreAdd", LoreAddReward::new);
        REWARDS_MAP.put("LoreReplace", LoreReplaceReward::new);
        REWARDS_MAP.put("LoreVar", LoreVarReward::new);
        REWARDS_MAP.put("Name", NameReward::new);
        REWARDS_MAP.put("NBTDouble", NBTDoubleReward::new);
        REWARDS_MAP.put("NBTString", NBTStringReward::new);
        REWARDS_MAP.put("Unbreakable", UnbreakableReward::new);
        // player
        REWARDS_MAP.put("Command", CommandReward::new);
        REWARDS_MAP.put("ExpLevel", ExpLevelReward::new);
//        REWARDS_MAP.put("ItemGive", ItemGiveReward::new);
        REWARDS_MAP.put("ItemTake", ItemTakeReward::new);
        REWARDS_MAP.put("MaxHealth", MaxHealthReward::new);
        REWARDS_MAP.put("Money", MoneyReward::new);
        REWARDS_MAP.put("Point", PointReward::new);
    }

    public static List<Reward> build(List<String> rewardList) {
        if (rewardList.isEmpty()) {
            return Collections.emptyList();
        }

        List<Reward> result = new ArrayList<>(rewardList.size());
        Logger logger = MagicGemTiny.getInstance().getLogger();

        for (String rewardStr : rewardList) {
            rewardStr = rewardStr.trim();

            int paramStart = rewardStr.indexOf("{");
            int paramEnd = rewardStr.indexOf("}");
            String typeName, paramsStr = null;
            String condition = null;
            if (paramStart != -1) { // 奖励1类型{参数1=值1;参数2=值2;..} $条件
                typeName = rewardStr.substring(0, paramStart);
                if (paramEnd == -1) {
                    logger.warning("无效的奖励表达式, 未找到参数终止符: " + rewardStr);
                    continue;
                }
                paramsStr = rewardStr.substring(paramStart + 1, paramEnd);
                condition = rewardStr.substring(paramEnd + 1).trim();
            } else { // 奖励1类型 $条件
                String[] splits = rewardStr.split(" ");
                if (splits.length > 2) {
                    logger.warning("无效的奖励表达式, 参数过多: " + rewardStr);
                    continue;
                }
                typeName = splits[0];
                condition = splits.length == 2 ? splits[1] : null;
            }

            boolean onSuccess = false, onFail = false, onRemove = false;
            if (StringUtil.isNotBlank(condition)) {
                if (!condition.startsWith("$")) {
                    logger.warning("奖励表达式触发条件配置错误, 需要以$开头: " + rewardStr);
                    continue;
                }
                condition = condition.substring(1);
                switch (condition) {
                    case "onSuccess" -> onSuccess = true;
                    case "onFail" -> onFail = true;
                    case "onRemove" -> onRemove = true;
                    default -> logger.warning("无效的奖励表达式, 未知的触发条件: " + rewardStr);
                }
            }

            if ("Empty".equalsIgnoreCase(typeName)) { // 空奖励
                continue;
            }

            Reward reward = Optional.ofNullable(REWARDS_MAP.get(typeName)).map(Supplier::get).orElse(null);
            if (reward == null) {
                logger.warning("不支持的奖励类型: " + typeName);
                continue;
            }
            if (reward instanceof PlayerReward playerReward) {
                playerReward.onSuccess = onSuccess;
                playerReward.onFail = onFail;
                playerReward.onRemove = onRemove;
            }

            if (StringUtil.isNotBlank(paramsStr)) {
                Map<String, String> parameters = new HashMap<>();
                // 解析参数部分
                String[] paramPairs = paramsStr.split(";");
                for (String pair : paramPairs) {
                    pair = pair.trim();
                    if (!pair.isEmpty()) {
                        int equalsIndex = pair.indexOf('=');
                        if (equalsIndex > 0 && equalsIndex < pair.length() - 1) {
                            String key = pair.substring(0, equalsIndex).trim();
                            String value = pair.substring(equalsIndex + 1).trim();
                            parameters.put(key, value);
                        } else {
                            logger.warning("奖励参数格式错误，跳过加载: " + pair);
                        }
                    }
                }
                reward.getArgs().putAll(parameters);
            }
            result.add(reward);
        }
        return result;
    }

}
