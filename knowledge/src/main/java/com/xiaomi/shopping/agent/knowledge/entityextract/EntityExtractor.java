package com.xiaomi.shopping.agent.knowledge.entityextract;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 命中实体提取（架构.md §7 / 知识库Agent §7）。
 * <p>
 * 从用户问题抽取型号/规格实体（小米14 / Redmi K70 / 16+512 / 5000mAh），
 * 供主 Agent 质量判断的「命中实体」信号比对。
 *
 * @author liyunyi
 */
@Component
public class EntityExtractor {

    /** 型号词：字母+数字组合（小米14 / Redmi K70 / P60） */
    private static final Pattern MODEL_PATTERN =
            Pattern.compile("[A-Za-z\\u4e00-\\u9fa5]*\\d+[A-Za-z\\u4e00-\\u9fa5+]*");

    /** 规格词：数字+单位（16+512 / 5000mAh / 8GB） */
    private static final Pattern SPEC_PATTERN =
            Pattern.compile("\\d+\\+?\\d*\\s*(GB|MB|TB|mAh|G|T|K)?", Pattern.CASE_INSENSITIVE);

    /**
     * 抽取实体集合。
     */
    public Set<String> extract(String query) {
        Set<String> entities = new HashSet<>();
        if (query == null || query.isBlank()) {
            return entities;
        }
        Matcher modelMatcher = MODEL_PATTERN.matcher(query);
        while (modelMatcher.find()) {
            String g = modelMatcher.group().trim();
            if (g.length() >= 2) {
                entities.add(g);
            }
        }
        Matcher specMatcher = SPEC_PATTERN.matcher(query);
        while (specMatcher.find()) {
            String g = specMatcher.group().trim();
            if (g.length() >= 2 && g.matches(".*\\d.*")) {
                entities.add(g);
            }
        }
        return entities;
    }
}
