package com.kotori316.maven;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VersionGetTest {
    @Nested
    class FixUrlTest {
        @ParameterizedTest
        @ValueSource(strings = {
            "https://dvs1.progwml6.com/files/maven/mezz/jei/jei-1.18/maven-metadata.xml",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/com/kotori316/scalablecatsforce/maven-metadata.xml",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/org/typelevel/cats-kernel_2.13/maven-metadata.xml",
        })
        void fixUrlTestWithSuffix(String original) {
            String fixed = VersionGet.fixUrl(original);
            assertEquals(original, fixed);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://dvs1.progwml6.com/files/maven/mezz/jei/jei-1.18",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/com/kotori316/scalablecatsforce",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/org/typelevel/cats-kernel_2.13",
        })
        void fixUrlTestWithoutSuffix1(String original) {
            String fixed = VersionGet.fixUrl(original);
            assertEquals(original + "/maven-metadata.xml", fixed);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "https://dvs1.progwml6.com/files/maven/mezz/jei/jei-1.18/",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/com/kotori316/scalablecatsforce/",
            "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/org/typelevel/cats-kernel_2.13/",
        })
        void fixUrlTestWithoutSuffix2(String original) {
            String fixed = VersionGet.fixUrl(original);
            assertEquals(original + "maven-metadata.xml", fixed);
        }
    }

    @Nested
    class GetActualVersionTest {
        static final String CAT_CORE_URL = "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/org/typelevel/cats-kernel_2.13/maven-metadata.xml";
        static final String SLP_URL = "https://pkgs.dev.azure.com/Kotori316/minecraft/_packaging/mods/maven/v1/com/kotori316/scalablecatsforce";

        @Test
        void readVersionTest1() {
            List<String> versions = VersionGet.readVersions(CAT_CORE_URL);
            assertTrue(versions.contains("2.6.2-kotori"), "2.6.2-kotori");
            assertTrue(versions.contains("2.7.1-kotori"), "2.7.1-kotori");
            assertFalse(versions.contains(null), "null");
        }

        @Test
        void readVersionTest2() {
            List<String> versions = VersionGet.readVersions(VersionGet.fixUrl(SLP_URL));
            assertTrue(versions.stream().allMatch(s -> s.contains("build")));
        }
    }
}
