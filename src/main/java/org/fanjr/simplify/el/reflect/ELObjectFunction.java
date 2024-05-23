package org.fanjr.simplify.el.reflect;

import java.lang.reflect.Method;
import java.util.Objects;

public class ELObjectFunction extends ELBaseFunction {

    public ELObjectFunction(Method method) {
        super(method);
    }

    /**
     * 执行方法
     *
     * @param instance 执行方法的实例
     * @param ctx      上下文
     * @param args     调用方法的参数
     * @return 执行结果
     */
    public Object invoke(Object instance, Object ctx, Object... args) {
        return invokeByInstance(instance, ctx, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ELObjectFunction)) return false;
        return Objects.equals(method, ((ELObjectFunction) o).method);
    }


    @Override
    public int hashCode() {
        return Objects.hash(method.toGenericString());
    }

}
