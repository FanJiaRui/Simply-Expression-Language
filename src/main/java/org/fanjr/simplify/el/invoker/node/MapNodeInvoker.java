package org.fanjr.simplify.el.invoker.node;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import org.fanjr.simplify.el.ElException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author fanjr@vip.qq.com
 * @file MapNodeInvoker.java
 * @since 2021/7/8 上午11:42
 */
public class MapNodeInvoker extends NodeInvoker {

    private static final Logger logger = LoggerFactory.getLogger(MapNodeInvoker.class);

    private MapNodeInvoker(String nodeName) {
        super(nodeName);
    }

    public static MapNodeInvoker newInstance(String nodeName) {
        return new MapNodeInvoker(nodeName);
    }

    @Override
    public void setValueByParent(NodeHolder parentNode, Object value, int index) {
        if (null == parentNode) {
            throw new ElException("不可对【" + this.toString() + "】进行赋值！");
        }
        Object parentValue = parentNode.getValue();
        if (null == parentValue) {
            if (parentNode.isRoot()) {
                throw new ElException("ROOT节点为空！不可对【" + this.toString() + "】进行赋值！");
            }
            parentNode.setValue(JSONObject.of(nodeName, value));
        } else {
            if (parentValue instanceof Map) {
                ((Map<String, Object>) parentValue).put(nodeName, value);
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
                            parentNode.setChange(true);
                            jsonObject.put(nodeName, value);
                            parentNode.setValue(jsonObject.toString());
                            return;
                        }
                    }
                }
            }

            if (parentClass.isArray()) {
                //打破原有结构
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(nodeName, value);
                parentNode.setValue(jsonObject);
                return;
            }

            ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
            ObjectWriter objectWriter = provider.getObjectWriter(parentClass);
            FieldWriter fieldWriter = objectWriter.getFieldWriter(nodeName);
            if (null != fieldWriter) {
                // 优先采用方法获取，其次采用Field
                Method method = fieldWriter.getMethod();
                if (method != null) {
                    try {
                        method.invoke(parentValue, value);
                    } catch (Exception e) {
                        // skip
                    }
                }
                Field field = fieldWriter.getField();
                if (field != null) {
                    try {
                        field.set(parentValue, value);
                    } catch (IllegalAccessException e) {
                        // skip
                    }
                }
            } else {
                //打破原有结构
                JSONObject jsonObject = JSONObject.parseObject(JSONObject.toJSONString(parentValue));
                jsonObject.put(nodeName, value);
                parentNode.setValue(jsonObject);
                return;
            }

            if (parentNode.isChange()) {
                parentNode.setValue(parentValue);
            }
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
            if (json.isEmpty() || "null".equals(json)) {
                return null;
            }

            char first = json.trim().charAt(0);
            if (first == '{') {
                try (JSONReader reader = JSONReader.of(json)) {
                    ObjectReader<JSONObject> objectReader = reader.getObjectReader(JSONObject.class);
                    JSONObject jsonObject = objectReader.readObject(reader, 0);
                    parentNode.setChange(true);
                    return jsonObject.get(nodeName);
                }
            }
        }

        //从javaBean中取值
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        ObjectReader<?> reader = provider.getObjectReader(parentClass);
        FieldReader<?> fieldReader = reader.getFieldReader(nodeName);
        if (null != fieldReader) {
            // 优先采用方法获取，其次采用Field
            Method method = fieldReader.getMethod();
            if (method != null) {
                try {
                    return method.invoke(parentValue);
                } catch (Exception e) {
                    // skip
                }
            }
            Field field = fieldReader.getField();
            if (field != null) {
                try {
                    return field.get(parentValue);
                } catch (IllegalAccessException e) {
                    // skip
                }
            }
        }

        logger.warn("无法从类型[{}]中获取属性[{}]", parentClass.getName(), nodeName);
        //无法获取值，取null
        return null;
    }

}
