package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/13 下午8:17
 */
public class GreaterInvoker extends BinocularInvoker {

    private GreaterInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance(">", stack, new GreaterInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.compareTo(num2) > 0;
    }
}
