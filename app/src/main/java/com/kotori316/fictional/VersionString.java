package com.kotori316.fictional;

import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionString {
    static final Pattern VERSION_PATTERN_WITH_GROUP = Pattern.compile(
        "(?<top>\\d)\\.(?<major>\\d+)(?:|\\.(?<minor>\\d+)(?<postfix>.*))-(?<group>\\w+)"
    );
    static final Pattern VERSION_PATTERN = Pattern.compile(
        "(?<top>\\d)\\.(?<major>\\d+)(?:|\\.(?<minor>\\d+)(?<postfix>.*))"
    );
    private final int top;
    private final int major;
    private final int minor;
    private final String postfix;

    public VersionString(int top, int major, int minor, String postfix) {
        this.top = top;
        this.major = major;
        this.minor = minor;
        this.postfix = postfix;
    }

    public VersionString(int top, int major, int minor) {
        this(top, major, minor, "");
    }

    public VersionString(int major, int minor) {
        this(1, major, minor);
    }

    public VersionString(String s, boolean ignoreMinor) {
        Matcher m;
        if (s.contains("-")) {
            m = VERSION_PATTERN_WITH_GROUP.matcher(s);
        } else {
            m = VERSION_PATTERN.matcher(s);
        }
        if (m.matches()) {
            this.top = Integer.parseInt(m.group("top"));
            this.major = Integer.parseInt(m.group("major"));
            var minor = m.group("minor");
            this.minor = minor == null ? (ignoreMinor ? -1 : 0) : Integer.parseInt(minor);
            var post = m.group("postfix");
            this.postfix = post == null ? "" : post;
        } else {
            throw new IllegalArgumentException("Bad version " + s);
        }
    }

    public VersionString(String s) {
        this(s, false);
    }

    public boolean equalsMajor(VersionString that) {
        return top == that.top && major == that.major;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VersionString that = (VersionString) o;
        return top == that.top && major == that.major && minor == that.minor && postfix.equals(that.postfix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(top, major, minor, postfix);
    }

    @Override
    public String toString() {
        if (minor == 0) {
            return String.format("%d.%d", top, major);
        } else {
            return String.format("%d.%d.%d%s", top, major, minor, postfix);
        }
    }

    public int getTop() {
        return top;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public String getPostfix() {
        return postfix;
    }

    public static final Comparator<VersionString> COMPARATOR =
        Comparator.comparingInt(VersionString::getTop)
            .thenComparingInt(VersionString::getMajor)
            .thenComparingInt(VersionString::getMinor)
            .thenComparing(Comparator.comparing(VersionString::getPostfix).reversed());
}
