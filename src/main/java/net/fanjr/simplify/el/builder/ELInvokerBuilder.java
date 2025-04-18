package net.fanjr.simplify.el.builder;


import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELInvoker;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午12:00
 */
public class ELInvokerBuilder implements Supplier<ELInvoker> {
    protected final LinkedList<ELInvoker> currStack = new LinkedList<>();
    protected final int needInvoker;
    protected final Function<LinkedList<ELInvoker>, ELInvoker> buildFunction;

    public ELInvokerBuilder(int needInvoker, Function<LinkedList<ELInvoker>, ELInvoker> buildFunction) {
        this.needInvoker = needInvoker;
        this.buildFunction = buildFunction;
    }

    public void pushInvoker(ELInvoker invoker) {
        currStack.addLast(invoker);
    }

    public boolean check() {
        return currStack.size() == needInvoker;
    }

    public int needNum() {
        return needInvoker - currStack.size();
    }

    @Override
    public ELInvoker get() {
        if (check()) {
            return buildFunction.apply(currStack);
        }
        throw new ELException("解析表达式发生异常！");
    }

}