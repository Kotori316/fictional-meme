package com.kotori316.fictional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class VersionGet {
    public static final String TARGET = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/promotions_slim.json";
    private final Map<Key, String> versionMap;

    public VersionGet() {
        this.versionMap = makeMap(getContent());
    }

    VersionGet(JsonObject stub) {
        this.versionMap = makeMap(stub);
    }

    static Map<Key, String> makeMap(JsonObject object) {
        return object.entrySet().stream().map(e -> {
            var key = Key.fromString(e.getKey(), false);
            var value = e.getValue().getAsString();
            return Map.entry(key, value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static JsonObject getContent() {
        try {
            var apiUrl = new URL(VersionGet.TARGET);
            try (var inputStream = apiUrl.openStream();
                 var reader = new InputStreamReader(inputStream)) {
                var gson = new Gson();
                return gson.fromJson(reader, JsonObject.class).getAsJsonObject("promos");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getLatest(String s) {
        var key1 = Key.fromString(s, false);
        if (this.versionMap.containsKey(key1)) // User gave correct key in version list.
            return getLatest(key1);
        else // We should search the matched version with the given parameter.
            return getLatest(Key.fromString(s, true));
    }

    private String getLatest(Key vKey) {
        if (!this.versionMap.containsKey(vKey)) {
            var allLatest = this.versionMap.entrySet().stream()
                .filter(e -> e.getKey().group.equals("latest"))
                .map(e -> Map.entry(e.getKey().versionString, e.getValue()))
                .collect(Collectors.toUnmodifiableList());
            var latest = allLatest.stream()
                .filter(e -> e.getKey().equalsMajor(vKey.versionString))
                .collect(Collectors.toUnmodifiableList());
            return latest.stream().filter(e -> e.getKey().equals(vKey.versionString)).findFirst()
                .or(() -> latest.stream().max(Map.Entry.comparingByKey(VersionString.COMPARATOR)))
                .or(() -> allLatest.stream().max(Map.Entry.comparingByKey(VersionString.COMPARATOR)))
                .map(e -> e.getKey().toString() + "-" + e.getValue()).orElse(null);
        } else {
            var version = this.versionMap.get(vKey);
            return vKey.versionString.toString() + "-" + version;
        }
    }

    public static class Key {
        private final VersionString versionString;
        private final String group;

        public Key(VersionString versionString, String group) {
            this.versionString = versionString;
            this.group = group;
        }

        static Key fromString(String s, boolean givenKey) {
            if (s == null) {
                return new Key(new VersionString(0, 0, 0), "latest");
            }
            var vs = new VersionString(s, givenKey);
            if (s.contains("-")) {
                var group = s.split("-")[1];
                return new Key(vs, group);
            } else {
                return new Key(vs, "");
            }
        }

        @Override
        public String toString() {
            if (!group.isEmpty())
                return versionString.toString() + "-" + group;
            else
                return versionString.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return versionString.equals(key.versionString) && group.equals(key.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionString, group);
        }
    }
}
