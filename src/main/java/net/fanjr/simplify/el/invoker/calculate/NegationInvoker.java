package net.fanjr.simplify.el.invoker.calculate;


import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.utils.SimplifyException;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author fanjr@vip.qq.com
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

        throw new SimplifyException("无法将类型" + targetObj.getClass() + "转换为boolean类型");
    }

    @Override
    public String toString() {
        return "(!" + subInvoker.toString() + ")";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            subInvoker.accept(visitor);
        }
    }
}
