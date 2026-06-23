package com.xiaomi.shopping.agent.orchestrator.entityextract;

import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 主 Agent 实体抽取器。
 *
 * @author liyunyi
 */
@Component
public class OrchestratorEntityExtractor {

    private static final Pattern SPEC_PATTERN = Pattern.compile("\\d{1,2}\\s*(GB)?\\s*[+＋]\\s*(\\d{2,4}GB|\\dT|\\dTB)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_PATTERN = Pattern.compile("(order|ORD|订单)[-_]?[0-9A-Za-z]{3,}");

    public Set<String> extract(String text) {
        Set<String> entities = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return entities;
        }
        addIfContains(text, entities, "小米14");
        addIfContains(text, entities, "小米15");
        addIfContains(text, entities, "小米13");
        addIfContains(text, entities, "Redmi K70");
        addIfContains(text, entities, "K70");
        addIfContains(text, entities, "SU7");
        addIfContains(text, entities, "影像");
        addIfContains(text, entities, "续航");
        addIfContains(text, entities, "屏幕");
        addIfContains(text, entities, "性能");
        addIfContains(text, entities, "处理器");

        Matcher specMatcher = SPEC_PATTERN.matcher(text);
        while (specMatcher.find()) {
            entities.add(specMatcher.group().replaceAll("\\s+", ""));
        }
        Matcher orderMatcher = ORDER_PATTERN.matcher(text);
        while (orderMatcher.find()) {
            entities.add(orderMatcher.group());
        }
        return entities;
    }

    private void addIfContains(String text, Set<String> entities, String candidate) {
        if (text.contains(candidate)) {
            entities.add(candidate);
        }
    }
}
