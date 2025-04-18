package net.fanjr.simplify.context;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderImplMap;
import net.fanjr.simplify.utils.$;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 简易上下文，方便内部节点的快速访问，支持EL表达式
 *
 * @author fanjr@vip.qq.com
 * @since 2022/5/19 下午4:59
 */
public interface SContext {

    Type MAP_STRING_OBJECT = new TypeReference<Map<String, Object>>() {
    }.getType();
    Type MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
    }.getType();
    ObjectReader<JSONContext> OBJECT_READER = ObjectReaderImplMap.of(MAP_STRING_OBJECT, JSONContext.class, 0);

    /**
     * 获取上下文实例
     */
    static SContext of() {
        return new JSONContext();
    }

    static SContext of(int initialCapacity) {
        return new JSONContext(initialCapacity);
    }

    /**
     * 根据K-V生成上下文
     */
    static SContext of(String key, Object value) {
        SContext object = new JSONContext(2);
        object.putNode(key, value);
        return object;
    }

    /**
     * 根据K-V生成上下文
     */
    static SContext of(String k1, Object v1, String k2, Object v2) {
        SContext object = new JSONContext(3);
        object.putNode(k1, v1);
        object.putNode(k2, v2);
        return object;
    }

    /**
     * 根据K-V生成上下文
     */
    static SContext of(String k1, Object v1, String k2, Object v2, String k3, Object v3) {
        SContext object = new JSONContext(5);
        object.putNode(k1, v1);
        object.putNode(k2, v2);
        object.putNode(k3, v3);
        return object;
    }

    static SContext toContext(Object obj) {
        if (null == obj) {
            return SContext.of();
        }

        if (obj instanceof SContext) {
            return (SContext) obj;
        }

        if (obj instanceof Map) {
            return new JSONContext((Map) obj);
        }

        if (obj instanceof String) {
            return new JSONContext(JSON.parseObject((String) obj));
        }

        return $.cast(JSON.toJSONString(obj), SContext.class);
    }

    /**
     * 执行表达式
     *
     * @param el 表达式
     * @return 执行结果
     */
    Object eval(String el);

    /**
     * 执行表达式
     *
     * @param el   表达式
     * @param type 预期结果类型
     * @return 执行结果
     */
    Object eval(String el, Type type);

    /**
     * 执行表达式
     *
     * @param el   表达式
     * @param type 预期结果类型
     * @return 执行结果
     */
    <T> T eval(String el, Class<T> type);

    /**
     * 执行表达式，若执行失败或执行结果为NULL则返回默认值
     *
     * @param el           表达式
     * @param defaultValue 默认值
     * @return 执行结果
     */
    Object evalOrDefault(String el, Object defaultValue);

    /**
     * 执行表达式，若执行失败或执行结果为NULL则返回默认值
     *
     * @param el           表达式
     * @param defaultValue 默认值
     * @param type         预期结果类型
     * @return 执行结果
     */
    <T> T evalOrDefault(String el, T defaultValue, Class<T> type);

    /**
     * 获取节点并转换为上下文
     *
     * @link net.fanjr.simplify.context.SContext#getByNode(java.lang.String)
     */
    default SContext getContext(String nodeName) {
        Object obj = getNode(nodeName);
        if (null == obj) {
            return SContext.of();
        }
        return SContext.toContext(obj);
    }

    /**
     * 从上下文中获取节点并转换为List结构
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @return 当对应节点存在这个对象时, 且为List时直接返回, 否则转换为List, 若不存在对象则返回空List<br>
     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作</br>
     * key为"a"时将返回[1]</br>
     * key为"c.b"时将返回[]</br>
     * @throws NullPointerException key为空时将会抛出这个异常
     */
    List<Object> getList(String nodeName);

    /**
     * 从上下文中获取节点并转换为List结构，同时将List每一个对象转换为固定类型
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @return 当对应节点存在这个对象时, 且为List时直接返回, 否则转换为List, 若不存在对象则返回空List<br>
     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作</br>
     * key为"a"时将返回[1]</br>
     * key为"c.b"时将返回[]</br>
     * @throws NullPointerException key为空时将会抛出这个异常
     */
    <T> List<T> getList(String nodeName, Class<T> type);

    /**
     * 从上下文中获取节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @return 当且仅当对应节点存在对象时, 则返回这个对象, 否则返回null<br>
     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作<br>
     * key为"a"时将返回1<br>
     * key为"c.b"时将返回null<br>
     * key为"b"时将返回{"c":"2"}
     * @throws NullPointerException key为空时将会抛出这个异常
     */
    Object getNode(String nodeName);

    /**
     * 获取节点并转换类型
     *
     * @link net.fanjr.simplify.context.SContext#getByNode(java.lang.String)
     */
    default <T> T getNode(String nodeName, Class<T> type) {
        Object object = getNode(nodeName);
        return $.cast(object, type);
    }

    /**
     * 获取节点并转换类型
     *
     * @link net.fanjr.simplify.context.SContext#getByNode(java.lang.String)
     */
    default Object getNode(String nodeName, Type type) {
        Object object = getNode(nodeName);
        return $.cast(object, type);
    }

    /**
     * 将变量value放入上下文中对应节点,并返回这个上下文
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @param value    需要存储的变量
     * @return 执行put方法时对已经存储的对象进行了覆盖, 则返回这个对象, 否则返回null<br>
     * 例如:对上下文{}执行put操作<br>
     * key为"a.b",value为"xxx"时上下文内容将变成{"a":{"b":"xxx"}}<br>
     * @return 返回自身
     * @throws NullPointerException key为空时将会抛出这个异常
     */
    SContext putNode(String nodeName, Object value);

    /**
     * 从上下文中移除节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     *                 例如:对上下文{"a":1,"b":{"c":"2"}}执行remove操作</br>
     *                 nodeName为"a"时上下文变为{"b":{"c":"2"}}</br>
     *                 nodeName为"c.b"时上下文不变化</br>
     */
    void removeNode(String nodeName);

    default Object to(Type type) {
        return $.cast(this, type);
    }

    default <T> T to(Class<T> type) {
        return $.cast(this, type);
    }

    /**
     * 将上下文转换为Map
     *
     * @return
     */
    Map<String, Object> toMap();

    /**
     * 将上下文转换为JsonObject
     *
     * @return
     */
    JSONObject toJSON();

    /**
     * 若上下文为空返回true,否则返回false.
     */
    boolean isEmpty();

    /**
     * 清空上下文
     */
    void clear();
}
