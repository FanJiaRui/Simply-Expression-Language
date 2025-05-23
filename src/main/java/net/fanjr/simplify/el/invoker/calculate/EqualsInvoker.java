package net.fanjr.simplify.el.invoker.calculate;

import com.alibaba.fastjson2.JSON;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.$;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;

import static net.fanjr.simplify.el.builder.KeywordBuilder.BLANK_FLAG;
import static net.fanjr.simplify.el.builder.KeywordBuilder.EMPTY_FLAG;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午4:38
 */
public class EqualsInvoker extends BinocularInvoker {

    private EqualsInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("==", stack, new EqualsInvoker());
    }

    public static ELInvoker buildNegationInstance(LinkedList<ELInvoker> stack) {
        return NegationInvoker.buildInstance(buildInstance(stack));
    }

    private static boolean isEmpty(Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof CharSequence) {
            return ((CharSequence) object).length() == 0;
        } else if (object.getClass().isArray()) {
            return Array.getLength(object) == 0;
        } else if (object instanceof Collection) {
            return ((Collection<?>) object).isEmpty();
        } else {
            return object instanceof Map<?, ?> && ((Map<?, ?>) object).isEmpty();
        }
    }

    private static boolean isBlank(Object obj) {
        if (null == obj || EMPTY_FLAG == obj || BLANK_FLAG == obj) {
            return true;
        }
        return $.isBlank(String.valueOf(obj));
    }

    @Override
    protected Object doOperation(Object eq1, Object eq2) {
        boolean objEquals = Objects.equals(eq1, eq2);
        if (objEquals) {
            return true;
        }

        //特殊
        if (BLANK_FLAG == eq1) {
            return isBlank(eq2);
        } else if (BLANK_FLAG == eq2) {
            return isBlank(eq1);
        } else if (EMPTY_FLAG == eq1) {
            return isEmpty(eq2);
        } else if (EMPTY_FLAG == eq2) {
            return isEmpty(eq1);
        }

        // 比较时将各类型转换为JSON字符串以比较不同类型的对象
        // TODO 这是临时的解决方案，后续应该考虑更具备性能的方案
        String str1;
        if (eq1 instanceof String) {
            str1 = (String) eq1;
        } else {
            str1 = JSON.toJSONString(eq1);
        }
        String str2;
        if (eq2 instanceof String) {
            str2 = (String) eq2;
        } else {
            str2 = JSON.toJSONString(eq2);
        }
        if ($.isNumber(str1)
                && $.isNumber(str2)) {
            //数字不比较精度
            BigDecimal bigDecimal1 = $.castToBigDecimal(eq1);
            BigDecimal bigDecimal2 = $.castToBigDecimal(eq2);
            return bigDecimal1.compareTo(bigDecimal2) == 0;
        }
        return Objects.equals(str1, str2);
    }

}
