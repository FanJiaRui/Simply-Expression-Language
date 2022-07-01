package org.fanjr.simplify.context;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;

import java.util.Map;
import java.util.function.Function;

/**
 * @author fanjr@vip.qq.com
 * @file IContext.java
 * @since 2022/5/23 下午5:33
 */
public interface IContext {

    static IContext toContext(Object javaObject) {
        if (null == javaObject) {
            return null;
        }

        if (javaObject instanceof IContext) {
            return (IContext) javaObject;
        }

        if (javaObject instanceof JSONObject) {
            return new JsonContext((JSONObject) javaObject);
        }

        if (javaObject instanceof Map) {
            return new JsonContext(new JSONObject((Map) javaObject));
        }

        if (javaObject instanceof String) {
            String json = (String) javaObject;
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

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function<Object, JSONObject> typeConvert = provider.getTypeConvert(javaObject.getClass(), JSONObject.class);
        if (typeConvert != null) {
            return new JsonContext(typeConvert.apply(javaObject));
        }

        throw new ContextException(String.format("转换成上下文发生错误,类型[%s]无法直接进行转换", javaObject.getClass().toString()));
    }

    /**
     * 将变量value放入上下文中对应节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @param value    需要存储的变量
     * @return 执行put方法时对已经存储的对象进行了覆盖, 则返回这个对象, 否则返回null</ br>
     * 例如:对上下文{}执行put操作</br>
     * key为"a.b",value为"xxx"时上下文内容将变成{"a":{"b":"xxx"}}</br>
     * @throws NullPointerException key为空时将会抛出这个异常
     * @throws ContextException     上下文操作发生错误时将会抛出这个异常
     */
    void putByNode(String nodeName, Object value);

    /**
     * 从上下文中获取节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @return 当且仅当对应节点存在对象时, 则返回这个对象, 否则返回null</ br>
     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作</br>
     * key为"a"时将返回1</br>
     * key为"c.b"时将返回null</br>
     * key为"b"时将返回{"c":"2"}
     * @throws NullPointerException key为空时将会抛出这个异常
     * @throws ContextException     上下文操作发生错误时将会抛出这个异常
     */
    Object getByNode(String nodeName);

    Map<String, Object> toMap();


}
