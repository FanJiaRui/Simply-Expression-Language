package org.fanjr.simplify.el.invoker;

import org.fanjr.simplify.el.ELInvoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午4:27
 */
public class StringInvoker implements ELInvoker {
    private static final Map<String, StringInvoker> POOL = new ConcurrentHashMap<>();

    private final String value;

    private StringInvoker(String value) {
        if (null == value) {
            this.value = "";
        } else {
            this.value = value.replace("\\\"", "\"").replace("\\'", "'");
        }
    }

    public static ELInvoker newInstance(String value) {
        return POOL.computeIfAbsent(value, StringInvoker::new);
    }

    @Override
    public Object invoke(Object ctx) {
        return value;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }
}
