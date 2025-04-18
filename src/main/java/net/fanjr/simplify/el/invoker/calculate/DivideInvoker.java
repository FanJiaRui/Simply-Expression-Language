package net.fanjr.simplify.el.invoker.calculate;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.$;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午9:25
 */
public class DivideInvoker extends BinocularInvoker {

    /**
     * 用于兼容JDK9之后的BigDecimal.ROUND_HALF_UP常量
     */
    private static final int ROUND_HALF_UP = 4;

    private DivideInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return buildInstance("/", stack, new DivideInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        BigDecimal num1 = $.castToBigDecimal(val1);
        BigDecimal num2 = $.castToBigDecimal(val2);
        return num1.divide(num2, 8, ROUND_HALF_UP);
    }
}
