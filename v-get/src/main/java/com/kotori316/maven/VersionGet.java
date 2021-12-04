package com.kotori316.maven;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Node;

public final class VersionGet {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("""
                Parameters:
                  Version: the version to check the existence
                  URL: the URL to version list of maven.""");
            return;
        }
        String version = args[0];
        if (version.toLowerCase(Locale.ROOT).contains("snapshot")) {
            return;
        }
        String mavenUrl = args[1];
        var versionList = readVersions(mavenUrl);
        if (versionList.stream().map(String::toLowerCase)
            .anyMatch(Predicate.isEqual(version.toLowerCase(Locale.ROOT)))) {
            System.out.println("exists.");
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static List<String> readVersions(String url) {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            var builder = factory.newDocumentBuilder();
            var data = builder.parse(url);

            var versioning = data.getElementsByTagName("versioning").item(0).getChildNodes();
            return IntStream.range(0, versioning.getLength())
                .mapToObj(versioning::item)
                .filter(n -> n.getNodeName().equals("versions"))
                .map(Node::getChildNodes)
                .flatMap(c -> IntStream.range(0, c.getLength()).mapToObj(c::item))
                .map(Node::getFirstChild)
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
