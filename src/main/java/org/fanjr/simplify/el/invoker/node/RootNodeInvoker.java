package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.context.ContextException;

/**
 * @author fanjr15662@hundsun.com
 * @file RootInvoker.java
 * @since 2021/7/6 下午4:28
 */
public class RootNodeInvoker extends NodeInvoker {

    public static final RootNodeInvoker INSTANCE = new RootNodeInvoker();

    private RootNodeInvoker() {
        super("this");
    }

    @Override
    public Object invoke(Object ctx) {
        return ctx;
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
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ContextException("不可设置ROOT(this)节点值！");
    }

    @Override
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //root节点没有父节点
        return ctx;
    }
}
