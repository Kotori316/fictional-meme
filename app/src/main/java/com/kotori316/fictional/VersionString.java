package com.kotori316.fictional;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record VersionString(int top, int major, int minor, String postfix) {
    static final Pattern VERSION_PATTERN_WITH_GROUP = Pattern.compile(
        "(?<top>\\d)\\.(?<major>\\d+)(?:|\\.(?<minor>\\d+)(?<postfix>.*))-(?<group>\\w+)"
    );
    static final Pattern VERSION_PATTERN = Pattern.compile(
        "(?<top>\\d)\\.(?<major>\\d+)(?:|\\.(?<minor>\\d+)(?<postfix>.*))"
    );

    public VersionString(int top, int major, int minor) {
        this(top, major, minor, "");
    }

    public VersionString(int major, int minor) {
        this(1, major, minor);
    }

    static VersionString getInstance(String s, boolean ignoreMinor) {
        Matcher m;
        if (s.contains("-")) {
            m = VERSION_PATTERN_WITH_GROUP.matcher(s);
        } else {
            m = VERSION_PATTERN.matcher(s);
        }
        if (m.matches()) {
            var top = Integer.parseInt(m.group("top"));
            var major = Integer.parseInt(m.group("major"));
            var minor = m.group("minor") == null ? (ignoreMinor ? -1 : 0) : Integer.parseInt(m.group("minor"));
            var post = m.group("postfix");
            var postfix = post == null ? "" : post;
            return new VersionString(top, major, minor, postfix);
        } else {
            throw new IllegalArgumentException("Bad version " + s);
        }
    }

    static VersionString getInstance(String s) {
        return getInstance(s, false);
    }

    public boolean equalsMajor(VersionString that) {
        return top == that.top && major == that.major;
    }

    @Override
    public String toString() {
        if (minor == 0) {
            return String.format("%d.%d", top, major);
        } else {
            return String.format("%d.%d.%d%s", top, major, minor, postfix);
        }
    }

    public static final Comparator<VersionString> COMPARATOR =
        Comparator.comparingInt(VersionString::top)
            .thenComparingInt(VersionString::major)
            .thenComparingInt(VersionString::minor)
            .thenComparing(Comparator.comparing(VersionString::postfix).reversed());
}
