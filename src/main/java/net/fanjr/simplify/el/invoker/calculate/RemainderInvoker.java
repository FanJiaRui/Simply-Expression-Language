package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.$;

import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午2:47
 */
public class RemainderInvoker extends BinocularInvoker {

    private RemainderInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("%", stack, new RemainderInvoker());
    }

    @Override
    protected Object doOperation(Object val1, Object val2) {
        return $.castToBigDecimal(val1).divideAndRemainder($.castToBigDecimal(val2))[1];
    }
}
