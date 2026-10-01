package com.illtamer.plugin.magicgemtiny.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SuperscriptUtilTest {

    @Test
    void toSuperscript() {
        assertEquals("⁰", SuperscriptUtil.toSuperscript(0));
        assertEquals("¹", SuperscriptUtil.toSuperscript(1));
        assertEquals("¹⁰", SuperscriptUtil.toSuperscript(10));
        assertEquals("¹²³", SuperscriptUtil.toSuperscript(123));
        assertThrows(IllegalArgumentException.class, () -> SuperscriptUtil.toSuperscript(-1));
    }

    @Test
    void getLevel() {
        assertEquals(0, SuperscriptUtil.getLevel("神奇镐"));
        assertEquals(1, SuperscriptUtil.getLevel("神奇镐⁺¹"));
        assertEquals(12, SuperscriptUtil.getLevel("神奇镐⁺¹²"));
        assertEquals(0, SuperscriptUtil.getLevel(null));
        // 非末尾的上标不识别
        assertEquals(0, SuperscriptUtil.getLevel("神奇镐⁺¹ 改"));
    }

    @Test
    void increase_noSuffix_appendsOne() {
        assertEquals("神奇镐⁺¹", SuperscriptUtil.increase("神奇镐", 1));
    }

    @Test
    void increase_existingSuffix() {
        assertEquals("神奇镐⁺²", SuperscriptUtil.increase("神奇镐⁺¹", 1));
        assertEquals("A⁺¹⁰", SuperscriptUtil.increase("A⁺⁹", 1));
        assertEquals("A⁺⁵", SuperscriptUtil.increase("A⁺³", 2));
    }

    @Test
    void increase_keepsColorCodes() {
        assertEquals("§b神奇镐⁺²", SuperscriptUtil.increase("§b神奇镐⁺¹", 1));
    }

    @Test
    void increase_middleSuperscriptUntouched() {
        assertEquals("X⁺¹ Y⁺¹", SuperscriptUtil.increase("X⁺¹ Y", 1));
    }

    @Test
    void decrease_toZero_removesSuffix() {
        assertEquals("神奇镐", SuperscriptUtil.increase("神奇镐⁺¹", -1));
        assertEquals("神奇镐⁺¹", SuperscriptUtil.increase("神奇镐⁺²", -1));
    }

    @Test
    void nullName() {
        assertNull(SuperscriptUtil.increase(null, 1));
        assertNull(SuperscriptUtil.applyLevel(null, 1));
    }

}
