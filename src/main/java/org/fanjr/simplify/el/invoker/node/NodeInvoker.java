package org.fanjr.simplify.el.invoker.node;


import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;

/**
 * 节点执行器，用于获取或设置节点
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午5:03
 */
public abstract class NodeInvoker implements ELInvoker, Node {

    protected final String nodeName;

    protected NodeInvoker parentNodeInvoker;

    protected NodeInvoker(String nodeName) {
        this.nodeName = nodeName;
    }

    @Override
    public Object getNode(Object ctx) {
        return getNodeHolder(ctx).getValue();
    }

    @Override
    public void putNode(Object ctx, Object value) {
        getNodeHolder(ctx).setValue(value);
    }

    @Override
    public void removeNode(Object ctx) {
        getNodeHolder(ctx).remove();
    }

    @Override
    public Object invoke(Object ctx) {
        return getNode(ctx);
    }

    /**
     * 计算出当前NodeHolder
     *
     * @param ctx 上下文
     * @return 节点Holder
     */
    public NodeHolder getNodeHolder(Object ctx) {
        NodeHolder parentNode = getParentNodeHolder(ctx);
        Object value = getValueByParent(ctx, parentNode);
        NodeHolder nodeHolder = NodeHolder.newNodeHolder(value, parentNode, this);
        if (parentNode.isChange()) {
            nodeHolder.setChange(true);
        }
        return nodeHolder;
    }

    /**
     * 计算出上一级NodeHolder
     *
     * @param ctx 上下文
     * @return 节点Holder
     */
    protected NodeHolder getParentNodeHolder(Object ctx) {
        if (null != parentNodeInvoker) {
            return parentNodeInvoker.getNodeHolder(ctx);
        }
        return RootNodeInvoker.INSTANCE.getNodeHolder(ctx);
    }

    public void setParentNodeInvoker(NodeInvoker parentNodeInvoker) {
        this.parentNodeInvoker = parentNodeInvoker;
    }

    abstract void setValueByParent(NodeHolder parentNode, Object value, int index);

    abstract Object getValueByParent(Object ctx, NodeHolder parentNode);

    abstract void removeValueByParent(NodeHolder parentNode, int index);

    public String toString() {
        if (null != parentNodeInvoker) {
            return parentNodeInvoker.toString() + '.' + nodeName;
        }
        return nodeName;
    }


    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            if (null != parentNodeInvoker) {
                parentNodeInvoker.accept(visitor);
            }
            acceptChild(visitor);
        }
    }

    protected abstract void acceptChild(ELVisitor visitor);

}
