package com.illtamer.plugin.magicgemtiny.reward.item;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.illtamer.lib.Pair;
import com.illtamer.plugin.magicgemtiny.exception.ConditionException;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.ExpressionEvaluator;
import com.illtamer.plugin.magicgemtiny.libs.ikexpression.datameta.Variable;
import com.illtamer.plugin.magicgemtiny.reward.ItemReward;
import com.illtamer.plugin.magicgemtiny.util.StringUtil;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑物品Lore数字
 * */
public class LoreVarReward extends ItemReward {

    // 变量名：要编辑的数字前面的识别标志。如"物理伤害"。
    // - 插件将会读取其后的第一个整数或者浮点数参与计算
    // - 识别变量名和读取数字均忽略颜色代码
    private String lore;
    private String var;
    private @Nullable String inv;
    private String limit;
    // 格式;(允许新增才填)
    private String format;
    // 模式
    private String mode;
    // 定位lore
    private String locator;
    // 是否把数字加到宝石前面
    private boolean prefix;
    // 第几个数字
    private Integer index;

    @Override
    protected void init() {
        lore = StringUtil.c(getParamString("lore", null));
        var = getParamString("var", null);
        inv = getParamString("inv", null);
        limit = getParamString("limit", null);
        format = getParamString("format", null);
        format = StringUtil.isBlank(format) ? "%.0f" : format;
        mode = getParamString("mode", null);
        locator = StringUtil.clearColor(getParamString("locator", null));
        prefix = getParamBoolean("prefix", null);
        index = getParamInteger("index", null);
        index = index == null ? 1 : index;
    }

    @Override
    public void execute(NBTItem nbtItem, Player player, JsonObject json) {
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();

        List<String> loreList = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        JsonObject record = new JsonObject();

        // 查找是否存在现有的变量名 Pair<lore所在索引, oldValue>
        Pair<Integer, Double> pair = anyMatchInLoreList(loreList);

        try {
            // 计算新值
            double newValue = Double.parseDouble(ExpressionEvaluator.evaluate(var, Collections.singleton(
                    Variable.createVariable("v", pair != null ? pair.getValue() : 0.0))).toString());

            if (pair != null) { // 替换逻辑
                int loreIndex = pair.getKey();
                String oldLore = loreList.get(loreIndex);
                double oldValue = pair.getValue();
                String newLore = replaceNumberAtIndex(oldLore, index, String.format(format, newValue));
                loreList.set(loreIndex, newLore);
                // record
                record(loreIndex, oldValue, oldLore, newValue, newLore, true, record);
            } else if (StringUtil.isNotBlank(mode)) { // 变量名没有找到时，根据locator添加
                String formatNewValue = String.format(format, newValue);
                String newLoreSub = prefix ? (formatNewValue + lore) : (lore + formatNewValue);
                int insertPos = loreList.size(); // 默认末尾
                boolean isReplace = false;

                switch (mode.toLowerCase()) {
                    case "first" -> insertPos = 0;
                    case "last" -> insertPos = loreList.size();
                    case "before", "after", "line" -> {
                        int locIdx = -1;
                        for (int i = 0; i < loreList.size(); i++) {
                            if (StringUtil.clearColor(loreList.get(i)).contains(locator)) {
                                locIdx = i;
                                break;
                            }
                        }
                        if (locIdx != -1) {
                            if ("before".equals(mode)) insertPos = locIdx;
                            else if ("after".equals(mode)) insertPos = locIdx + 1;
                            else {
                                insertPos = locIdx;
                                isReplace = true;
                            }
                        }
                    }
                }

                if (isReplace && insertPos < loreList.size()) { // 将locator替换为变量名
                    String oldLore = loreList.get(insertPos);
                    String newLore = oldLore.replace(locator, newLoreSub);
                    loreList.set(insertPos, newLore);
                    // record
                    record(insertPos, null, oldLore, newValue, newLore, true, record);
                } else { // 新增
                    loreList.add(insertPos, newLoreSub);
                    // record
                    record(insertPos, null, null, newValue, newLoreSub, false, record);
                }
            }

            meta.setLore(loreList);
            item.setItemMeta(meta);
        } finally {
            json.add("LoreVar", record);
        }
    }

    /**
     * @param oldValue 变量名没有找到时，根据locator添加时，始终为null
     * @param oldLore 变量名没有找到时，根据locator添加时，找不到locator时，为null
     * */
    private void record(
            int pos,
            @Nullable Double oldValue,
            @Nullable String oldLore,
            double newValue,
            String newLore,
            boolean replace,
            JsonObject json
    ) {
        json.addProperty("index", pos);
        json.addProperty("oldValue", oldValue);
        json.addProperty("oldLore", oldLore);
        json.addProperty("newValue", newValue);
        json.addProperty("newLore", newLore);
        json.addProperty("replace", replace);
    }

    @Override
    protected boolean tryTest(NBTItem nbtItem) throws ConditionException {
        ItemMeta meta = nbtItem.getItem().getItemMeta();
        if (meta == null) {
            throw new ConditionException("该物品不支持编辑Lore");
        }
        List<String> loreList = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        if (StringUtil.isBlank(lore)) {
            throw new ConditionException("要修改的lore不能为空");
        }
        if (StringUtil.isBlank(var)) {
            throw new ConditionException("强化表达公式为空");
        }
        // 结果包含index筛选
        Pair<Integer, Double> pair = anyMatchInLoreList(loreList);
        if (pair == null) { // 变量名没有找到时，需要设置模式
            if (StringUtil.isBlank(mode) || !Arrays.asList("first", "last", "before", "after", "line").contains(mode)) {
                throw new ConditionException("变量名没有找到，不支持的替换模式: " + mode);
            }
            if (StringUtil.isBlank(locator) && Arrays.asList("before", "after", "line").contains(mode)) {
                throw new ConditionException("变量名没有找到，当前处于定位器模式，但定位器为空");
            }
            if ("line".equals(mode)) { // line模式下将locator替换为变量名
                int locIdx = -1;
                for (int i = 0; i < loreList.size(); i++) {
                    if (StringUtil.clearColor(loreList.get(i)).contains(locator)) {
                        locIdx = i;
                        break;
                    }
                }
                if (locIdx == -1) {
                    throw new ConditionException("变量名没有找到，line模式下找不到locator");
                }
            }
        }
        if (format.contains("ROMAN")) {
            throw new ConditionException("暂不支持提取罗马数字");
        }

        // 判断装备属性是否达上限
        return StringUtil.isBlank(limit) || (StringUtil.isNotBlank(limit) && checkLimit(pair != null ? pair.getValue() : 0.0));
    }

    @Override
    public boolean disassemble() {
        return true;
    }

    /**
     * 拆卸还原
     * @return 是否可安全提交拆卸。目标行已非强化后的值(被篡改/覆盖)时返回 false 以中止拆卸
     * @apiNote replace=true 将该行设回 oldLore；replace=false（新增模式）移除新增的 newLore 行
     * */
    @Override
    public boolean restore(NBTItem nbtItem, Player player, JsonObject log) {
        JsonElement element = log.get("LoreVar");
        if (element == null || !element.isJsonObject()) {
            return true; // 本奖励未产生记录, 无需还原
        }
        JsonObject record = element.getAsJsonObject();
        if (record.size() == 0) { // 镶嵌时未命中任何修改
            return true;
        }
        ItemStack item = nbtItem.getItem();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        List<String> loreList = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        int index = record.has("index") && !record.get("index").isJsonNull() ? record.get("index").getAsInt() : -1;
        boolean replace = record.has("replace") && !record.get("replace").isJsonNull() && record.get("replace").getAsBoolean();
        String oldLore = record.has("oldLore") && !record.get("oldLore").isJsonNull() ? record.get("oldLore").getAsString() : null;
        String newLore = record.has("newLore") && !record.get("newLore").isJsonNull() ? record.get("newLore").getAsString() : null;

        // 目标行越界、或当前已非强化后的值, 视为无法还原
        if (index < 0 || index >= loreList.size() || newLore == null || !loreList.get(index).equals(newLore)) {
            return false;
        }
        if (replace) {
            if (oldLore != null) {
                loreList.set(index, oldLore);
            } else {
                return false; // 记录缺少原始值, 无法还原
            }
        } else { // 新增模式，移除该行
            loreList.remove(index);
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);
        return true;
    }

    /**
     * 查找是否存在现有的变量名
     * @return lore所在索引, oldValue
     * @apiNote 匹配前对 lore 与目标行一并去除颜色代码,
     *      以兼容颜色码插在文字中间的写法(如 星火&x&F&F&F&F&C&C附带伤害)
     * */
    @Nullable
    protected Pair<Integer, Double> anyMatchInLoreList(List<String> loreList) {
        // 正则：匹配整数或浮点数
        String target = toVisibleText(lore).text;

        for (int i = 0; i < loreList.size(); i++) {
            String plainLine = toVisibleText(loreList.get(i)).text;
            if (plainLine.contains(target)) {
                Matcher matcher = NUMBER_PATTERN.matcher(plainLine);
                int count = 0;
                while (matcher.find()) {
                    count++;
                    if (count == index) {
                        return new Pair<>(i, Double.parseDouble(matcher.group()));
                    }
                }
                // TODO 处理罗马数字逻辑 (如果 format 是 ROMAN)
            }
        }
        return null;
    }

    /**
     * 只替换去色后第 targetIndex 个数字, 保留原始 lore 的所有颜色码和其它数字。
     * 该方法包可见, 便于回归测试覆盖颜色码本身含数字的场景。
     * */
    static String replaceNumberAtIndex(String rawLine, int targetIndex, String replacement) {
        VisibleText visible = toVisibleText(rawLine);
        Matcher matcher = NUMBER_PATTERN.matcher(visible.text);
        int count = 0;
        while (matcher.find()) {
            count++;
            if (count == targetIndex) {
                int rawStart = visible.rawStarts.get(matcher.start());
                int rawEnd = visible.rawEnds.get(matcher.end() - 1);
                return rawLine.substring(0, rawStart) + replacement + rawLine.substring(rawEnd);
            }
        }
        return rawLine;
    }

    /**
     * 将原始 lore 转为可见文本, 同时记录每个可见字符对应的原始字符区间。
     * 支持 §/& legacy 颜色码、§x/&x 六位 hex 颜色码和 &#RRGGBB 写法。
     * */
    private static VisibleText toVisibleText(String rawText) {
        StringBuilder text = new StringBuilder(rawText.length());
        List<Integer> rawStarts = new ArrayList<>();
        List<Integer> rawEnds = new ArrayList<>();

        for (int i = 0; i < rawText.length();) {
            int colorEnd = colorCodeEnd(rawText, i);
            if (colorEnd != -1) {
                i = colorEnd;
                continue;
            }
            rawStarts.add(i);
            rawEnds.add(i + 1);
            text.append(rawText.charAt(i));
            i++;
        }
        return new VisibleText(text.toString(), rawStarts, rawEnds);
    }

    /**
     * 返回从 start 开始的颜色码结束位置, 非颜色码返回 -1。
     * */
    private static int colorCodeEnd(String text, int start) {
        if (start + 1 >= text.length()) {
            return -1;
        }
        char marker = text.charAt(start);
        if (marker != '§' && marker != '&') {
            return -1;
        }

        char code = text.charAt(start + 1);
        if (code == '#') {
            if (start + 8 <= text.length()) {
                for (int i = start + 2; i < start + 8; i++) {
                    if (!isHexDigit(text.charAt(i))) {
                        return -1;
                    }
                }
                return start + 8;
            }
            return -1;
        }

        if (code == 'x' || code == 'X') {
            int cursor = start + 2;
            for (int i = 0; i < 6; i++) {
                if (cursor + 1 >= text.length()
                        || (text.charAt(cursor) != '§' && text.charAt(cursor) != '&')
                        || !isHexDigit(text.charAt(cursor + 1))) {
                    return -1;
                }
                cursor += 2;
            }
            return cursor;
        }

        return isLegacyColorCode(code) ? start + 2 : -1;
    }

    private static boolean isLegacyColorCode(char code) {
        return code >= '0' && code <= '9'
                || code >= 'a' && code <= 'f'
                || code >= 'A' && code <= 'F'
                || code >= 'k' && code <= 'o'
                || code >= 'K' && code <= 'O'
                || code == 'r' || code == 'R';
    }

    private static boolean isHexDigit(char c) {
        return c >= '0' && c <= '9'
                || c >= 'a' && c <= 'f'
                || c >= 'A' && c <= 'F';
    }

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    private record VisibleText(String text, List<Integer> rawStarts, List<Integer> rawEnds) {
    }


    /**
     * 校验逻辑表达式 (如 v<=10)
     */
    private boolean checkLimit(double v) {
        List<Variable> variables = new ArrayList<>();
        variables.add(Variable.createVariable("v", v));
        Object result = ExpressionEvaluator.evaluate(limit, variables);
        return result instanceof Boolean ? (Boolean) result : false;
    }

}
