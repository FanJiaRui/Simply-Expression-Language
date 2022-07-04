package org.fanjr.simplify.el;

/**
 * @author fanjr@vip.qq.com
 * @file SimpleEL.java
 * @since 2021/6/28 下午4:08
 */
public class SimpleEL implements EL {

    private final ELInvoker invoker;

    public SimpleEL(ELInvoker invoker) {
        this.invoker = invoker;
    }

    @Override
    public Object invoke(Object ctx) {
        return invoker.invoke(ctx);
    }
}
