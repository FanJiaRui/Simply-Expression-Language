package net.fanjr.simplify.el.invoker.node;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/7 上午10:46
 */
public final class NodeHolder {

    private final NodeHolder parentNode;
    private final int index;
    private final NodeInvoker nodeInvoker;
    private Object value;
    /**
     * 用于标记get操作时是否变化了原有类型
     */
    private boolean change;

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

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean change) {
        this.change = change;
    }

    public Object getValue() {
        return value;
    }

    /**
     * 本质上是将这个值设置到其上层结构中
     *
     * @param value 要设置的值
     */
    public void setValue(Object value) {
        this.value = value;
        nodeInvoker.setValueByParent(parentNode, value, index);
    }

    public void remove() {
        this.value = null;
        nodeInvoker.removeValueByParent(parentNode, index);
    }

    public NodeHolder getParentNode() {
        return parentNode;
    }

    public boolean isRoot() {
        return nodeInvoker instanceof RootNodeInvoker;
    }


}
