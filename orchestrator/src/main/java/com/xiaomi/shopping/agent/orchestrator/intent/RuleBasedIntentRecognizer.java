package com.xiaomi.shopping.agent.orchestrator.intent;

import com.xiaomi.shopping.agent.common.contract.IntentResult;
import com.xiaomi.shopping.agent.common.contract.SessionSnapshot;
import com.xiaomi.shopping.agent.orchestrator.entityextract.OrchestratorEntityExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 规则版意图识别器，默认用于本地与自动化测试。
 *
 * @author liyunyi
 */
@Component
@RequiredArgsConstructor
public class RuleBasedIntentRecognizer implements IntentRecognizer {

    private static final Pattern QUANTITY_PATTERN = Pattern.compile("([一二两三四五六七八九十\\d]+)\\s*(台|个|件)?");
    private static final Pattern SPEC_PATTERN = Pattern.compile("\\d{1,2}\\s*(GB)?\\s*[+＋]\\s*(\\d{2,4}|\\dT|\\dTB)", Pattern.CASE_INSENSITIVE);
    private static final Pattern ORDER_PATTERN = Pattern.compile("(order|ORD|订单)[-_]?[0-9A-Za-z]{3,}");

    private final OrchestratorEntityExtractor entityExtractor;

    @Value("${xiaomi.agent.intent.confidence-threshold:0.6}")
    private double confidenceThreshold;

    @Override
    public IntentResult recognize(String userInput, SessionSnapshot snapshot) {
        String text = userInput == null ? "" : userInput.trim();
        Set<String> entities = entityExtractor.extract(text);
        Map<String, Object> slots = extractSlots(text, snapshot);
        if (text.isBlank() || isAmbiguous(text)) {
            return build(IntentResult.IntentType.KNOWLEDGE, "意图澄清", 0.3, true, entities, slots);
        }
        if (isSystemCommand(text)) {
            String secondary = containsAny(text, "清除", "删除", "忘记") ? "记忆清除" : "历史回显";
            return build(IntentResult.IntentType.SYSTEM, secondary, 0.92, false, entities, slots);
        }
        if (isHybridKnowledgeThenShopping(text)) {
            return build(IntentResult.IntentType.KNOWLEDGE, "商品推荐", 0.86, false, entities, slots);
        }
        if (containsAny(text, "加购", "加入购物车", "下单", "买", "物流", "订单", "库存")) {
            return build(IntentResult.IntentType.TOOL, secondaryToolIntent(text), 0.9, false, entities, slots);
        }
        if (containsAny(text, "规格", "参数", "影像", "性能", "续航", "屏幕", "处理器", "推荐", "对比", "比较", "怎么样", "优惠", "售后")) {
            return build(IntentResult.IntentType.KNOWLEDGE, secondaryKnowledgeIntent(text), 0.88, false, entities, slots);
        }
        return build(IntentResult.IntentType.KNOWLEDGE, "意图澄清", confidenceThreshold - 0.1, true, entities, slots);
    }

    private IntentResult build(IntentResult.IntentType type, String secondaryIntent, double confidence,
                               boolean needClarify, Set<String> entities, Map<String, Object> slots) {
        return IntentResult.builder()
                .primaryIntent(type)
                .secondaryIntent(secondaryIntent)
                .confidence(confidence)
                .needClarify(needClarify || confidence < confidenceThreshold)
                .entities(entities)
                .slots(slots)
                .build();
    }

    private boolean isAmbiguous(String text) {
        return text.length() <= 3 || Set.of("手机", "买手机", "小米").contains(text);
    }

    private boolean isSystemCommand(String text) {
        return text.contains("记忆") && containsAny(text, "清除", "删除", "忘记")
                || text.contains("历史") && containsAny(text, "查看", "看", "回显");
    }

    private String secondaryToolIntent(String text) {
        if (containsAny(text, "下单", "购买", "买")) {
            return "下单";
        }
        if (containsAny(text, "物流", "订单")) {
            return "物流查询";
        }
        if (containsAny(text, "库存")) {
            return "库存查询";
        }
        return "加购";
    }

    private boolean isHybridKnowledgeThenShopping(String text) {
        return containsAny(text, "推荐", "适合", "对比")
                && containsAny(text, "加购", "下单", "购买");
    }

    private String secondaryKnowledgeIntent(String text) {
        if (containsAny(text, "推荐")) {
            return "商品推荐";
        }
        if (containsAny(text, "对比", "比较")) {
            return "参数对比";
        }
        if (containsAny(text, "优惠")) {
            return "促销咨询";
        }
        return "参数咨询";
    }

    private Map<String, Object> extractSlots(String text, SessionSnapshot snapshot) {
        Map<String, Object> slots = new HashMap<>();
        String sku = extractSku(text);
        if (sku == null && snapshot != null && snapshot.getSelectedProducts() != null) {
            sku = snapshot.getSelectedProducts();
        }
        putIfPresent(slots, "skuId", sku);
        Matcher specMatcher = SPEC_PATTERN.matcher(text);
        if (specMatcher.find()) {
            putIfPresent(slots, "spec", specMatcher.group().replaceAll("\\s+", ""));
        }
        Integer quantity = extractQuantity(text);
        if (quantity != null) {
            slots.put("quantity", quantity);
        }
        if (containsAny(text, "武汉", "北京", "上海", "广州", "深圳", "洪山")) {
            slots.put("address", text);
        }
        Matcher orderMatcher = ORDER_PATTERN.matcher(text);
        if (orderMatcher.find()) {
            slots.put("orderId", orderMatcher.group());
        }
        return slots;
    }

    private String extractSku(String text) {
        if (text.contains("小米14")) {
            return "小米14";
        }
        if (text.contains("小米15")) {
            return "小米15";
        }
        if (text.contains("Redmi K70") || text.contains("K70")) {
            return "Redmi K70";
        }
        if (text.contains("小米13")) {
            return "小米13";
        }
        return null;
    }

    private Integer extractQuantity(String text) {
        if (!containsAny(text, "台", "个", "件")) {
            return containsAny(text, "加购", "加入购物车") ? 1 : null;
        }
        Matcher matcher = QUANTITY_PATTERN.matcher(text);
        if (matcher.find()) {
            return parseChineseNumber(matcher.group(1));
        }
        return 1;
    }

    private Integer parseChineseNumber(String raw) {
        return switch (raw) {
            case "一" -> 1;
            case "二", "两" -> 2;
            case "三" -> 3;
            case "四" -> 4;
            case "五" -> 5;
            case "六" -> 6;
            case "七" -> 7;
            case "八" -> 8;
            case "九" -> 9;
            case "十" -> 10;
            default -> {
                try {
                    yield Integer.parseInt(raw);
                } catch (NumberFormatException ex) {
                    yield 1;
                }
            }
        };
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private void putIfPresent(Map<String, Object> slots, String key, Object value) {
        if (value != null) {
            slots.put(key, value);
        }
    }
}
