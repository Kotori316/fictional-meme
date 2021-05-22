package com.kotori316.fictional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String[] args) {
        var vg = new VersionGet();
        List<String> results;
        if (args.length == 0) {
            results = List.of(vg.getLatest(null));
        } else {
            results = Arrays.stream(args).map(vg::getLatest).collect(Collectors.toList());
        }
        for (String s : results) {
            System.out.println(s);
        }
    }
}
