package com.xiaomi.shopping.agent.shopping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Shopping 架构边界测试（对应 test/08 ARCH-001/002/003）。
 *
 * @author liyunyi
 */
class ArchitectureConstraintTest {

    private static final Path SHOPPING_SRC = Path.of("src/main/java");

    @Test
    @DisplayName("ARCH-003 Shopping 不通过 Java 依赖引用 mcpserver 包")
    void shouldNotImportMcpserverPackage() throws IOException {
        assertNoSourceContains("com.xiaomi.shopping.agent.mcpserver");
    }

    @Test
    @DisplayName("ARCH-001/002 Shopping 不使用 LLM/记忆组件")
    void shouldNotUseLlmOrMemoryComponents() throws IOException {
        assertNoSourceContains("ChatClient");
        assertNoSourceContains("OpenAiChatModel");
        assertNoSourceContains("ChatMemory");
        assertNoSourceContains("RedisTemplate");
    }

    private void assertNoSourceContains(String forbidden) throws IOException {
        try (Stream<Path> paths = Files.walk(SHOPPING_SRC)) {
            List<Path> offenders = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> !path.getFileName().toString().equals("ArchitectureConstraintTest.java"))
                    .filter(path -> contains(path, forbidden))
                    .toList();
            assertFalse(!offenders.isEmpty(), "Shopping 源码不应包含: " + forbidden + " offenders=" + offenders);
        }
    }

    private boolean contains(Path path, String text) {
        try {
            return Files.readString(path, StandardCharsets.UTF_8).contains(text);
        } catch (IOException e) {
            return false;
        }
    }
}
