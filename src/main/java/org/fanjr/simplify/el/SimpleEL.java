package org.fanjr.simplify.el;

/**
 * 简易EL表达式
 *
 * @author fanjr@vip.qq.com
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

    @Override
    public void accept(ELVisitor visitor) {
        // 访问自身
        if (visitor.visit(this)) {
            // 访问子节点
            invoker.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return "[EL: " + invoker.toString() + "]";
    }
}
