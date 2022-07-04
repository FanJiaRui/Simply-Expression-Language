package org.fanjr.simplify.el.invoker.node;

import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.ELInvoker;

/**
 * 用于对节点的第一个特殊节点进行取值
 *
 * @author fanjr@vip.qq.com
 * @file FirstNodeInvoker.java
 * @since 2021/7/8 上午10:13
 */
public class FirstNodeInvoker extends NodeInvoker {

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
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        throw new ElException("不可设置【" + el.toString() + "】的值！");
    }

    @Override
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //当前节点没有父节点
        return ctx;
    }

}
