package com.xiaomi.shopping.agent.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ARCH-003 多模块边界测试。
 *
 * @author liyunyi
 */
class ArchitectureBoundaryIntegrationTest {

    private static final Path ROOT = Path.of("..").toAbsolutePath().normalize();

    @Test
    @DisplayName("ARCH-003 三节点 Maven 依赖保持正交，bootstrap 聚合")
    void shouldKeepMavenModuleBoundaries() throws Exception {
        String orchestratorPom = Files.readString(ROOT.resolve("orchestrator/pom.xml"));
        String knowledgePom = Files.readString(ROOT.resolve("knowledge/pom.xml"));
        String shoppingPom = Files.readString(ROOT.resolve("shopping/pom.xml"));
        String bootstrapPom = Files.readString(ROOT.resolve("bootstrap/pom.xml"));

        assertFalse(orchestratorPom.contains("<artifactId>knowledge</artifactId>"));
        assertFalse(orchestratorPom.contains("<artifactId>shopping</artifactId>"));
        assertFalse(knowledgePom.contains("<artifactId>shopping</artifactId>"));
        assertFalse(shoppingPom.contains("<artifactId>knowledge</artifactId>"));
        assertTrue(bootstrapPom.contains("<artifactId>orchestrator</artifactId>"));
        assertTrue(bootstrapPom.contains("<artifactId>knowledge</artifactId>"));
        assertTrue(bootstrapPom.contains("<artifactId>shopping</artifactId>"));
    }

    @Test
    @DisplayName("ARCH-003 Java 包不出现 knowledge/shopping 互相 import，shopping 不 import mcpserver")
    void shouldKeepJavaPackageBoundaries() throws Exception {
        assertNoForbiddenText(ROOT.resolve("knowledge/src/main/java"), List.of(
                "import com.xiaomi.shopping.agent.shopping",
                "import com.xiaomi.shopping.agent.orchestrator"
        ));
        assertNoForbiddenText(ROOT.resolve("shopping/src/main/java"), List.of(
                "import com.xiaomi.shopping.agent.knowledge",
                "import com.xiaomi.shopping.agent.orchestrator",
                "import com.xiaomi.shopping.agent.mcpserver"
        ));
        assertNoForbiddenText(ROOT.resolve("orchestrator/src/main/java"), List.of(
                "import com.xiaomi.shopping.agent.knowledge",
                "import com.xiaomi.shopping.agent.shopping"
        ));
    }

    private void assertNoForbiddenText(Path root, List<String> forbidden) throws Exception {
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> offenders = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> containsAny(path, forbidden))
                    .toList();
            assertTrue(offenders.isEmpty(), "发现模块边界违规: " + offenders);
        }
    }

    private boolean containsAny(Path path, List<String> forbidden) {
        try {
            String content = Files.readString(path);
            return forbidden.stream().anyMatch(content::contains);
        } catch (Exception ex) {
            return false;
        }
    }
}
