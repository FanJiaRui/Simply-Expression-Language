package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.JSONArray;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Array;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author fanjr@vip.qq.com
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
        int index = getIndex(ctx);
        NodeHolder parentNode = getParentNodeHolder(ctx);
        Object value = getValueByParent(ctx, parentNode, index);
        NodeHolder nodeHolder = NodeHolder.newArrayNodeHolder(value, parentNode, this, index);
        if (parentNode.isChange()) {
            nodeHolder.setChange(true);
        }
        return nodeHolder;
    }

    @Override
    void setValueByParent(NodeHolder parentNode, Object value, int index) {
        if (null == parentNode) {
            throw new ElException("不可对【" + this.toString() + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (parentValue == null) {
            List<Object> newArray = new JSONArray();
            newArray.set(index, value);
            parentNode.setValue(newArray);
            return;
        }

        if (parentValue instanceof List) {
            if (parentValue instanceof JSONArray) {
                ((JSONArray) parentValue).set(index, value);
                if (parentNode.isChange()) {
                    parentNode.setValue(parentValue);
                }
            } else if (index >= ((List<Object>) parentValue).size()) {
                JSONArray jsonArray = new JSONArray((List<Object>) parentValue);
                parentNode.setValue(jsonArray);
            } else {
                ((List<Object>) parentValue).set(index, value);
                if (parentNode.isChange()) {
                    parentNode.setValue(parentValue);
                }
            }
            return;
        }
        Class<?> parentValueClass = parentValue.getClass();
        if (parentValueClass.isArray()) {
            int arrayLen = Array.getLength(parentValue);
            if (index < arrayLen) {
                Array.set(parentValue, index, ElUtils.cast(value, parentValueClass.getComponentType()));
                if (parentNode.isChange()) {
                    parentNode.setValue(parentValue);
                }
            } else {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < arrayLen; i++) {
                    jsonArray.add(Array.get(parentValue, i));
                }
                jsonArray.set(index, value);
                parentNode.setValue(jsonArray);
            }

            return;
        }


        List<Object> jsonArray = new JSONArray();
        jsonArray.add(parentValue);
        jsonArray.set(index, value);
        parentNode.setValue(jsonArray);
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        if (null == parentNode) {
            throw new ElException("不可对【" + this.toString() + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (parentValue == null) {
            // 空值无需再移除，跳过操作
            return;
        }

        if (parentValue instanceof List) {
            if (index < ((List<?>) parentValue).size()) {
                ((List<?>) parentValue).remove(index);
            }
            return;
        }

        Class<?> parentClass = parentValue.getClass();
        if (parentClass.isArray()) {
            int arrayLen = Array.getLength(parentValue);
            if (index < arrayLen) {
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < arrayLen; i++) {
                    if (i != index) {
                        jsonArray.add(Array.get(parentValue, i));
                    }
                }
                parentNode.setValue(jsonArray);
            }
            return;
        }

        if (parentValue instanceof String) {
            String str = (String) parentValue;
            char first = str.trim().charAt(0);
            if (first == '[') {
                try {
                    JSONArray jsonArray = JSONArray.parse(str);
                    if (index < jsonArray.size()) {
                        jsonArray.remove(index);
                        parentNode.setValue(jsonArray);
                    }
                    return;
                } catch (Exception e) {
                    // 解析JSON字符串失败，不按照JSON进行解析
                }
            }
        }

        if (index == 0) {
            //既不是List也不是Array，若index为0则移除parent，否则不做操作
            parentNode.remove();
        }
    }

    Object getValueByParent(Object ctx, NodeHolder parentNode, int index) {
        if (parentNode == null || parentNode.getValue() == null) {
            return null;
        }
        Object parentValue = parentNode.getValue();
        if (parentValue instanceof List) {
            if (index >= ((List<?>) parentValue).size()) {
                return null;
            }
            return ((List<?>) parentValue).get(index);
        }

        if (parentValue.getClass().isArray()) {
            if (index >= Array.getLength(parentValue)) {
                return null;
            }
            return Array.get(parentValue, index);
        }

        if (parentValue instanceof String) {
            String str = (String) parentValue;
            char first = str.trim().charAt(0);
            if (first == '[') {
                try {
                    JSONArray jsonArray = JSONArray.parse(str);
                    parentNode.setChange(true);
                    return jsonArray.get(0);
                } catch (Exception e) {
                    // 解析JSON字符串失败，不按照JSON进行解析
                }
            }
        }

        // index等于0时，且不是数组、list或者JsonArray字符串时，返回自身
        if (index == 0) {
            return parentValue;
        }
        // 没有子节点，返回0
        return null;
    }

    @Override
    @Deprecated
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        //skip
        return null;
    }

    @Override
    public String toString() {
        return parentNodeInvoker.toString() + "[" + indexEl.toString() + "]";
    }
}
