package com.xiaomi.shopping.agent.orchestrator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * INTENT-001 / ARCH-001..003 架构约束测试。
 *
 * @author liyunyi
 */
class ArchitectureConstraintTest {

    @Test
    @DisplayName("INTENT-001/ARCH-001 意图识别逻辑仅在 orchestrator 模块")
    void shouldKeepIntentRecognitionOnlyInOrchestrator() throws Exception {
        assertTrue(Files.exists(Path.of("src/main/java/com/xiaomi/shopping/agent/orchestrator/intent")));
        assertNoForbiddenText(Path.of("../knowledge/src/main/java"), List.of("IntentRecognizer", "primaryIntent"));
        assertNoForbiddenText(Path.of("../shopping/src/main/java"), List.of("IntentRecognizer", "primaryIntent", "意图识别"));
    }

    @Test
    @DisplayName("ARCH-002 记忆实现仅在 orchestrator 模块")
    void shouldKeepMemoryOnlyInOrchestrator() throws Exception {
        assertTrue(Files.exists(Path.of("src/main/java/com/xiaomi/shopping/agent/orchestrator/memory")));
        assertNoForbiddenText(Path.of("../knowledge/src/main/java"), List.of("LongTermMemory", "ShortTermMemory", "MessageArchive"));
        assertNoForbiddenText(Path.of("../shopping/src/main/java"), List.of("LongTermMemory", "ShortTermMemory", "MessageArchive"));
    }

    @Test
    @DisplayName("ARCH-003 orchestrator 不直接依赖 knowledge/shopping 实现包")
    void shouldNotImportChildImplementations() throws Exception {
        assertNoForbiddenText(Path.of("src/main/java"), List.of(
                "import com.xiaomi.shopping.agent.knowledge",
                "import com.xiaomi.shopping.agent.shopping"
        ));
        String pom = Files.readString(Path.of("pom.xml"));
        assertFalse(pom.contains("<artifactId>knowledge</artifactId>"));
        assertFalse(pom.contains("<artifactId>shopping</artifactId>"));
    }

    private void assertNoForbiddenText(Path root, List<String> forbidden) throws Exception {
        if (!Files.exists(root)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(root)) {
            List<Path> offenders = paths
                    .filter(path -> path.toString().endsWith(".java") || path.toString().endsWith("pom.xml"))
                    .filter(path -> containsAny(path, forbidden))
                    .toList();
            assertTrue(offenders.isEmpty(), "发现架构约束违规文件: " + offenders);
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
