package org.fanjr.simplify.el.invoker.calculate;


import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr15662@hundsun.com
 * @file EqualOrLessInvoker.java
 * @since 2021/7/13 下午8:19
 */
public class EqualOrLessInvoker extends BinocularInvoker {

    private EqualOrLessInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("<=", stack, new EqualOrLessInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.compareTo(num2) <= 0;
    }
}
