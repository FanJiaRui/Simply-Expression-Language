package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import org.fanjr.simplify.context.ContextException;

import java.util.Map;

/**
 * FIXME 还没做完
 *
 * @author fanjr@vip.qq.com
 * @file MapNodeInvoker.java
 * @since 2021/7/8 上午11:42
 */
public class MapNodeInvoker extends NodeInvoker {

//    private final GapsContextDispatcher dispatcher;

    private MapNodeInvoker(String nodeName) {
        super(nodeName);
//        this.dispatcher = GapsContextDispatcher.getInstance(nodeName);
    }

    public static MapNodeInvoker newInstance(String nodeName) {
        return new MapNodeInvoker(nodeName);
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        if (null == parentNode) {
            throw new ContextException("不可对【" + this.toString() + "】进行赋值！");
        }
        if (null == parentNode.getValue()) {
            if (parentNode.isRoot()) {
                throw new ContextException("ROOT节点为空！不可对【" + this.toString() + "】进行赋值！");
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(nodeName, value);
            parentNode.setValue(jsonObject);
        } else {
//            dispatcher.doPut(parentNode.getValue(), value, false);
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
        if (parentValue instanceof Map) {
            return ((Map<?, ?>) parentValue).get(nodeName);
        }
        if (parentValue instanceof String) {
            String json = (String) parentValue;
            if (json.isEmpty() || "null".equals(json)) {
                return null;
            }

            char first = json.trim().charAt(0);
            if (first == '{') {
                try (JSONReader reader = JSONReader.of(json)) {
                    ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
                    JSONObject jsonObject = objectReader.readObject(reader, 0);
                    return new JsonContext(jsonObject);
                }
            }
        }

//        return dispatcher.doGet(parentValue);
    }
}
