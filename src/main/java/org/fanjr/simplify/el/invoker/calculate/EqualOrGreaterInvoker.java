package org.fanjr.simplify.el.invoker.calculate;


import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr15662@hundsun.com
 * @file EqualOrGreaterInvoker.java
 * @since 2021/7/13 下午8:13
 */
public class EqualOrGreaterInvoker extends BinocularInvoker {

    private EqualOrGreaterInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance(">=", stack, new EqualOrGreaterInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.compareTo(num2) >= 0;
    }
}