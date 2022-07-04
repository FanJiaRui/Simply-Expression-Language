package org.fanjr.simplify.el.invoker.calculate;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @file DivideInvoker.java
 * @since 2021/7/9 下午9:25
 */
public class DivideInvoker extends BinocularInvoker {

    private DivideInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("/", stack, new DivideInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.divide(num2, 8, BigDecimal.ROUND_HALF_UP);
    }
}
