package com.kotori316.fictional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionGetTest {

    static VersionGet vg;

    @BeforeAll
    static void setup() {
        var inputStream = VersionGetTest.class.getResourceAsStream("/versions.json");
        assertNotNull(inputStream);
        var gson = new Gson();
        try (var reader = new InputStreamReader(inputStream)) {
            var json = gson.fromJson(reader, JsonObject.class);
            vg = new VersionGet(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void createStubInstance() {
        var vg = new VersionGet(new JsonObject());
        assertNotNull(vg);
    }

    @Test
    void createWithRealJsonFile() {
        assertNotNull(vg);
    }

    @Test
    void dummy() {
        assertTrue(getLatest().count() > 0);
        assertTrue(getPresentKey().count() > 0);
    }

    static Stream<String[]> getLatest() {
        return Stream.of(
            Map.entry("1.1", "1.3.4.29"),
            Map.entry("1.2", "3.4.9.171"),
            Map.entry("1.2.5", "3.4.9.171"),
            Map.entry("1.2.3", "1.4.1.64"),
            Map.entry("1.3", "4.3.5.318"),
            Map.entry("1.3.2", "4.3.5.318"),
            Map.entry("1.4.0", "5.0.0.326"),
            Map.entry("1.4.3", "6.2.1.358"),
            Map.entry("1.4.7", "6.6.2.534"),
            Map.entry("1.4", "6.6.2.534"),
            Map.entry("1.5.0", "7.7.0.598"),
            Map.entry("1.5", "7.8.1.738"),
            Map.entry("1.7.10", "10.13.4.1614"),
            Map.entry("1.7", "10.13.4.1614"),
            Map.entry("1.7.2", "10.12.2.1161"),
            Map.entry("1.8", "11.15.1.2318"),
            Map.entry("1.8.0", "11.14.4.1577"),
            Map.entry("1.10.0", "12.18.0.2000"),
            Map.entry("1.10.2", "12.18.3.2511"),
            Map.entry("1.10", "12.18.3.2511"),
            Map.entry("1.14", "28.2.23"),
            Map.entry("1.15.0", "29.0.4"),
            Map.entry("1.15", "31.2.50"),
            Map.entry("1.17", "37.0.0"),
            new AbstractMap.SimpleImmutableEntry<String, String>(null, "37.0.0")
        ).map(e -> new String[]{e.getKey(), e.getValue()});
    }

    @ParameterizedTest
    @MethodSource
    void getLatest(String key, String ans) {
        var actual = vg.getLatest(key);
        assertTrue(actual.contains(ans),
            String.format("Actual %s, expected %s, key=%s", actual, ans, key));
    }

    static Stream<String[]> getPresentKey() {
        return Stream.of(
            Map.entry("1.1-latest", "1.1-1.3.4.29"),
            Map.entry("1.7.10_pre4-latest", "1.7.10_pre4-10.12.2.1149"),
            Map.entry("1.11-recommended", "1.11-13.19.1.2189"),
            Map.entry("1.11-latest", "1.11-13.19.1.2199"),
            Map.entry("1.11.2-latest", "1.11.2-13.20.1.2588"),
            Map.entry("1.12.2-recommended", "1.12.2-14.23.5.2855"),
            Map.entry("1.17.0-latest", "1.17-37.0.0")
        ).map(e -> new String[]{e.getKey(), e.getValue()});
    }

    @ParameterizedTest
    @MethodSource
    void getPresentKey(String key, String ans) {
        assertEquals(ans, vg.getLatest(key));
    }

    @Test
    void notExistLatest() {
        assertAll(
            () -> assertEquals("1.7.10-10.13.4.1614", vg.getLatest("1.7.5-latest")),
            () -> assertEquals("1.16.5-36.1.23", vg.getLatest("1.16-latest")),
            () -> assertEquals("1.17-37.0.0", vg.getLatest("1.18")),
            () -> assertEquals("1.17-37.0.0", vg.getLatest("2.0"))
        );
    }

    @ParameterizedTest
    @MethodSource({"getPresentKey", "getLatest"})
    void notFound(String input) {
        var vg = new VersionGet(new JsonObject());
        assertThrows(IllegalArgumentException.class, () -> vg.getLatest(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "1.7.10-10.13.4.1600",
        "1.16.4-35.1.36",
        "1.17.0-37.1.0"
    })
    void checkKey(String key) {
        assertFalse(App.shouldSearch(key));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"1.1-latest", "1.7.10_pre4-latest", "1.11-recommended", "1.11-latest",
        "1.11.2-latest", "1.12.2-recommended", "1.17.0-latest"})
    void createKey(String keyString) {
        var key = VersionGet.Key.fromString(keyString, false);
        assertNotNull(key);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1", "1.16.5", "1.16", "1.17", "2.0"})
    void replaceGroup(String version) {
        var key = VersionGet.Key.fromString(version + "-recommend", false);
        assertEquals(VersionGet.Key.fromString(version + "-recommended", false), key);
    }
}
