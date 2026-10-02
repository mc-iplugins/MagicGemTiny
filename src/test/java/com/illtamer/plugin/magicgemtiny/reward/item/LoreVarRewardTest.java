package com.illtamer.plugin.magicgemtiny.reward.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoreVarRewardTest {

    @Test
    void replaceNumberAtIndex_keepsLegacyColorDigits() {
        String lore = "§f最高§x§6§6§9§9§F§F星火§x§F§F§F§F§C§C数量§9: §69";

        assertEquals("§f最高§x§6§6§9§9§F§F星火§x§F§F§F§F§C§C数量§9: §611",
                LoreVarReward.replaceNumberAtIndex(lore, 1, "11"));
    }

    @Test
    void replaceNumberAtIndex_keepsHashHexColorDigits() {
        String lore = "&#FFFFCC最高&#6699FF星火&#FFFFCC数量&9: &69";

        assertEquals("&#FFFFCC最高&#6699FF星火&#FFFFCC数量&9: &611",
                LoreVarReward.replaceNumberAtIndex(lore, 1, "11"));
    }

    @Test
    void replaceNumberAtIndex_replacesOnlySelectedVisibleNumber() {
        String lore = "§x§6§6§9§9§F§F等级: §61, 伤害: §62";

        assertEquals("§x§6§6§9§9§F§F等级: §61, 伤害: §63",
                LoreVarReward.replaceNumberAtIndex(lore, 2, "3"));
    }

}
