package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.ArrayInvoker;
import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author fanjr@vip.qq.com
 * @file NewObjectNodeInvoker.java
 * @since 2021/7/12 下午1:42
 */
public class NewObjectNodeInvoker extends NodeInvoker {

    private static final Map<String, Supplier<Constructor<?>>> CONSTRUCTOR_POOL = new ConcurrentHashMap<>();

    //用于获取方法参数的EL，返回结果必须是数组
    private final ArrayInvoker parameterEl;

    private final String className;

    private NewObjectNodeInvoker(String nodeName, String className, ArrayInvoker parameterEl) {
        super(nodeName);
        this.parameterEl = parameterEl;
        this.className = className;
    }

    public static NewObjectNodeInvoker newInstance(String nodeName, String className, ArrayInvoker parameterEl) {
        return new NewObjectNodeInvoker(nodeName, className, parameterEl);
    }

    private static Constructor<?> findConstructor(String className, int argNum) {
        String key = className + '@' + argNum;
        return CONSTRUCTOR_POOL.computeIfAbsent(key, k -> {
            try {
                Class<?> type = Class.forName(className);
                if (0 == argNum) {
                    Constructor<?> constructor = type.getConstructor();
                    return () -> constructor;
                } else {
                    Constructor<?>[] constructors = type.getConstructors();
                    for (Constructor<?> constructor : constructors) {
                        if (argNum == constructor.getParameterCount()) {
                            return () -> constructor;
                        }
                    }
                    return () -> {
                        throw new ElException(className + "实例化失败！找不到构造方法！");
                    };
                }
            } catch (Exception e) {
                return () -> {
                    throw new ElException(className + "实例化失败！", e);
                };
            }
        }).get();
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ElException("不可对【" + this.toString() + "】执行结果重新赋值！");
    }

    @Override
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        try {
            Object[] parameters = parameterEl.invoke(ctx).toArray();
            Constructor<?> constructor = findConstructor(className, parameters.length);
            if (parameters.length == 0) {
                return constructor.newInstance();
            } else {
                Type[] types = constructor.getGenericParameterTypes();
                for (int i = 0; i < parameters.length; i++) {
                    parameters[i] = ElUtils.cast(parameters[i], types[i]);
                }
                return constructor.newInstance(parameters);
            }
        } catch (Exception e) {
            throw new ElException(className + "实例化失败！", e);
        }
    }
}
