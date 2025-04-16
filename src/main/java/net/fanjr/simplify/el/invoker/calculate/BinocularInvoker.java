package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;

import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/8 下午5:39
 */
public abstract class BinocularInvoker implements ELInvoker {

    protected ELInvoker el1;

    protected ELInvoker el2;

    protected String optStr;

    public static BinocularInvoker buildInstance(String optStr, LinkedList<ELInvoker> stack, BinocularInvoker binocularInvoker) {
        binocularInvoker.el1 = stack.pollFirst();
        binocularInvoker.el2 = stack.pollFirst();
        binocularInvoker.optStr = optStr;
        return binocularInvoker;
    }

    @Override
    public Object invoke(Object ctx) {
        return doOperation(invokeVal1(ctx), invokeVal2(ctx));
    }

    protected Object invokeVal1(Object ctx) {
        return el1.invoke(ctx);
    }

    protected Object invokeVal2(Object ctx) {
        return el2.invoke(ctx);
    }

    protected abstract Object doOperation(Object val1, Object val2);

    @Override
    public String toString() {
        return "(" + el1.toString() + " " + optStr + " " + el2.toString() + ")";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            el1.accept(visitor);
            el2.accept(visitor);
        }
    }
}
