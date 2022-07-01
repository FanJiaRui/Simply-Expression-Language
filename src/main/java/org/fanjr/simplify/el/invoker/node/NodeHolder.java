package org.fanjr.simplify.el.invoker.node;

/**
 * @author fanjr15662@hundsun.com
 * @file NodeResult.java
 * @since 2021/7/7 上午10:46
 */
public final class NodeHolder {

    private final Object value;
    private final NodeHolder parentNode;
    private final int index;

    private final NodeInvoker nodeInvoker;

    private NodeHolder(Object value, NodeHolder parentNode, NodeInvoker nodeInvoker, int index) {
        this.value = value;
        this.parentNode = parentNode;
        this.nodeInvoker = nodeInvoker;
        this.index = index;
    }

    public static NodeHolder newNodeHolder(Object value, NodeHolder parentNode, NodeInvoker nodeInvoker) {
        return new NodeHolder(value, parentNode, nodeInvoker, 0);
    }

    public static NodeHolder newArrayNodeHolder(Object value, NodeHolder parentNode, NodeInvoker nodeInvoker, int nowIndex) {
        return new NodeHolder(value, parentNode, nodeInvoker, nowIndex);
    }

    public Object getValue() {
        return value;
    }

    /**
     * 本质上是将这个值设置到其上层结构中
     *
     * @param value
     * @return
     */
    public void setValue(Object value) {
        nodeInvoker.setValueByParent(parentNode, value, index);
    }

    public NodeHolder getParentNode() {
        return parentNode;
    }

    public boolean isRoot() {
        return nodeInvoker instanceof RootNodeInvoker;
    }
}
