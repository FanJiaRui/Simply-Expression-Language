package org.fanjr.simplify.el.invoker;


import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Type;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/8 下午5:25
 */
public class ConversionInvoker implements ELInvoker {

    private final String keyword;

    private final ELInvoker subInvoker;

    private final Type type;

    private ConversionInvoker(String keyword, ELInvoker subInvoker, Type type) {
        this.keyword = keyword;
        this.subInvoker = subInvoker;
        this.type = type;
    }

    public static ELInvoker buildInstance(String keyword, LinkedList<ELInvoker> stack, Type type) {
        return new ConversionInvoker(keyword, stack.removeLast(), type);
    }

    @Override
    public Object invoke(Object ctx) {
        return ElUtils.cast(subInvoker.invoke(ctx), type);
    }

    @Override
    public String toString() {
        return "(" + keyword + " " + subInvoker.toString() + ")";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            subInvoker.accept(visitor);
        }
    }
}
