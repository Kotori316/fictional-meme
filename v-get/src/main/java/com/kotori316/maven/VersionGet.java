package com.kotori316.maven;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class VersionGet {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Parameters:\n" +
                               "  Version: the version to check the existence\n" +
                               "  URL: the URL to version list of maven.");
            System.exit(1);
            return;
        }
        String version = args[0];
        if (version.toLowerCase(Locale.ROOT).contains("snapshot")) {
            return;
        }
        String mavenUrl = fixUrl(args[1]);
        List<String> versionList = readVersions(mavenUrl);
        if (versionList.stream().map(String::toLowerCase)
            .anyMatch(Predicate.isEqual(version.toLowerCase(Locale.ROOT)))) {
            System.out.println("exists.");
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    static List<String> readVersions(String url) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document data = builder.parse(url);

            NodeList versioning = data.getElementsByTagName("versioning").item(0).getChildNodes();
            return IntStream.range(0, versioning.getLength())
                .mapToObj(versioning::item)
                .filter(n -> n.getNodeName().equals("versions"))
                .map(Node::getChildNodes)
                .flatMap(c -> IntStream.range(0, c.getLength()).mapToObj(c::item))
                .map(Node::getFirstChild)
                .filter(Objects::nonNull)
                .map(Node::getNodeValue)
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String fixUrl(String original) {
        if (original.endsWith(".xml")) {
            return original;
        } else {
            String append = (original.endsWith("/") ? "" : "/") + "maven-metadata.xml";
            return original + append;
        }
    }
}
