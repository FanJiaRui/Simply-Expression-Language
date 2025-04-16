package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午7:07
 */
public class SubtractInvoker extends BinocularInvoker {

    private SubtractInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("-", stack, new SubtractInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = ElUtils.castToBigDecimal(val1);
        BigDecimal num2 = ElUtils.castToBigDecimal(val2);
        return num1.subtract(num2);
    }
}
