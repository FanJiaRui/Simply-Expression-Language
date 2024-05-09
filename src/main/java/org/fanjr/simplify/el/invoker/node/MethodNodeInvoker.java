package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.util.BeanUtils;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.ArrayInvoker;
import org.fanjr.simplify.utils.ElUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/7 下午2:43
 */
public class MethodNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MethodNodeInvoker.class);

    private static final Map<String, Supplier<Method>> METHOD_POOL = new ConcurrentHashMap<>();

    private static final Method ARRAY_LENGTH_METHOD;

    static {
        try {
            ARRAY_LENGTH_METHOD = Array.class.getMethod("getLength", Object.class);
        } catch (NoSuchMethodException e) {
            throw new ElException("获取数组长度方法不存在！", e);
        }
    }

    //用于获取方法参数的EL，返回结果必须是数组
    private final ArrayInvoker parameterEl;
    private final String methodName;

    private MethodNodeInvoker(String nodeName, String methodName, ArrayInvoker parameterEl) {
        super(nodeName);
        this.methodName = methodName;
        this.parameterEl = parameterEl;
    }

    public static MethodNodeInvoker newInstance(String nodeName, String methodName, ArrayInvoker parameterEl) {
        return new MethodNodeInvoker(nodeName, methodName, parameterEl);
    }

    private static Method findMethod(Class<?> type, String methodName, int argNum) {
        String key = type.getName() + '#' + methodName + '@' + argNum;
        return METHOD_POOL.computeIfAbsent(key, k -> {
            try {
                if (type.isArray() && "size".equals(methodName) && argNum == 0) {
                    // 特殊情况，支持size方法获取数组长度
                    return () -> ARRAY_LENGTH_METHOD;
                }

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
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        if (null == parentNode) {
            return null;
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            return null;
        }

        try {
            Object[] parameters = parameterEl.invoke(ctx).toArray();
            Class<?> type;
            if (parentValue instanceof Class) {
                type = (Class<?>) parentValue;
            } else {
                type = parentValue.getClass();
            }
            Method method = findMethod(type, methodName, parameters.length);
            if (parameters.length == 0) {
                if (ARRAY_LENGTH_METHOD == method) {
                    // 计算数组长度
                    return Array.getLength(parentValue);
                } else {
                    return method.invoke(parentValue);
                }
            } else {
                Type[] types = method.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    if (types[i] instanceof TypeVariable) {
                        parameters[i] = ElUtils.cast(parameters[i], BeanUtils.getRawType(types[i]));
                    } else {
                        parameters[i] = ElUtils.cast(parameters[i], types[i]);
                    }
                }
                return method.invoke(parentValue, parameters);
            }
        } catch (Exception e) {
            throw new ElException(methodName + "执行失败！", e);
        }
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

}
