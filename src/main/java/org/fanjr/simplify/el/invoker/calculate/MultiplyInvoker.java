package org.fanjr.simplify.el.invoker.calculate;


import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午8:38
 */
public class MultiplyInvoker extends BinocularInvoker {

    private MultiplyInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("*", stack, new MultiplyInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.multiply(num2);
    }
}
