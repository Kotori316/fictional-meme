package com.kotori316.fictional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionStringTest {
    @ParameterizedTest
    @ValueSource(strings = {"1.1-recommended", "1.1-latest"})
    void regexTest1(String input) {
        var m = VersionString.VERSION_PATTERN_WITH_GROUP.matcher(input);
        assertTrue(m.matches());
        assertAll(
            () -> assertEquals("1", m.group("top")),
            () -> assertEquals("1", m.group("major")),
            () -> assertNull(m.group("minor")),
            () -> assertNull(m.group("postfix")),
            () -> assertTrue(m.group("group").equals("recommended") || m.group("group").equals("latest"))
        );

        var vs = new VersionString(input);
        assertEquals("1.1", vs.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5.2-recommended", "1.5.2-latest"})
    void regexTest2(String input) {
        var m = VersionString.VERSION_PATTERN_WITH_GROUP.matcher(input);
        assertTrue(m.matches());
        assertAll(
            () -> assertEquals("1", m.group("top")),
            () -> assertEquals("5", m.group("major")),
            () -> assertEquals("2", m.group("minor")),
            () -> assertEquals("", m.group("postfix")),
            () -> assertTrue(m.group("group").equals("recommended") || m.group("group").equals("latest"))
        );
        var vs = new VersionString(input);
        assertEquals("1.5.2", vs.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5.2_pre-recommended", "1.5.2_pre-latest"})
    void regexTest3(String input) {
        var m = VersionString.VERSION_PATTERN_WITH_GROUP.matcher(input);
        assertTrue(m.matches());
        assertAll(
            () -> assertEquals("1", m.group("top")),
            () -> assertEquals("5", m.group("major")),
            () -> assertEquals("2", m.group("minor")),
            () -> assertEquals("_pre", m.group("postfix")),
            () -> assertTrue(m.group("group").equals("recommended") || m.group("group").equals("latest"))
        );
        var vs = new VersionString(input);
        assertEquals("1.5.2_pre", vs.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"bad", "1", "1L.2.52", "1.2L.1", "1,2,3", "1.3."})
    void badInput(String input) {
        assertThrows(IllegalArgumentException.class, () -> new VersionString(input));
    }

    @Test
    void equal1() {
        var a = new VersionString(1, 16, 5);
        var b = new VersionString(16, 5);
        assertEquals(a, b);
    }

    @Test
    void compare1() {
        var a = new VersionString(1, 16, 4);
        var b = new VersionString(1, 16, 5);
        assertTrue(VersionString.COMPARATOR.compare(a, b) < 0);
    }

    @Test
    void compare2() {
        var a = new VersionString(1, 15, 5);
        var b = new VersionString(1, 16, 5);
        assertTrue(VersionString.COMPARATOR.compare(a, b) < 0);
    }

    @Test
    void compare3() {
        var a = new VersionString(1, 17, 4);
        var b = new VersionString(2, 0, 0);
        assertTrue(VersionString.COMPARATOR.compare(a, b) < 0);
    }

    @Test
    void compare4() {
        var a = new VersionString(0, 0, 0);
        var b = new VersionString(1, 0, 0);
        assertTrue(VersionString.COMPARATOR.compare(a, b) < 0);
    }

    @Test
    void compare5() {
        var a = new VersionString(1, 16, 5);
        var b = new VersionString(1, 16, 5);
        assertEquals(0, VersionString.COMPARATOR.compare(a, b));
        assertEquals(a, b);
    }

    @Test
    void compare6() {
        var a = new VersionString(1, 16, 5, "_pre");
        var b = new VersionString(1, 16, 5);
        assertTrue(VersionString.COMPARATOR.compare(a, b) < 0);
    }
}
