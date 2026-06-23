package com.xiaomi.shopping.agent.shopping.orchestration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Shopping 槽位读取工具。
 *
 * @author liyunyi
 */
public final class ShoppingSlotUtils {

    private ShoppingSlotUtils() {
    }

    public static String stringSlot(Map<String, Object> slots, String... names) {
        Object value = first(slots, names);
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    public static Integer intSlot(Map<String, Object> slots, Integer defaultValue, String... names) {
        Object value = first(slots, names);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static List<Object> listSlot(Map<String, Object> slots, String... names) {
        Object value = first(slots, names);
        if (value == null) {
            return List.of();
        }
        if (value instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }
        return List.of(value);
    }

    public static Object first(Map<String, Object> slots, String... names) {
        if (slots == null || names == null) {
            return null;
        }
        for (String name : names) {
            if (slots.containsKey(name)) {
                return slots.get(name);
            }
        }
        return null;
    }
}
