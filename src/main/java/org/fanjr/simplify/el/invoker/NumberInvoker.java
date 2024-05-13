package org.fanjr.simplify.el.invoker;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.cache.ConcurrentCache;
import org.fanjr.simplify.utils.Pair;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/6/29 上午10:58
 */
public class NumberInvoker implements ELInvoker {
    private static final ConcurrentCache<String, NumberInvoker> POOL = new ConcurrentCache<>(10000);

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

    @Override
    public void accept(ELVisitor visitor) {
        visitor.visit(this);
    }
}
