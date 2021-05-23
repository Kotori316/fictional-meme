package com.kotori316.fictional;

import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        List<String> results;
        if (args.length == 0) {
            var vg = new VersionGet();
            results = List.of(vg.getLatest(null));
        } else {
            results = getVersions(args);
        }
        for (String s : results) {
            System.out.println(s);
        }
    }

    static List<String> getVersions(String[] versionStrings) {
        VersionGet vg = null;
        List<String> list = new ArrayList<>();
        for (String s : versionStrings) {
            String ver;
            if (shouldSearch(s)) {
                if (vg == null)
                    vg = new VersionGet();
                ver = vg.getLatest(s);
            } else {
                ver = s;
            }
            list.add(ver);
        }
        return list;
    }

    static boolean shouldSearch(String key) {
        var split = key.split("-", 2);
        if (split.length == 2) {
            var maybeVersion = split[1].split("\\.");
            return maybeVersion.length <= 2;
        }
        return true;
    }
}
