package org.fanjr.simplify.el.invoker.node;


import org.fanjr.simplify.el.ELInvoker;

/**
 * 节点执行器，用于获取或设置节点
 *
 * @author fanjr15662@hundsun.com
 * @file NodeInvoker.java
 * @since 2021/6/28 下午5:03
 */
public abstract class NodeInvoker implements ELInvoker {

    protected final String nodeName;

    protected NodeInvoker parentNodeInvoker;

    protected NodeInvoker(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public Object invoke(Object ctx) {
        return getNodeHolder(ctx).getValue();
    }

    public NodeHolder getNodeHolder(Object ctx) {
        NodeHolder parentNode = getParentNodeHolder(ctx);
        Object value = getValueByParent(ctx, parentNode);
        return NodeHolder.newNodeHolder(value, parentNode, this);
    }

    public NodeHolder getParentNodeHolder(Object ctx) {
        if (null != parentNodeInvoker) {
            return parentNodeInvoker.getNodeHolder(ctx);
        }
        return RootNodeInvoker.INSTANCE.getNodeHolder(ctx);
    }

    public void setParentNodeInvoker(NodeInvoker parentNodeInvoker) {
        this.parentNodeInvoker = parentNodeInvoker;
    }

    public abstract void setValueByParent(NodeHolder parentNode, Object value, int index);

    public abstract Object getValueByParent(Object ctx, NodeHolder parentNode);

    public String toString() {
        if (null != parentNodeInvoker) {
            return parentNodeInvoker.toString() + '.' + nodeName;
        }
        return nodeName;
    }

}
