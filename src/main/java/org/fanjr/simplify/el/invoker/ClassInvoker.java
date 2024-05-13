package org.fanjr.simplify.el.invoker;


import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.ELInvoker;

import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/13 下午3:48
 */
public class ClassInvoker implements ELInvoker {

    private final ELInvoker subInvoker;

    private ClassInvoker(ELInvoker subInvoker) {
        this.subInvoker = subInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new ClassInvoker(stack.removeLast());
    }

    @Override
    public Object invoke(Object ctx) {
        String className = String.valueOf(subInvoker.invoke(ctx));
        try {
            return Class.forName(className);
        } catch (Exception e) {
            throw new ElException("找不到类：" + className, e);
        }
    }

    @Override
    public String toString() {
        return "((class) " + subInvoker.toString() + ")";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            subInvoker.accept(visitor);
        }
    }
}