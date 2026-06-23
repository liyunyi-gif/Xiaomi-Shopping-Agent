package com.xiaomi.shopping.agent.orchestrator.intent;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.orchestrator.entityextract.OrchestratorEntityExtractor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * INTENT-002..006 意图识别测试。
 *
 * @author liyunyi
 */
class RuleBasedIntentRecognizerTest {

    private final RuleBasedIntentRecognizer recognizer = new RuleBasedIntentRecognizer(new OrchestratorEntityExtractor());

    @Test
    @DisplayName("INTENT-002 知识库问答意图分类")
    void shouldRecognizeKnowledgeIntent() {
        IntentResult result = recognizer.recognize("小米14的影像规格怎么样", null);

        assertEquals(IntentResult.IntentType.KNOWLEDGE, result.getPrimaryIntent());
        assertEquals("参数咨询", result.getSecondaryIntent());
        assertTrue(result.getEntities().contains("小米14"));
        assertTrue(result.getEntities().contains("影像"));
    }

    @Test
    @DisplayName("INTENT-003 工具调用意图分类并抽取槽位")
    void shouldRecognizeToolIntent() {
        IntentResult result = recognizer.recognize("帮我加购一台小米14 16+512", null);

        assertEquals(IntentResult.IntentType.TOOL, result.getPrimaryIntent());
        assertEquals("加购", result.getSecondaryIntent());
        assertEquals("小米14", result.getSlots().get("skuId"));
        assertEquals("16+512", result.getSlots().get("spec"));
        assertEquals(1, result.getSlots().get("quantity"));
    }

    @Test
    @DisplayName("INTENT-004 系统指令由主 Agent 自处理")
    void shouldRecognizeSystemIntent() {
        IntentResult result = recognizer.recognize("清除我的记忆", null);

        assertEquals(IntentResult.IntentType.SYSTEM, result.getPrimaryIntent());
        assertEquals("记忆清除", result.getSecondaryIntent());
        assertFalse(result.isNeedClarify());
    }

    @Test
    @DisplayName("INTENT-005 低置信度触发澄清")
    void shouldClarifyLowConfidenceIntent() {
        IntentResult result = recognizer.recognize("手机", null);

        assertTrue(result.isNeedClarify());
    }

    @Test
    @DisplayName("INTENT-006 下单意图明确但槽位可交由 Shopping 举手")
    void shouldRecognizeOrderIntentWithMissingSlots() {
        IntentResult result = recognizer.recognize("帮我下单", null);

        assertEquals(IntentResult.IntentType.TOOL, result.getPrimaryIntent());
        assertEquals("下单", result.getSecondaryIntent());
        assertFalse(result.isNeedClarify());
        assertTrue(result.getSlots().isEmpty());
    }
}
