package net.fanjr.simplify.el.invoker.node;

import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.invoker.ArrayInvoker;
import net.fanjr.simplify.el.reflect.ELFunctionInvokeUtils;
import net.fanjr.simplify.el.reflect.ELInnerFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 自定义函数执行器
 *
 * @author fanjr@vip.qq.com
 * @since 2023/5/30 下午15:28
 */
public class FunctionMethodNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MethodNodeInvoker.class);

    //用于获取方法参数的EL，返回结果必须是数组
    private final ArrayInvoker parameterEl;
    private final String utilName;
    private final String methodName;

    private FunctionMethodNodeInvoker(String nodeName, String utilName, String functionName, ArrayInvoker parameterEl) {
        super(nodeName);
        this.utilName = utilName;
        this.methodName = functionName;
        this.parameterEl = parameterEl;
    }

    public static FunctionMethodNodeInvoker newInstance(String nodeName, String className, String methodName, ArrayInvoker parameterEl) {
        return new FunctionMethodNodeInvoker(nodeName, className, methodName, parameterEl);
    }

    @Override
    public NodeHolder getNodeHolder(Object ctx) {
        Object[] parameters = parameterEl.invoke(ctx).toArray();
        ELInnerFunction function = ELFunctionInvokeUtils.findFunction(utilName, methodName, parameters);
        if (null == function) {
            return NodeHolder.newNodeHolder(null, null, this);
        }
        return NodeHolder.newNodeHolder(function.invoke(ctx, parameters), null, this);
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
        throw new ELException("不可对【" + this + "】方法执行结果重新赋值！");
    }

    @Override
    public boolean isVariable() {
        // 动作、方法类为非变量
        return false;
    }

    @Override
    protected void acceptChild(ELVisitor visitor) {
        // 内置函数，子节点只包括参数
        parameterEl.accept(visitor);
    }
}
