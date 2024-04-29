package org.fanjr.simplify.el.cache;

import org.fanjr.simplify.el.EL;
import org.fanjr.simplify.el.invoker.node.NodeInvoker;

/**
 * 缓存集中管理
 * TODO 后续考虑把缓存大小做成可配置化
 * @author fanjr@vip.qq.com
 */
public class ELCacheManager {

    private static final ConcurrentCache<String, EL> COMPILES_EL = new ConcurrentCache<>(10000);
    private static final ConcurrentCache<String, NodeInvoker> COMPILES_NODE = new ConcurrentCache<>(10000);


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


}
