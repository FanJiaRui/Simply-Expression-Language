package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.ElException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于对节点的第一个特殊节点进行取值
 *
 * @author fanjr@vip.qq.com
 * @since 2021/7/8 上午10:13
 */
public class FirstNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(FirstNodeInvoker.class);

    private final ELInvoker el;

    private FirstNodeInvoker(ELInvoker el) {
        super(el.toString());
        this.el = el;
    }

    public static FirstNodeInvoker newInstance(ELInvoker el) {
        return new FirstNodeInvoker(el);
    }

    @Override
    public Object invoke(Object ctx) {
        return el.invoke(ctx);
    }

    @Override
    public NodeHolder getNodeHolder(Object ctx) {
        return NodeHolder.newNodeHolder(el.invoke(ctx), null, this);
    }

    @Override
    void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ElException("不可设置【" + el.toString() + "】的值！");
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        // skip
        logger.info("移除【{}】操作无效，无需移除！", this.toString());
    }

    @Override
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //当前节点没有父节点
        return ctx;
    }


    @Override
    public boolean isVariable() {
        // 动作、方法类为非变量
        return false;
    }

    @Override
    protected void acceptChild(ELVisitor visitor) {
        el.accept(visitor);
    }
}
