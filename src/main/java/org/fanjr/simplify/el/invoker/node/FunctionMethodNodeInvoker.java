package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.ArrayInvoker;
import org.fanjr.simplify.utils.ElUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 自定义函数执行器
 *
 * @author fanjr@vip.qq.com
 * @since 2023/5/30 下午15:28
 */
public class FunctionMethodNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MethodNodeInvoker.class);

    private static final Map<String, Supplier<Method>> METHOD_POOL = new ConcurrentHashMap<>();

    //用于获取方法参数的EL，返回结果必须是数组
    private final ArrayInvoker parameterEl;
    private final String methodName;

    private final Class<?> invokeInstance;

    private FunctionMethodNodeInvoker(String className, String methodName, ArrayInvoker parameterEl) {
        super(className);
        Class<?> instance;
        try {
            instance = Class.forName(className);
        } catch (Exception e) {
            instance = null;
        }
        this.invokeInstance = instance;
        this.methodName = methodName;
        this.parameterEl = parameterEl;
    }

    public static FunctionMethodNodeInvoker newInstance(String className, String methodName, ArrayInvoker parameterEl) {
        return new FunctionMethodNodeInvoker(className, methodName, parameterEl);
    }

    @Override
    public NodeHolder getNodeHolder(Object ctx) {
        if (null == invokeInstance) {
            return NodeHolder.newNodeHolder(null, null, this);
        }
        try {
            Object[] parameters = parameterEl.invoke(ctx).toArray();
            Method method = findMethod(invokeInstance, methodName, parameters.length);
            if (parameters.length == 0) {
                return NodeHolder.newNodeHolder(method.invoke(invokeInstance), null, this);
            } else {
                Type[] types = method.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = ElUtils.cast(parameters[i], types[i]);
                }
                return NodeHolder.newNodeHolder(method.invoke(invokeInstance, parameters), null, this);
            }
        } catch (Exception e) {
            throw new ElException(methodName + "执行失败！", e);
        }
    }

    @Override
    @Deprecated
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //SKIP
        return null;
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        // skip
        logger.info("移除【{}】操作无效，无需移除！", this);
    }

    @Override
    void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ElException("不可对【" + this + "】方法执行结果重新赋值！");
    }

    private static Method findMethod(Class<?> type, String methodName, int argNum) {
        String key = type.getName() + '#' + methodName + '@' + argNum;
        return METHOD_POOL.computeIfAbsent(key, k -> {
            try {
                if (0 == argNum) {
                    Method method = type.getMethod(methodName);
                    method.setAccessible(true);
                    return () -> method;
                } else {
                    Method[] methods = type.getMethods();
                    List<Method> targetMethods = new ArrayList<>();
                    for (Method method : methods) {
                        if (argNum == method.getParameterCount() && method.getName().equals(methodName)) {
                            targetMethods.add(method);
                        }
                    }
                    //存在多个方法，需要定位优先级
                    if (targetMethods.size() == 1) {
                        Method method = targetMethods.get(0);
                        method.setAccessible(true);
                        return () -> method;
                    } else if (targetMethods.size() > 1) {
                        // final标识的方法一定是目标方法
                        for (Method method : targetMethods) {
                            int mod = method.getModifiers();
                            if (Modifier.isFinal(mod)) {
                                method.setAccessible(true);
                                return () -> method;
                            }
                        }
                        // 优先使用非抽象方法
                        for (Method method : targetMethods) {
                            int mod = method.getModifiers();
                            if (!Modifier.isAbstract(mod)) {
                                method.setAccessible(true);
                                return () -> method;
                            }
                        }
                        Method method = targetMethods.get(0);
                        method.setAccessible(true);
                        return () -> method;
                    }
                    return () -> {
                        throw new ElException(methodName + "执行失败！找不到方法！");
                    };
                }
            } catch (Exception e) {
                return () -> {
                    throw new ElException(methodName + "执行失败！", e);
                };
            }
        }).get();
    }

    @Override
    public String toString() {
        return "Function:" + super.toString();
    }
}
