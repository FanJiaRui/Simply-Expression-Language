package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.$;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
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
        BigDecimal num1 = $.castToBigDecimal(val1);
        BigDecimal num2 = $.castToBigDecimal(val2);
        return num1.compareTo(num2) <= 0;
    }
}
