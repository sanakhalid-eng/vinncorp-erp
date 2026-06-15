package com.vinncorp.erp.migration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class FlywayMigrationConventionTest {

    static final Path MIGRATIONS_DIR = Paths.get("src/main/resources/db/migration");

    static Stream<Path> migrationFiles() throws IOException {
        return Files.list(MIGRATIONS_DIR)
                .filter(p -> p.toString().endsWith(".sql"))
                .sorted();
    }

    @ParameterizedTest
    @MethodSource("migrationFiles")
    void allCreateTablesShouldHaveIfNotExists(Path file) throws IOException {
        String content = Files.readString(file);
        List<String> violations = new ArrayList<>();

        Pattern createTable = Pattern.compile(
                "CREATE\\s+TABLE\\s+(IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = createTable.matcher(content);
        while (matcher.find()) {
            if (matcher.group(1) == null) {
                violations.add("Missing IF NOT EXISTS: CREATE TABLE " + matcher.group(2));
            }
        }

        if (!violations.isEmpty()) {
            fail("File " + file.getFileName() + " has convention violations:\n" +
                    String.join("\n", violations) +
                    "\n\nTip: Add IF NOT EXISTS to all CREATE TABLE statements for idempotent re-runs.");
        }
    }

    @ParameterizedTest
    @MethodSource("migrationFiles")
    void allCreateTablesShouldHaveEngineAndCharset(Path file) throws IOException {
        String content = Files.readString(file);
        List<String> violations = new ArrayList<>();

        Pattern createPattern = Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?(\\S+)\\s*\\(",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = createPattern.matcher(content);
        while (matcher.find()) {
            String tableName = matcher.group(1);
            int start = matcher.start();
            int nextCreate = content.indexOf("CREATE ", start + 1);
            if (nextCreate == -1) nextCreate = content.length();
            String stmt = content.substring(start, nextCreate);

            if (!stmt.contains("ENGINE=InnoDB") && !stmt.contains("ENGINE = InnoDB")) {
                violations.add("Table " + tableName + " is missing ENGINE=InnoDB");
            } else if (!stmt.contains("CHARSET=utf8mb4") && !stmt.contains("CHARSET = utf8mb4")) {
                violations.add("Table " + tableName + " missing CHARSET=utf8mb4");
            }
        }

        if (!violations.isEmpty()) {
            fail("File " + file.getFileName() + " has convention violations:\n" +
                    String.join("\n", violations));
        }
    }

    @ParameterizedTest
    @MethodSource("migrationFiles")
    void noDeprecatedCharset(Path file) throws IOException {
        String content = Files.readString(file);
        if (content.contains("utf8mb4_unicode_ci")) {
            fail("File " + file.getFileName() +
                    " uses deprecated utf8mb4_unicode_ci. Replace with utf8mb4_0900_ai_ci.");
        }
    }

    @Test
    void flywayMavenPluginIsConfigured() throws IOException {
        String pom = Files.readString(Paths.get("pom.xml"));
        assertTrue(pom.contains("flyway-maven-plugin"),
                "pom.xml should have flyway-maven-plugin configured for mvn flyway:repair");
    }
}
