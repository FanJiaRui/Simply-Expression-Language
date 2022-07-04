package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.JSONArray;
import org.fanjr.simplify.context.ContextException;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author fanjr@vip.qq.com
 * @file IndexNodeInvoker.java
 * @since 2021/7/8 上午11:42
 */
public class IndexNodeInvoker extends NodeInvoker {

    private final ELInvoker indexEl;

    private IndexNodeInvoker(String nodeName, NodeInvoker superNode, ELInvoker indexEl) {
        super(nodeName);
        parentNodeInvoker = superNode;
        this.indexEl = indexEl;
    }

    public static IndexNodeInvoker newInstance(String nodeName, NodeInvoker superNode, ELInvoker indexEl) {
        return new IndexNodeInvoker(nodeName, superNode, indexEl);
    }

    @Override
    public void setParentNodeInvoker(NodeInvoker parentNodeInvoker) {
        this.parentNodeInvoker.setParentNodeInvoker(parentNodeInvoker);
    }

    private int getIndex(Object ctx) {
        return ElUtils.cast(indexEl.invoke(ctx), int.class);
    }

    public NodeHolder getNodeHolder(Object ctx) {
        NodeHolder parentNode = getParentNodeHolder(ctx);
        int index = getIndex(ctx);
        if (parentNode == null || parentNode.getValue() == null) {
            return NodeHolder.newArrayNodeHolder(null, parentNode, this, index);
        }
        Object parentValue = parentNode.getValue();
        Object value = null;
        if (parentValue instanceof List) {
            value = ((List<?>) parentValue).get(index);
        } else if (parentValue.getClass().isArray()) {
            value = Array.get(parentValue, index);
        } else if (index == 0) {
            value = parentValue;
        }

        return NodeHolder.newArrayNodeHolder(value, parentNode, this, index);
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        if (null == parentNode) {
            throw new ContextException("不可对【" + this.toString() + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (parentValue == null) {
            List<Object> newArray = new JSONArray();
            newArray.set(index, value);
            parentNode.setValue(newArray);
        } else if (parentValue instanceof List) {
            ((List<Object>) parentValue).set(index, value);
        } else if (parentValue.getClass().isArray()) {
            Array.set(parentValue, index, value);
        } else {
            List<Object> gapsArray = new JSONArray();
            gapsArray.add(parentValue);
            gapsArray.set(index, value);
            parentNode.setValue(gapsArray);
        }
    }

    @Override
    @Deprecated
    public Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //skip
        return null;
    }

    @Override
    public String toString() {
        return parentNodeInvoker.toString() + "[" + indexEl.toString() + "]";
    }
}
