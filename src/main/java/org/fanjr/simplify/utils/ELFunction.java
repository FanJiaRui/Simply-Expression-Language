package org.fanjr.simplify.utils;

import com.alibaba.fastjson2.JSON;
import org.fanjr.simplify.el.ElException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EL方法执行器
 *
 * @author fanjr@vip.qq.com
 * @since 1.1.1
 */
public class ELFunction implements Comparable<ELFunction> {

    private final Object instance;
    private final Method method;
    private final Type[] genericParameterTypes;
    private final Class<?>[] parameterTypes;
    private final List<Class<? extends RuntimeException>> userDefinedExceptions;
    private final int order;
    private final String instanceName;
    private final String methodName;

    public ELFunction(Object instance, Method method) {
        this(instance, method, 0);

    }

    public ELFunction(Object instance, Method method, int order) {
        this(instance.getClass().getTypeName(), method.getName(), instance, method, order);
    }

    public ELFunction(String instanceName, String methodName, Object instance, Method method, int order) {
        this(instanceName, methodName, instance, method, order, new ArrayList<>());
    }

    public ELFunction(String instanceName, String methodName, Object instance, Method method, int order, List<Class<? extends RuntimeException>> userDefinedExceptions) {
        Pair<Class<?>[], Type[]> pair = ELMethodInvokeUtils.getMethodParameters(method);
        this.methodName = methodName;
        this.instanceName = instanceName;
        this.parameterTypes = pair.k;
        this.genericParameterTypes = pair.v;
        this.method = method;
        this.instance = instance;
        this.order = order;
        this.userDefinedExceptions = userDefinedExceptions;
    }


    public int getOrder() {
        return order;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getParameterCount() {
        return parameterTypes.length;
    }

    public boolean match(Object... args) {
        if (args.length != parameterTypes.length) {
            return false;
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (null != args[i] && !parameterTypes[i].isInstance(args[i])) {
                // 既不是null，也非参数子类
                return false;
            }
        }
        // 传入值均匹配(null或相关类型子类)
        return true;
    }

    public Object invoke(Object... args) {
        try {
            Object[] target = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                target[i] = ElUtils.cast(args[i], genericParameterTypes[i]);
            }
            return method.invoke(instance, target);
        } catch (Exception e) {
            // 这里需要计算是否需要包装异常
            Throwable throwTarget;
            if (e instanceof InvocationTargetException) {
                Throwable targetException = ((InvocationTargetException) e).getTargetException();
                if (targetException instanceof RuntimeException) {
                    for (Class<? extends Exception> ue : userDefinedExceptions) {
                        if (ue.isInstance(targetException)) {
                            // 用户自定义异常，跳过
                            throw (RuntimeException) targetException;
                        }
                    }
                }
                throwTarget = ((InvocationTargetException) e).getTargetException();
            } else {
                throwTarget = e;
            }

            throw new ElException(instanceName + '.' + methodName + "(" + JSON.toJSONString(parameterTypes) + ")" + "执行失败！", throwTarget);
        }
    }

    @Override
    public int compareTo(ELFunction o) {
        return Integer.compare(this.order, o.order);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ELFunction)) return false;
        ELFunction elFunction = (ELFunction) o;
        return order == elFunction.order && Objects.equals(method, elFunction.method) && Objects.equals(instanceName, elFunction.instanceName) && Objects.equals(methodName, elFunction.methodName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, order, instanceName, methodName);
    }
}