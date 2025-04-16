package net.fanjr.simplify.el.reflect;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EL方法执行器
 *
 * @author fanjr@vip.qq.com
 * @since 1.1.1
 */
public class ELInnerFunction extends ELBaseFunction {

    private final Class<?> instance;

    public ELInnerFunction(Class<?> instance, Method method) {
        this(instance, method, 0);
    }

    public ELInnerFunction(Class<?> instance, Method method, int order) {
        this(instance.getTypeName(), method.getName(), instance, method, order);
    }

    public ELInnerFunction(String instanceName, String methodName, Class<?> instance, Method method, int order) {
        this(instanceName, methodName, instance, method, order, new ArrayList<>());
    }

    public ELInnerFunction(String instanceName, String methodName, Class<?> instance, Method method, int order, List<Class<? extends RuntimeException>> userDefinedExceptions) {
        super(instanceName, methodName, method, order, userDefinedExceptions);
        this.instance = instance;
    }

    public Object invoke(Object ctx, Object... args) {
        return invokeByInstance(instance, ctx, args);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ELInnerFunction)) return false;
        ELInnerFunction elInnerFunction = (ELInnerFunction) o;
        return order == elInnerFunction.order && Objects.equals(method, elInnerFunction.method) && Objects.equals(instanceName, elInnerFunction.instanceName) && Objects.equals(methodName, elInnerFunction.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, order, instanceName, methodName);
    }

}