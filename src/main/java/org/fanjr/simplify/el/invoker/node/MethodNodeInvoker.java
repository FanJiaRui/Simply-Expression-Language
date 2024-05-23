package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.ArrayInvoker;
import org.fanjr.simplify.el.reflect.ELFunctionInvokeUtils;
import org.fanjr.simplify.el.reflect.ELObjectFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/7 下午2:43
 */
public class MethodNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MethodNodeInvoker.class);

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

            if (parameters.length == 0) {
                if (type.isArray() && ("size".equals(methodName) || "getLength".equals(methodName))) {
                    // 特殊情况，支持size方法、getLength方法获取数组长度
                    return Array.getLength(parentValue);
                }
            }
            ELObjectFunction objectFunction = ELFunctionInvokeUtils.getObjectFunction(parentValue, methodName, parameters);
            if (null == objectFunction) {
                // 找不到方法
                return null;
            }
            return objectFunction.invoke(parentValue, ctx, parameters);
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
