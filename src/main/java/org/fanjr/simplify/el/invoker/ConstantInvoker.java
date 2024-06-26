package org.fanjr.simplify.el.invoker;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.cache.ConcurrentCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/5 上午11:31
 */
public class ConstantInvoker implements ELInvoker {

    private static final ConcurrentCache<String, ConstantInvoker> POOL = new ConcurrentCache<>(10000);

    private final Object constant;

    private final String elString;

    private ConstantInvoker(String elString, Object constant) {
        this.elString = elString;
        this.constant = constant;
    }

    public static ELInvoker newInstance(String key, Object value) {
        return POOL.computeIfAbsent(key, k -> new ConstantInvoker(key, value));
    }

    @Override
    public Object invoke(Object ctx) {
        return constant;
    }

    @Override
    public String toString() {
        return elString;
    }

    @Override
    public void accept(ELVisitor visitor) {
        visitor.visit(this);
    }
}
