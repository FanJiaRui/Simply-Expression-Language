package org.fanjr.simplify.el.invoker.calculate;

import com.hundsun.gaps.flowexecutor.el.ELInvoker;
import com.hundsun.gaps.flowexecutor.el.ElUtils;

import java.util.LinkedList;

/**
 * @author fanjr15662@hundsun.com
 * @file RemainderInvoker.java
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
        return ElUtils.castToBigDecimal(val1).divideAndRemainder(ElUtils.castToBigDecimal(val2))[1];
    }
}
