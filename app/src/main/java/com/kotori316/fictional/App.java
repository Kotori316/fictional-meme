package com.kotori316.fictional;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        var vg = new VersionGet();
        List<String> results;
        if (args.length == 0) {
            results = List.of(vg.getLatest(null));
        } else {
            results = Arrays.stream(args).map(getVersion(vg)).collect(Collectors.toList());
        }
        for (String s : results) {
            System.out.println(s);
        }
    }

    static Function<String, String> getVersion(VersionGet vg) {
        return key -> {
            var split = key.split("-", 2);
            if (split.length == 2) {
                var maybeVersion = split[1].split("\\.");
                if (maybeVersion.length > 2) {
                    return key;
                }
            }
            return vg.getLatest(key);
        };
    }
}
