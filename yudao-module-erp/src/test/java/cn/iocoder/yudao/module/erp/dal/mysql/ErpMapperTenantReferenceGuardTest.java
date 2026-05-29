package cn.iocoder.yudao.module.erp.dal.mysql;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ErpMapperTenantReferenceGuardTest {

    private static final String FORBIDDEN_REFERENCE =
            "cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder";

    @Test
    void shouldNotReferenceTenantContextHolderInWorkspaceMainResourceXml() throws IOException {
        Path repoRoot = findRepoRoot();
        List<Path> resourceDirs = findMainResourceDirs(repoRoot);
        List<Path> offenders = new ArrayList<>();

        for (Path resourceDir : resourceDirs) {
            try (Stream<Path> pathStream = Files.walk(resourceDir)) {
                List<Path> xmlFiles = pathStream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".xml"))
                        .collect(Collectors.toList());
                for (Path xmlFile : xmlFiles) {
                    if (Files.readString(xmlFile, StandardCharsets.UTF_8).contains(FORBIDDEN_REFERENCE)) {
                        offenders.add(repoRoot.relativize(xmlFile));
                    }
                }
            }
        }

        assertThat(resourceDirs)
                .as("workspace should expose src/main/resources directories")
                .isNotEmpty();
        assertThat(offenders)
                .as("workspace main resource xml files should not reference TenantContextHolder via OGNL")
                .isEmpty();
    }

    private static Path findRepoRoot() throws IOException {
        Path current = Path.of("").toAbsolutePath().normalize();
        while (current != null) {
            Path pomPath = current.resolve("pom.xml");
            if (Files.isRegularFile(pomPath)
                    && Files.readString(pomPath, StandardCharsets.UTF_8).contains("<artifactId>yudao</artifactId>")) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Cannot locate repository root from current test execution directory.");
    }

    private static List<Path> findMainResourceDirs(Path repoRoot) throws IOException {
        try (Stream<Path> pathStream = Files.walk(repoRoot, 5)) {
            return pathStream
                    .filter(Files::isDirectory)
                    .filter(path -> path.endsWith(Path.of("src", "main", "resources")))
                    .filter(path -> !path.toString().contains("\\target\\"))
                    .collect(Collectors.toList());
        }
    }

}
