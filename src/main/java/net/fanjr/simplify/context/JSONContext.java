package net.fanjr.simplify.context;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELExecutor;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 上下文默认实现
 *
 * @author fanjr@vip.qq.com
 * @since 2022/5/19 下午4:59
 */
class JSONContext extends JSONObject implements SContext {

    private static final long serialVersionUID = 1L;

    JSONContext() {
        //skip
    }

    public JSONContext(int initialCapacity) {
        super(initialCapacity);
    }

    public JSONContext(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public JSONContext(int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }


    @SuppressWarnings("unchecked")
    public JSONContext(Map context) {
        putAll(context);
    }

    @Override
    public JSONContext clone() {
        return new JSONContext(this);
    }

    @Override
    public Object eval(String el) {
        try {
            return ELExecutor.eval(el, this);
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    @Override
    public Object evalOrDefault(String el, Object defaultValue) {
        try {
            Object eval = ELExecutor.eval(el, this);
            return null == eval ? defaultValue : eval;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public <T> T evalOrDefault(String el, T defaultValue, Class<T> type) {
        try {
            T eval = ELExecutor.eval(el, this, type);
            return null == eval ? defaultValue : eval;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    public Object eval(String el, Type type) {
        try {
            return ELExecutor.eval(el, this, type);
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    @Override
    public <T> T eval(String el, Class<T> type) {
        try {
            return ELExecutor.eval(el, this, type);
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> getList(String nodeName) {
        Object obj = getNode(nodeName);
        if (null == obj) {
            return new JSONArray();
        }
        if (obj instanceof List) {
            return (List<Object>) obj;
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            List<Object> target = new JSONArray(len);
            for (int i = 0; i < len; i++) {
                target.add(Array.get(obj, i));
            }
            return target;
        } else {
            List<Object> list = new JSONArray();
            list.add(obj);
            return list;
        }
    }

    @Override
    public <T> List<T> getList(String nodeName, Class<T> type) {
        Object obj = getNode(nodeName);
        if (null == obj) {
            return new ArrayList<>();
        }
        if (obj instanceof JSONArray) {
            return ((JSONArray) obj).toJavaList(type);
        }
        if (obj instanceof List) {
            return new JSONArray((Collection<?>) obj).toJavaList(type);
        } else if (obj.getClass().isArray()) {
            int len = Array.getLength(obj);
            JSONArray target = new JSONArray(len);
            for (int i = 0; i < len; i++) {
                target.add(Array.get(obj, i));
            }
            return target.toJavaList(type);
        } else {
            JSONArray target = new JSONArray();
            target.add(obj);
            return target.toJavaList(type);
        }
    }

    /**
     * 从上下文中获取节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @return 当且仅当对应节点存在对象时, 则返回这个对象, 否则返回null</ br>
     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作</br>
     * nodeName为"a"时将返回1</br>
     * nodeName为"c.b"时将返回null</br>
     * nodeName为"b"时将返回{"c":"2"}
     */
    @Override
    public Object getNode(String nodeName) {
        try {
            return ELExecutor.compileNode(nodeName).getNode(this);
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::putNode);
    }

    /**
     * 将变量value放入上下文中对应节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     * @param value    需要存储的变量
     * @throws NullPointerException nodeName为空时将会抛出这个异常
     */
    @Override
    public SContext putNode(String nodeName, Object value) {
        try {
            ELExecutor.compileNode(nodeName).putNode(this, value);
            return this;
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    /**
     * 从上下文中移除节点
     *
     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
     *                 例如:对上下文{"a":1,"b":{"c":"2"}}执行remove操作</br>
     *                 nodeName为"a"时上下文变为{"b":{"c":"2"}}</br>
     *                 nodeName为"c.b"时上下文不变化</br>
     */
    @Override
    public void removeNode(String nodeName) {
        try {
            ELExecutor.compileNode(nodeName).removeNode(this);
        } catch (Exception e) {
            throw new ELException(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> toMap() {
        return this;
    }

    @Override
    public JSONObject toJSON() {
        return this;
    }

}
