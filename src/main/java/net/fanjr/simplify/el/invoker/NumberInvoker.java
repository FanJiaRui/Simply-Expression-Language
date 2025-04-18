package net.fanjr.simplify.el.invoker;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.utils.SimplifyCache;

import java.math.BigDecimal;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/6/29 上午10:58
 */
public class NumberInvoker implements ELInvoker {
    private static final SimplifyCache<String, NumberInvoker> POOL = new SimplifyCache<>(10000);

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
