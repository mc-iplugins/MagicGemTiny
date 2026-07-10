package com.illtamer.plugin.magicgemtiny.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.plugin.magicgemtiny.MagicGemTiny;
import com.illtamer.plugin.magicgemtiny.util.EnumUtil;
import com.illtamer.plugin.magicgemtiny.util.GemUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * 装备药水效果周期任务
 * <p>
 * 每 40 tick（2 秒）扫描所有在线玩家的主手、副手和 4 件盔甲槽，
 * 读取物品 {@code MAGICGEM_CHANGE_LOG} 中的 {@code Potion} 条目，
 * 持续为玩家施加对应药水效果。
 * <p>
 * 当宝石被拆卸（changelog 记录删除）或物品离开有效槽位后，
 * 本任务会在下一周期主动移除对应效果。
 */
public class EquipmentPotionEffectTask {

    /** 扫描周期（tick），2 秒 */
    private static final long PERIOD_TICK = 40L;

    /**
     * 有限 duration 最小续期时长（tick）。
     * 每次续期传入 duration_seconds * 20 与此值的较大值，
     * 保证在下一周期到达前效果不会自然消失。
     */
    private static final int MIN_REFRESH_TICK = (int) (PERIOD_TICK * 2);

    private static BukkitTask task;

    /**
     * 记录上一轮为每位玩家施加过的效果类型集合。
     * 本轮不再命中的类型会被主动 removePotionEffect，
     * 避免效果在物品被移除后持续残留。
     */
    private static final Map<UUID, Set<PotionEffectType>> LAST_APPLIED = new HashMap<>();

    /**
     * 在插件 onEnable 时调用，启动周期任务。
     */
    public static void start() {
        MagicGemTiny plugin = MagicGemTiny.getInstance();
        task = Bukkit.getScheduler().runTaskTimer(
                plugin,
                EquipmentPotionEffectTask::tick,
                PERIOD_TICK,
                PERIOD_TICK
        );
    }

    /**
     * 在插件 onDisable 时调用，停止任务并清除已施加的效果，防止残留。
     */
    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // 对仍在线的玩家主动移除所有本插件施加的效果
        for (Map.Entry<UUID, Set<PotionEffectType>> entry : LAST_APPLIED.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) continue;
            for (PotionEffectType type : entry.getValue()) {
                player.removePotionEffect(type);
            }
        }
        LAST_APPLIED.clear();
    }

    // ---- 内部实现 ----

    private static void tick() {
        // 清理已下线玩家的记录，避免 Map 无限增长
        LAST_APPLIED.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);

        for (Player player : Bukkit.getOnlinePlayers()) {
            applyForPlayer(player);
        }
    }

    /**
     * 计算并应用单个玩家当前应有的装备药水效果。
     */
    private static void applyForPlayer(Player player) {
        // key: 效果类型；value: [level, durationSec]（-1 表示无限）
        Map<PotionEffectType, int[]> target = new HashMap<>();

        // 扫描所有有效槽位：主手、副手、头盔、胸甲、护腿、靴子
        List<ItemStack> slots = new ArrayList<>(6);
        slots.add(player.getInventory().getItemInMainHand());
        slots.add(player.getInventory().getItemInOffHand());
        Collections.addAll(slots, player.getInventory().getArmorContents());

        for (ItemStack item : slots) {
            collectPotionsFromItem(item, target);
        }

        // 应用本轮收集到的效果，同时记录本轮已应用的类型
        Set<PotionEffectType> applied = new HashSet<>();
        for (Map.Entry<PotionEffectType, int[]> entry : target.entrySet()) {
            PotionEffectType type = entry.getKey();
            int level = entry.getValue()[0];
            int durationSec = entry.getValue()[1];

            int amplifier = Math.max(0, level - 1);
            int durationTick;
            if (durationSec < 0) {
                // 无限持续
                durationTick = PotionEffect.INFINITE_DURATION;
            } else {
                // 有限持续：确保每周期续期后不会在下一周期前消失
                durationTick = Math.max(MIN_REFRESH_TICK, durationSec * 20);
            }

            PotionEffect effect = new PotionEffect(
                    type,
                    durationTick,
                    amplifier,
                    true,   // ambient：减少粒子并使图标呈信标样式
                    false,  // particles：关闭粒子效果，减少视觉干扰
                    true    // icon：在 HUD 显示效果图标
            );
            // 直接覆盖旧效果（Bukkit 会用新 PotionEffect 覆盖同类型旧效果）
            player.addPotionEffect(effect);
            applied.add(type);
        }

        // 上一轮有、本轮没有的效果 → 主动移除（装备拆下 / 宝石拆卸 / 手持更换等）
        Set<PotionEffectType> previous = LAST_APPLIED.getOrDefault(
                player.getUniqueId(), Collections.emptySet());
        for (PotionEffectType type : previous) {
            if (!applied.contains(type)) {
                player.removePotionEffect(type);
            }
        }

        // 更新记录
        if (applied.isEmpty()) {
            LAST_APPLIED.remove(player.getUniqueId());
        } else {
            LAST_APPLIED.put(player.getUniqueId(), applied);
        }
    }

    /**
     * 从单个物品的 changelog 中收集所有 {@code Potion} 条目，
     * 合并到 {@code target} Map 中（同类型取最高等级；duration 无限优先）。
     *
     * @param item   待扫描的物品
     * @param target 结果汇总 Map，key = 效果类型，value = [level, durationSec]
     */
    private static void collectPotionsFromItem(ItemStack item,
                                               Map<PotionEffectType, int[]> target) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) {
            return;
        }

        JsonArray array = GemUtil.getChangeLogArray(item);
        for (JsonElement element : array) {
            if (!element.isJsonObject()) continue;
            JsonObject log = element.getAsJsonObject();

            JsonElement potionEle = log.get("Potion");
            if (potionEle == null || !potionEle.isJsonObject()) continue;

            JsonObject p = potionEle.getAsJsonObject();
            if (!p.has("name") || p.get("name").isJsonNull()) continue;

            PotionEffectType type = EnumUtil.getPotionEffectType(p.get("name").getAsString());
            if (type == null) continue;

            int level = (p.has("level") && !p.get("level").isJsonNull())
                    ? p.get("level").getAsInt() : 1;
            int duration = (p.has("duration") && !p.get("duration").isJsonNull())
                    ? p.get("duration").getAsInt() : -1;

            // 合并：同类型取最高等级；duration：无限（-1）优先，否则取较大值
            target.merge(type, new int[]{ level, duration }, (a, b) -> new int[]{
                    Math.max(a[0], b[0]),
                    (a[1] < 0 || b[1] < 0) ? -1 : Math.max(a[1], b[1])
            });
        }
    }

}
