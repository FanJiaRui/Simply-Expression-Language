package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.ElException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/6 下午4:28
 */
public class RootNodeInvoker extends NodeInvoker {

    public static final RootNodeInvoker INSTANCE = new RootNodeInvoker();
    private static final Logger logger = LoggerFactory.getLogger(RootNodeInvoker.class);

    private RootNodeInvoker() {
        super("this");
    }

    @Override
    public NodeHolder getNodeHolder(Object ctx) {
        return NodeHolder.newNodeHolder(ctx, null, this);
    }

    @Override
    public NodeHolder getParentNodeHolder(Object ctx) {
        return null;
    }

    @Override
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //root节点没有父节点
        return ctx;
    }

    @Override
    public Object invoke(Object ctx) {
        return ctx;
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ElException("上下文根节点不可被赋值或为常量！");
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        // skip
        logger.info("移除【{}】操作无效，无需移除！", this);
    }

    @Override
    protected void acceptChild(ELVisitor visitor) {

    }

    @Override
    public boolean isVariable() {
        // root根点认为为常量
        return false;
    }

    @Override
    public void accept(ELVisitor visitor) {
        visitor.visit(this);
    }
}
