package org.fanjr.simplify.el.invoker.node;

import com.hundsun.gaps.core.context.GapsContextDispatcher;
import com.hundsun.gaps.core.exceptions.GapsUnusableException;
import com.hundsun.gaps.flowexecutor.GapsContext;
import com.hundsun.gaps.flowexecutor.exceptions.GapsFlowContextException;

/**
 * @author fanjr15662@hundsun.com
 * @file MapNodeInvoker.java
 * @since 2021/7/8 上午11:42
 */
public class MapNodeInvoker extends NodeInvoker {

    private final GapsContextDispatcher dispatcher;

    private MapNodeInvoker(String nodeName) {
        super(nodeName);
        this.dispatcher = GapsContextDispatcher.getInstance(nodeName);
    }

    public static MapNodeInvoker newInstance(String nodeName) {
        return new MapNodeInvoker(nodeName);
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        if (null == parentNode) {
            throw new GapsFlowContextException("不可对【" + this.toString() + "】进行赋值！");
        }
        if (null == parentNode.getValue()) {
            if (parentNode.isRoot()) {
                throw new GapsUnusableException("ROOT节点为空！不可对【" + this.toString() + "】进行赋值！");
            }
            GapsContext context = new GapsContext();
            context.put(nodeName, value);
            parentNode.setValue(context);
        } else {
            dispatcher.doPut(parentNode.getValue(), value, false);
        }
    }

    @Override
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        if (null == parentNode) {
            return null;
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            return null;
        }
        return dispatcher.doGet(parentValue);
    }
}
