package net.fanjr.simplify.utils;

import java.util.Objects;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/4/2 下午3:33
 */
public class Pair<K, V> {

    public final K k;

    public final V v;

    public Pair(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public static <K, V> Pair<K, V> of(K k, V v) {
        return new Pair<>(k, v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(k, pair.k) && Objects.equals(v, pair.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(k, v);
    }
}
