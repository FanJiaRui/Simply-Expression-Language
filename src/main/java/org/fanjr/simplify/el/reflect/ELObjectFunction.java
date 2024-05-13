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
     * @param instance
     * @param ctx
     * @param args
     * @return
     */
    public Object invoke(Object instance, Object ctx, Object... args) {
        return invokeByInstance(instance, ctx, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ELObjectFunction)) return false;
        ELObjectFunction elInnerFunction = (ELObjectFunction) o;
        return Objects.equals(method, elInnerFunction.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method);
    }
}
