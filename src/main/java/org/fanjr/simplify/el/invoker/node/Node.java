package org.fanjr.simplify.el.invoker.node;

/**
 * 代表一个指定的节点
 *
 * @author fanjr@vip.qq.com
 * @file Node.java
 * @since 2022/7/5 上午9:33
 */
public interface Node {

    /**
     * 从对象中获得该节点值
     *
     * @param ctx 对象
     * @return 该对象在该节点中的值
     */
    Object getNode(Object ctx);

    /**
     * 将值放入目标对象的该节点中
     *
     * @param ctx   对象
     * @param value 要放入对象该节点的值
     */
    void putNode(Object ctx, Object value);
}
