package org.fanjr.simplify.el.cache;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 缓存参考自tomcat org.apache.el.util.ConcurrentCache，做了一点改进适配当前场景
 * 并发非常频繁的时候,可能导致短时间内实际容量超过
 *
 * @author fanjr@vip.qq.com
 */
public final class ConcurrentCache<K, V> {

    private final int size;

    private final Map<K, V> eden;

    private final Map<K, V> longterm;

    public ConcurrentCache(int size) {
        this.size = size;
        this.eden = new ConcurrentHashMap<>(size);
        this.longterm = new WeakHashMap<>(size);
    }

    public V get(K k) {
        V v = this.eden.get(k);
        if (v == null) {
            synchronized (longterm) {
                v = this.longterm.get(k);
            }
            if (v != null) {
                synchronized (longterm) {
                    if (this.eden.size() >= size) {
                        this.longterm.putAll(this.eden);
                        this.eden.clear();
                    }
                }
                this.eden.put(k, v);
            }
        }
        return v;
    }

    public void put(K k, V v) {
        if (this.eden.size() >= size) {
            synchronized (longterm) {
                if (this.eden.size() >= size) {
                    this.longterm.putAll(this.eden);
                    this.eden.clear();
                }
            }
        }
        this.eden.put(k, v);
    }


    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        V oldValue, newValue;
        oldValue = get(key);
        if (null != oldValue) {
            return oldValue;
        }

        newValue = mappingFunction.apply(key);
        if (null == newValue) {
            return null;
        }


        if (this.eden.size() >= size) {
            synchronized (longterm) {
                if (this.eden.size() >= size) {
                    this.longterm.putAll(this.eden);
                    this.eden.clear();
                }
            }
        }

        oldValue = this.eden.putIfAbsent(key, newValue);
        return null == oldValue ? newValue : oldValue;
    }

}