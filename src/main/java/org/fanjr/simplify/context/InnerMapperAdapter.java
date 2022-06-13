package org.fanjr.simplify.context;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author fanjr@vip.qq.com
 * @file InnerMapperAdapter.java
 * @since 2022/5/24 下午4:34
 */
public abstract class InnerMapperAdapter<K, V> implements Map<K, V> {

    protected final Map<K, V> innerMap;

    protected InnerMapperAdapter(Map<K, V> innerMap) {
        this.innerMap = innerMap;
    }


    @Override
    public int size() {
        return innerMap.size();
    }

    @Override
    public boolean isEmpty() {
        return innerMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return innerMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return innerMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return innerMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        return innerMap.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return innerMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        innerMap.putAll(m);
    }

    @Override
    public void clear() {
        innerMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return innerMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return innerMap.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return innerMap.entrySet();
    }

    public Map<K, V> getInnerMap() {
        return this.innerMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InnerMapperAdapter<?, ?> that = (InnerMapperAdapter<?, ?>) o;
        return Objects.equals(innerMap, that.innerMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(innerMap);
    }
}
