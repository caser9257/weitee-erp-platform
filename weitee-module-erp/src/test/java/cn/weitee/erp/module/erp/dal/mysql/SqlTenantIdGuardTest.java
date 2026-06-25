package cn.weitee.erp.module.erp.dal.mysql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guard test: ensure no SQL migration file (except the de-tenantization migration itself)
 * contains {@code tenant_id} references. This prevents AI and developers from accidentally
 * re-introducing multi-tenant patterns when authoring new DDL / seed data.
 *
 * @see "AGENTS.md Section 16 - De-tenantization Rules"
 */
class SqlTenantIdGuardTest {

    private static final String MIGRATION_FILE = "137-tenant-id-merge-and-drop.sql";
    private static final String FORBIDDEN = "tenant_id";

    @Test
    void sqlFilesShouldNotContainTenantId() throws IOException {
        Path repoRoot = findRepoRoot();
        Path sqlDir = repoRoot.resolve("sql").resolve("mysql");
        assertThat(Files.isDirectory(sqlDir))
                .as("sql/mysql directory should exist")
                .isTrue();

        List<Path> offenders = new ArrayList<>();

        try (Stream<Path> pathStream = Files.walk(sqlDir)) {
            List<Path> sqlFiles = pathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .filter(path -> !path.getFileName().toString().equals(MIGRATION_FILE))
                    .filter(path -> !path.toString().contains("\\dm\\"))
                    .filter(path -> !path.toString().contains("\\oracle\\"))
                    .filter(path -> !path.toString().contains("\\postgresql\\"))
                    .filter(path -> !path.toString().contains("\\sqlserver\\"))
                    .filter(path -> !path.toString().contains("\\kingbase\\"))
                    .filter(path -> !path.toString().contains("\\opengauss\\"))
                    .filter(path -> !path.toString().contains("\\handoff-archive\\"))
                    .collect(Collectors.toList());

            for (Path sqlFile : sqlFiles) {
                String content = Files.readString(sqlFile, StandardCharsets.UTF_8);
                if (content.contains(FORBIDDEN)) {
                    offenders.add(repoRoot.relativize(sqlFile));
                }
            }
        }

        assertThat(offenders)
                .as("SQL migration files (excluding %s) should not contain '%s'. "
                        + "See AGENTS.md Section 16 for de-tenantization rules.", MIGRATION_FILE, FORBIDDEN)
                .isEmpty();
    }

    private static Path findRepoRoot() throws IOException {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path pomPath = current.resolve("pom.xml");
            Path sqlDir = current.resolve("sql").resolve("mysql");
            if (Files.isRegularFile(pomPath)
                    && Files.readString(pomPath, StandardCharsets.UTF_8).contains("<artifactId>weitee</artifactId>")
                    && Files.isDirectory(sqlDir)) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root from current test execution directory.");
    }

}
