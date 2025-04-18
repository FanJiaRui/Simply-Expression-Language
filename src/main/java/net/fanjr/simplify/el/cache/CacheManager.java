package net.fanjr.simplify.el.cache;

import net.fanjr.simplify.el.EL;
import net.fanjr.simplify.el.invoker.node.NodeInvoker;
import net.fanjr.simplify.utils.SimplifyCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存集中管理
 * @author fanjr@vip.qq.com
 */
public class CacheManager {

    private static final SimplifyCache<String, EL> COMPILES_EL = new SimplifyCache<>(10000);
    private static final SimplifyCache<String, NodeInvoker> COMPILES_NODE = new SimplifyCache<>(10000);
    private static final Map<Class<?>, SimplifyCache<String, ?>> POOL = new ConcurrentHashMap<>();
    public static EL getEL(String el) {
        return COMPILES_EL.get(el);
    }

    public static void putEL(String el, EL instance) {
        COMPILES_EL.put(el, instance);
    }

    public static NodeInvoker getNode(String node) {
        return COMPILES_NODE.get(node);
    }

    public static void putNode(String node, NodeInvoker instance) {
        COMPILES_NODE.put(node, instance);
    }

    public static <T> SimplifyCache<String, T> getPool(Class<T> type) {
        return cast(POOL.computeIfAbsent(type, (i) -> new SimplifyCache<String, T>(10000)));
    }

    public static <T> SimplifyCache<String, T> getPool(Class<T> type, int size) {
        return cast(POOL.computeIfAbsent(type, (i) -> new SimplifyCache<String, T>(size)));
    }

    private static <T> T cast(Object o) {
        //noinspection unchecked
        return (T) o;
    }
}
