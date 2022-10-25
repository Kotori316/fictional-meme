package com.kotori316.maven;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CountVersionTest {
    @Test
    void allMatch() {
        List<String> versions = Stream.of(
            "19.0-fabric",
            "19.1-fabric",
            "19.2-fabric",
            "19.0",
            "19.1"
        ).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("fabric");
        List<String> list = CountVersion.filterVersions(versions, pattern);
        assertTrue(list.isEmpty());
    }

    @Test
    void allMatch2() {
        List<String> versions = Stream.of(
            "19.0-fabric",
            "19.1-fabric",
            "19.2-fabric",
            "19.0",
            "19.1"
        ).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("19\\.0-fabric");
        List<String> list = CountVersion.filterVersions(versions, pattern);
        assertEquals(1, list.size());
    }

    @Test
    void wild1() {
        List<String> versions = Stream.of(
            "19.0-fabric",
            "19.1-fabric",
            "19.2-fabric",
            "19.0",
            "19.1"
        ).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("19\\.\\d+-fabric");
        List<String> list = CountVersion.filterVersions(versions, pattern);
        assertEquals(3, list.size());
    }

    @Test
    void wild2() {
        List<String> versions = Stream.of(
            "19.0-fabric",
            "19.1-fabric",
            "19.2-fabric",
            "19.0",
            "19.1"
        ).collect(Collectors.toList());
        Pattern pattern = Pattern.compile("19\\.\\d+");
        List<String> list = CountVersion.filterVersions(versions, pattern);
        assertEquals(2, list.size());
    }
}
