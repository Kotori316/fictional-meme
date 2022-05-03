package com.kotori316.fictional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
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
            Key key = Key.fromString(e.getKey(), false);
            String value = e.getValue().getAsString();
            return new AbstractMap.SimpleImmutableEntry<>(key, value);
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    static JsonObject getContent() {
        try {
            URL apiUrl = new URL(VersionGet.TARGET);
            try (InputStream inputStream = apiUrl.openStream();
                 InputStreamReader reader = new InputStreamReader(inputStream)) {
                Gson gson = new Gson();
                return gson.fromJson(reader, JsonObject.class).getAsJsonObject("promos");
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getLatest(String s) {
        Key key1 = Key.fromString(s, false);
        if (this.versionMap.containsKey(key1)) // User gave correct key in version list.
            return getLatest(key1);
        else // We should search the matched version with the given parameter.
            return getLatest(Key.fromString(s, true));
    }

    private String getLatest(Key vKey) {
        if (!this.versionMap.containsKey(vKey)) {
            List<Map.Entry<VersionString, String>> allLatest = this.versionMap.entrySet().stream()
                .filter(e -> e.getKey().group.equals("latest"))
                .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey().versionString, e.getValue()))
                .collect(Collectors.toList());
            List<Map.Entry<VersionString, String>> sameMajors = allLatest.stream()
                .filter(e -> e.getKey().equalsMajor(vKey.versionString))
                .collect(Collectors.toList());
            return or(
                () -> sameMajors.stream().filter(e -> e.getKey().equals(vKey.versionString)).findFirst(),
                () -> sameMajors.stream().max(Map.Entry.comparingByKey(VersionString.COMPARATOR)),
                () -> allLatest.stream().max(Map.Entry.comparingByKey(VersionString.COMPARATOR))
            )
                .map(e -> e.getKey().toString() + "-" + e.getValue())
                .orElseThrow(() -> new IllegalArgumentException("No compatible version found for " + vKey));
        } else {
            String version = this.versionMap.get(vKey);
            return vKey.versionString.toString() + "-" + version;
        }
    }

    private static <T> Optional<T> or(Supplier<Optional<T>> o1, Supplier<Optional<T>> o2, Supplier<Optional<T>> o3) {
        Optional<T> t1 = o1.get();
        if (t1.isPresent()) return t1;
        Optional<T> t2 = o2.get();
        if (t2.isPresent()) return t2;
        return o3.get();
    }

    public static final class Key {
        private static final Map<String, String> replaceMap;

        static {
            Map<String, String> m = new HashMap<>();
            m.put("recommend", "recommended");
            replaceMap = Collections.unmodifiableMap(m);
        }

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
            VersionString vs = VersionString.getInstance(s, givenKey);
            if (s.contains("-")) {
                String group = s.split("-")[1];
                return new Key(vs, replaceMap.getOrDefault(group, group));
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
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            Key that = (Key) obj;
            return Objects.equals(this.versionString, that.versionString) &&
                   Objects.equals(this.group, that.group);
        }

        @Override
        public int hashCode() {
            return Objects.hash(versionString, group);
        }

    }
}
