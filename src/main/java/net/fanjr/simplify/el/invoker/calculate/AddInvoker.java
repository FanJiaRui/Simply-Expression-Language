package net.fanjr.simplify.el.invoker.calculate;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.ElUtils;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午12:48
 */
public class AddInvoker extends BinocularInvoker {

    private AddInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("+", stack, new AddInvoker());
    }


    @Override
    protected Object doOperation(Object val1, Object val2) {
        if (null == val1) {
            return val2;
        }
        if (null == val2) {
            return val1;
        }

        //左右两边均为数值时按数值加法计算，其他情况均按字符串拼接处理。
        if (val1 instanceof Number && val2 instanceof Number) {
            BigDecimal num1 = ElUtils.castToBigDecimal(val1);
            BigDecimal num2 = ElUtils.castToBigDecimal(val2);
            return num1.add(num2);
        }

        String str1 = String.valueOf(val1);
        String str2 = String.valueOf(val2);
        return str1 + str2;
    }

}
