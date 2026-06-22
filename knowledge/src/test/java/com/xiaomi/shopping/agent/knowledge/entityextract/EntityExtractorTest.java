package com.xiaomi.shopping.agent.knowledge.entityextract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EntityExtractor 单测（架构.md §7 实体抽取，供命中信号比对）。
 *
 * @author liyunyi
 */
class EntityExtractorTest {

    private final EntityExtractor extractor = new EntityExtractor();

    @Test
    @DisplayName("抽取型号词：小米14 / Redmi K70")
    void shouldExtractModelEntities() {
        Set<String> entities = extractor.extract("小米14的影像怎么样，对比Redmi K70");
        assertTrue(entities.stream().anyMatch(e -> e.contains("14")), "应抽到 小米14: " + entities);
        assertTrue(entities.stream().anyMatch(e -> e.contains("70")), "应抽到 K70: " + entities);
    }

    @Test
    @DisplayName("抽取规格词：16+512 / 5000mAh")
    void shouldExtractSpecEntities() {
        Set<String> entities = extractor.extract("要16+512的版本，电池5000mAh");
        assertFalse(entities.isEmpty(), "应抽到规格词: " + entities);
        assertTrue(entities.stream().anyMatch(e -> e.contains("16")), "应含 16+512: " + entities);
    }

    @Test
    @DisplayName("空输入返回空集合")
    void shouldReturnEmptyForBlank() {
        assertTrue(extractor.extract(null).isEmpty());
        assertTrue(extractor.extract("").isEmpty());
        assertTrue(extractor.extract("你好").isEmpty());
    }
}
