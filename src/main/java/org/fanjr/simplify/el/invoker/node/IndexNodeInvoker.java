package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson.parser.ParserConfig;
import com.hundsun.gaps.flowexecutor.GapsArray;
import com.hundsun.gaps.flowexecutor.el.ELInvoker;
import com.hundsun.gaps.flowexecutor.exceptions.GapsFlowContextException;
import com.hundsun.gaps.flowexecutor.utils.GapsTypeUtils;

import java.lang.reflect.Array;
import java.util.List;

/**
 * @author fanjr15662@hundsun.com
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
        return GapsTypeUtils.cast(indexEl.invoke(ctx), int.class, ParserConfig.getGlobalInstance());
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
            throw new GapsFlowContextException("不可对【" + this.toString() + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (parentValue == null) {
            GapsArray gapsArray = new GapsArray();
            gapsArray.set(index, value);
            parentNode.setValue(gapsArray);
        } else if (parentValue instanceof List) {
            ((List<Object>) parentValue).set(index, value);
        } else if (parentValue.getClass().isArray()) {
            Array.set(parentValue, index, value);
        } else {
            GapsArray gapsArray = new GapsArray();
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
