package org.fanjr.simplify.el.invoker.calculate;


import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.fanjr.simplify.context.ContextException;
import org.fanjr.simplify.el.ELInvoker;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author fanjr15662@hundsun.com
 * @file NegationInvoker.java
 * @since 2021/6/29 上午11:19
 */
public class NegationInvoker implements ELInvoker {

    private final ELInvoker subInvoker;

    private NegationInvoker(ELInvoker subInvoker) {
        this.subInvoker = subInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new NegationInvoker(stack.poll());
    }

    public static ELInvoker buildInstance(ELInvoker subInvoker) {
        return new NegationInvoker(subInvoker);
    }

    @Override
    public Object invoke(Object ctx) {
        Object targetObj = subInvoker.invoke(ctx);
        if (targetObj == null) {
            return true;
        }
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(targetObj.getClass(), boolean.class);
        if (typeConvert != null) {
            return !(boolean) typeConvert.apply(targetObj);
        }

        throw new ContextException("无法将类型" + targetObj.getClass() + "转换为boolean类型");
    }

    @Override
    public String toString() {
        return "(!" + subInvoker.toString() + ")";
    }
}
