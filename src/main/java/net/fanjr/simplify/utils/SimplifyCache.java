package net.fanjr.simplify.utils;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 缓存参考自tomcat org.apache.el.util.ConcurrentCache，做了一点改进适配当前场景
 * 并发非常频繁且大量增加的时候，可能导致短时间内实际容量超过指定大小，但这不重要
 * 当前工程场景中引入分代缓存的目标是为了避免垃圾对象无限膨胀而不是精确控制到垃圾最多达到多少个
 *
 * @author fanjr@vip.qq.com
 */
public final class SimplifyCache<K, V> {

    private final int size;

    private final Map<K, V> eden;

    private final Map<K, V> longterm;

    public SimplifyCache(int size) {
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