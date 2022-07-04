package org.fanjr.simplify.el.invoker.calculate;


import com.alibaba.fastjson2.util.TypeUtils;
import org.fanjr.simplify.el.ELInvoker;

import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @file OrInvoker.java
 * @since 2021/7/9 下午3:58
 */
public class OrInvoker extends BinocularInvoker {

    private OrInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("||", stack, new OrInvoker());
    }

    @Override
    public Object invoke(Object ctx) {
        Object val1 = invokeVal1(ctx);
        boolean b1 = null != val1 && TypeUtils.cast(val1, boolean.class);
        if (b1) {
            return true;
        }
        Object val2 = invokeVal2(ctx);
        return null != val2 && TypeUtils.cast(val2, boolean.class);
    }

    @Override
    @Deprecated
    protected Object doOperation(Object val1, Object val2) {
        //skip
        return null;
    }
}
