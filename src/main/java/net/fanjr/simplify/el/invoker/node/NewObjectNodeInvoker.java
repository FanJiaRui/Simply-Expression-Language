package net.fanjr.simplify.el.invoker.node;

import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.invoker.ArrayInvoker;
import net.fanjr.simplify.utils.$;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/12 下午1:42
 */
public class NewObjectNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(NewObjectNodeInvoker.class);

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
                        throw new ELException(className + "实例化失败！找不到构造方法！");
                    };
                }
            } catch (Exception e) {
                return () -> {
                    throw new ELException(className + "实例化失败！", e);
                };
            }
        }).get();
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ELException("不可对【" + this + "】执行结果重新赋值！");
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
                    parameters[i] = $.cast(parameters[i], types[i]);
                }
                return constructor.newInstance(parameters);
            }
        } catch (Exception e) {
            throw new ELException(className + "实例化失败！", e);
        }
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        // skip
        logger.info("移除【{}】操作无效，无需移除！", this);
    }

    @Override
    public boolean isVariable() {
        // 动作、方法类为非变量
        return false;
    }

    @Override
    protected void acceptChild(ELVisitor visitor) {
        parameterEl.accept(visitor);
    }
}
