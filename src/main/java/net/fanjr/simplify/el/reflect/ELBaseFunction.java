package net.fanjr.simplify.el.reflect;

import com.alibaba.fastjson2.JSON;
import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.utils.$;
import net.fanjr.simplify.utils.Pair;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 表达式基础JAVA方法执行器
 */
public abstract class ELBaseFunction implements Comparable<ELBaseFunction> {

    protected final Method method;
    protected final Type[] genericParameterTypes;
    protected final Class<?>[] parameterTypes;
    protected final List<Class<? extends RuntimeException>> userDefinedExceptions;
    protected final int order;
    protected final String instanceName;
    protected final String methodName;

    protected ELBaseFunction(Method method) {
        Pair<Class<?>[], Type[]> pair = ELFunctionInvokeUtils.getMethodParameters(method);
        this.methodName = method.getName();
        this.instanceName = null;
        this.parameterTypes = pair.k;
        this.genericParameterTypes = pair.v;

        this.method = method;
        this.userDefinedExceptions = new ArrayList<>();

        int mod = method.getModifiers();
        if (Modifier.isFinal(mod)) {
            // final标识的方法优先级较高
            this.order = -1;
        } else {
            this.order = 0;
        }

        // 可能存在方法为非public或方法所在类为非public的情况，需要强行设置accessible
        // 这里不关注是否为私有方法或者私有对象，使用这个类时根据实际情况进行判断是否将方法转换为ELFunction
        if (!Modifier.isPublic(mod) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }

    protected ELBaseFunction(String instanceName, String methodName, Method method, int order, List<Class<? extends RuntimeException>> userDefinedExceptions) {
        Pair<Class<?>[], Type[]> pair = ELFunctionInvokeUtils.getMethodParameters(method);
        this.methodName = methodName;
        this.instanceName = instanceName;
        this.parameterTypes = pair.k;
        this.genericParameterTypes = pair.v;
        this.method = method;
        this.order = order;
        this.userDefinedExceptions = userDefinedExceptions;

        // 可能存在方法为非public或方法所在类为非public的情况，需要强行设置accessible
        // 这里不关注是否为私有方法或者私有对象，使用这个类时根据实际情况进行判断是否将方法转换为ELFunction
        if (!Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }
    }

    /**
     * 调用JAVA方法
     *
     * @param instance 执行的对象
     * @param ctx      当前整体上下文，暂时没用上，后续可以考虑用于实现一些内置扩展
     * @param args     传入的参数
     * @return 执行结果
     */
    protected Object invokeByInstance(Object instance, Object ctx, Object... args) {
        try {
            Object[] target = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                target[i] = $.cast(args[i], genericParameterTypes[i]);
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

            // 判断是否指定了名称，未指定则信息以反射为准
            if (null != instanceName) {
                throw new ELException(instanceName + '.' + methodName + "(" + JSON.toJSONString(parameterTypes) + ")" + "执行失败！", throwTarget);
            } else {
                String name;
                if (instance instanceof Class) {
                    name = ((Class<?>) instance).getName();
                } else {
                    name = instance.getClass().getName();
                }
                throw new ELException(name + '.' + methodName + "(" + JSON.toJSONString(parameterTypes) + ")" + "执行失败！", throwTarget);
            }
        }
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

    @Override
    public int compareTo(ELBaseFunction o) {
        if (this.equals(o)) {
            // 结果比较相等，返回0
            return 0;
        }
        // 按排序
        int compare = Integer.compare(this.order, o.order);
        if (compare == 0) {
            // 排序相等时后面的覆盖前面的
            return 1;
        }
        return compare;
    }
}
