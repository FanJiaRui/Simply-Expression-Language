package net.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.utils.ElUtils;
import net.fanjr.simplify.utils.SimplifyException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class IndexMapNodeInvoker extends NodeInvoker {

    private final String indexNodeName;

    private IndexMapNodeInvoker(String nodeName, NodeInvoker superNode, String indexNodeName) {
        super(nodeName);
        parentNodeInvoker = superNode;
        this.indexNodeName = indexNodeName;
    }

    public static IndexMapNodeInvoker newInstance(String nodeName, NodeInvoker superNode, String indexNodeName) {
        return new IndexMapNodeInvoker(nodeName, superNode, indexNodeName);
    }

    @Override
    public void setParentNodeInvoker(NodeInvoker parentNodeInvoker) {
        if (this.parentNodeInvoker == null) {
            this.parentNodeInvoker = parentNodeInvoker;
        } else {
            this.parentNodeInvoker.setParentNodeInvoker(parentNodeInvoker);
        }
    }

    @Override
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        String nodeName = this.indexNodeName;
        if (null == parentNode) {
            return null;
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            return null;
        }
        if (parentValue instanceof Map) {
            return ((Map<?, ?>) parentValue).get(nodeName);
        }
        Class<?> parentClass = parentValue.getClass();
        if (parentClass == Class.class) {
            if (((Class<?>) parentValue).isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) parentValue, nodeName);
            }
        } else if (parentClass.isEnum()) {
            try {
                Field field = parentClass.getDeclaredField(nodeName);
                field.setAccessible(true);
                return field.get(parentValue);
            } catch (Exception e) {
                return null;
            }
        } else if (parentClass == String.class) {
            String json = (String) parentValue;
            if (ElUtils.isBlank(json) || "null".equals(json)) {
                return null;
            }

            char first = json.trim().charAt(0);
            if (first == '{') {
                try {
                    JSONObject jsonObject = JSON.parseObject(json);
                    parentNode.setChange(true);
                    return jsonObject.get(nodeName);
                } catch (Exception e) {
                    // 解析JSON字符串失败，不按照JSON进行解析
                    // TODO 按临时解决方案，后续考虑更具性能的方案
                    return null;
                }
            }
        }

        return ElUtils.getFieldByPojo(parentValue, nodeName);
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {
        String nodeName = this.indexNodeName;
        if (null == parentNode) {
            return;
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            return;
        }
        if (parentValue instanceof Map) {
            ((Map<?, ?>) parentValue).remove(nodeName);
        }

        Class<?> parentClass = parentValue.getClass();
        if (parentClass == String.class) {
            //Parent类型为字符串，操作完后确保推送回Parent为字符串
            String json = (String) parentValue;
            if (ElUtils.isBlank(json) || "null".equals(json)) {
                return;
            } else {
                char first = json.trim().charAt(0);
                if (first == '{') {
                    try (JSONReader reader = JSONReader.of(json)) {
                        ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
                        JSONObject jsonObject = objectReader.readObject(reader, 0);
                        jsonObject.remove(nodeName);
                        parentNode.setValue(jsonObject.toString());
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        if (parentValue instanceof List || parentClass.isArray()) {
            // 不做处理
            return;
        }

        ElUtils.putFieldByPojo(parentValue, nodeName, null);
    }

    @Override
    void setValueByParent(NodeHolder parentNode, Object value, int index) {
        String nodeName = this.indexNodeName;
        if (null == parentNode) {
            throw new SimplifyException("不可对【" + this + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            if (parentNode.isRoot()) {
                throw new SimplifyException("ROOT节点为空！不可对【" + this + "】进行赋值！");
            }
            parentNode.setValue(JSONObject.of(nodeName, value));
        } else {
            if (parentValue instanceof Map) {
                ((Map<String, Object>) parentValue).put(nodeName, value);
                if (parentNode.isChange()) {
                    parentNode.setValue(parentValue);
                }
                return;
            }

            Class<?> parentClass = parentValue.getClass();
            if (parentClass == String.class) {
                //Parent类型为字符串，操作完后确保推送回Parent为字符串
                String json = (String) parentValue;
                if (json.isEmpty() || "null".equals(json)) {
                    parentNode.setValue(JSONObject.of(nodeName, value).toString());
                    return;
                } else {
                    char first = json.trim().charAt(0);
                    if (first == '{') {
                        try (JSONReader reader = JSONReader.of(json)) {
                            ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
                            JSONObject jsonObject = objectReader.readObject(reader, 0);
                            jsonObject.put(nodeName, value);
                            parentNode.setValue(jsonObject.toString());
                            return;
                        }
                    }
                }
            }

            if (parentClass.isArray() || parentValue instanceof Collection) {
                //打破原有结构
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(nodeName, value);
                parentNode.setValue(jsonObject);
                return;
            }

            if (ElUtils.putFieldByPojo(parentValue, nodeName, value)) {
                if (parentNode.isChange()) {
                    parentNode.setValue(parentValue);
                }
            }
        }
    }

    @Override
    public String toString() {
        return parentNodeInvoker.toString() + "['" + indexNodeName + "']";
    }

    @Override
    public boolean isVariable() {
        if (null == parentNodeInvoker || parentNodeInvoker instanceof RootNodeInvoker) {
            // 没有上层节点，或者上层节点为ROOT节点，说明当前节点为变量
            return true;
        }

        // 上级节点为变量则当前节点也为变量
        return parentNodeInvoker.isVariable();
    }

    @Override
    protected void acceptChild(ELVisitor visitor) {
        // skip
    }

}
