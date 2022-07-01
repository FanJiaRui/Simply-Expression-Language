package org.fanjr.simplify.el.invoker;

import org.fanjr.simplify.el.ELInvoker;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr15662@hundsun.com
 * @file NumberInvoker.java
 * @since 2021/6/29 上午10:58
 */
public class NumberInvoker implements ELInvoker {
    private static final Map<String, NumberInvoker> POOL = new ConcurrentHashMap<>();

    private final BigDecimal value;

    private NumberInvoker(String value) {
        this.value = new BigDecimal(value);
    }

    public static ELInvoker newInstance(String value) {
        return POOL.computeIfAbsent(value, NumberInvoker::new);
    }

    @Override
    public BigDecimal invoke(Object ctx) {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
