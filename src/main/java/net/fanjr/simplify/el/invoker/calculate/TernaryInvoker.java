package net.fanjr.simplify.el.invoker.calculate;

import com.alibaba.fastjson2.util.TypeUtils;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;

import java.util.LinkedList;

/**
 * 三元运算
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/30 下午2:57
 */
public class TernaryInvoker implements ELInvoker {

    private final ELInvoker exp;

    private final ELInvoker first;

    private final ELInvoker second;

    private TernaryInvoker(ELInvoker exp, ELInvoker first, ELInvoker second) {
        this.exp = exp;
        this.first = first;
        this.second = second;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new TernaryInvoker(stack.pollFirst(), stack.pollFirst(), stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        if (TypeUtils.cast(exp.invoke(ctx), boolean.class)) {
            return first.invoke(ctx);
        } else {
            return second.invoke(ctx);
        }
    }

    @Override
    public String toString() {
        return "(" + exp.toString() + "?" + first.toString() + ":" + second.toString() + ")";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            exp.accept(visitor);
            first.accept(visitor);
            second.accept(visitor);
        }
    }
}
