package com.kotori316.maven;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class CountVersion {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Parameters:\n" +
                               "  URL: the URL to version list of maven.\n" +
                               "  Version: the version to check the existence\n"
            );
            System.exit(1);
            return;
        }
        String url = args[0];
        String query;
        if (args.length >= 2) {
            query = args[1];
        } else {
            // We will use regex to select version.
            query = ".*";
        }

        Pattern pattern = Pattern.compile(query);
        List<String> versions = VersionGet.readVersions(url);
        List<String> filtered = filterVersions(versions, pattern);
        filtered.forEach(System.out::println);
    }

    static List<String> filterVersions(List<String> versions, Pattern pattern) {
        return versions.stream().filter(s -> pattern.matcher(s).matches()).collect(Collectors.toList());
    }
}
