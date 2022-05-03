package com.kotori316.fictional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.AbstractMap;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
        InputStream inputStream = VersionGetTest.class.getResourceAsStream("/versions.json");
        assertNotNull(inputStream);
        Gson gson = new Gson();
        try (InputStreamReader reader = new InputStreamReader(inputStream)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            vg = new VersionGet(json);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void createStubInstance() {
        VersionGet vg = new VersionGet(new JsonObject());
        assertNotNull(vg);
    }

    @Test
    void createWithRealJsonFile() {
        assertNotNull(vg);
    }

    @Test
    void dummy() {
        assertTrue(getLatest().findAny().isPresent());
        assertTrue(getPresentKey().findAny().isPresent());
    }

    static Stream<String[]> getLatest() {
        return Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("1.1", "1.3.4.29"),
            new AbstractMap.SimpleImmutableEntry<>("1.2", "3.4.9.171"),
            new AbstractMap.SimpleImmutableEntry<>("1.2.5", "3.4.9.171"),
            new AbstractMap.SimpleImmutableEntry<>("1.2.3", "1.4.1.64"),
            new AbstractMap.SimpleImmutableEntry<>("1.3", "4.3.5.318"),
            new AbstractMap.SimpleImmutableEntry<>("1.3.2", "4.3.5.318"),
            new AbstractMap.SimpleImmutableEntry<>("1.4.0", "5.0.0.326"),
            new AbstractMap.SimpleImmutableEntry<>("1.4.3", "6.2.1.358"),
            new AbstractMap.SimpleImmutableEntry<>("1.4.7", "6.6.2.534"),
            new AbstractMap.SimpleImmutableEntry<>("1.4", "6.6.2.534"),
            new AbstractMap.SimpleImmutableEntry<>("1.5.0", "7.7.0.598"),
            new AbstractMap.SimpleImmutableEntry<>("1.5", "7.8.1.738"),
            new AbstractMap.SimpleImmutableEntry<>("1.7.10", "10.13.4.1614"),
            new AbstractMap.SimpleImmutableEntry<>("1.7", "10.13.4.1614"),
            new AbstractMap.SimpleImmutableEntry<>("1.7.2", "10.12.2.1161"),
            new AbstractMap.SimpleImmutableEntry<>("1.8", "11.15.1.2318"),
            new AbstractMap.SimpleImmutableEntry<>("1.8.0", "11.14.4.1577"),
            new AbstractMap.SimpleImmutableEntry<>("1.10.0", "12.18.0.2000"),
            new AbstractMap.SimpleImmutableEntry<>("1.10.2", "12.18.3.2511"),
            new AbstractMap.SimpleImmutableEntry<>("1.10", "12.18.3.2511"),
            new AbstractMap.SimpleImmutableEntry<>("1.14", "28.2.26"),
            new AbstractMap.SimpleImmutableEntry<>("1.15.0", "29.0.4"),
            new AbstractMap.SimpleImmutableEntry<>("1.15", "31.2.57"),
            new AbstractMap.SimpleImmutableEntry<>("1.17", "37.1.1"),
            new AbstractMap.SimpleImmutableEntry<>("1.18.0", "38.0.17"),
            new AbstractMap.SimpleImmutableEntry<>("1.18", "39.0.5"),
            new AbstractMap.SimpleImmutableEntry<String, String>(null, "39.0.5")
        ).map(e -> new String[]{e.getKey(), e.getValue()});
    }

    @ParameterizedTest
    @MethodSource
    void getLatest(String key, String ans) {
        String actual = vg.getLatest(key);
        assertTrue(actual.contains(ans),
            String.format("Actual %s, expected %s, key=%s", actual, ans, key));
    }

    static Stream<String[]> getPresentKey() {
        return Stream.of(
            new AbstractMap.SimpleImmutableEntry<>("1.1-latest", "1.1-1.3.4.29"),
            new AbstractMap.SimpleImmutableEntry<>("1.7.10_pre4-latest", "1.7.10_pre4-10.12.2.1149"),
            new AbstractMap.SimpleImmutableEntry<>("1.11-recommended", "1.11-13.19.1.2189"),
            new AbstractMap.SimpleImmutableEntry<>("1.11-latest", "1.11-13.19.1.2199"),
            new AbstractMap.SimpleImmutableEntry<>("1.11.2-latest", "1.11.2-13.20.1.2588"),
            new AbstractMap.SimpleImmutableEntry<>("1.12.2-recommended", "1.12.2-14.23.5.2859"),
            new AbstractMap.SimpleImmutableEntry<>("1.18.0-latest", "1.18-38.0.17"),
            new AbstractMap.SimpleImmutableEntry<>("1.18-latest", "1.18-38.0.17"), // This is not valid.
            new AbstractMap.SimpleImmutableEntry<>("1.18.1-latest", "1.18.1-39.0.5")
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
            () -> assertEquals("1.16.5-36.2.22", vg.getLatest("1.16-latest")),
            () -> assertEquals("1.18.1-39.0.5", vg.getLatest("1.30")),
            () -> assertEquals("1.18.1-39.0.5", vg.getLatest("2.0"))
        );
    }

    @ParameterizedTest
    @MethodSource({"getPresentKey", "getLatest"})
    void notFound(String input) {
        VersionGet vg = new VersionGet(new JsonObject());
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
        VersionGet.Key key = VersionGet.Key.fromString(keyString, false);
        assertNotNull(key);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.1", "1.16.5", "1.16", "1.17", "2.0"})
    void replaceGroup(String version) {
        VersionGet.Key key = VersionGet.Key.fromString(version + "-recommend", false);
        assertEquals(VersionGet.Key.fromString(version + "-recommended", false), key);
    }
}
