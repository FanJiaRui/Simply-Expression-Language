package net.fanjr.simplify.el.invoker.node;

import net.fanjr.simplify.el.ELVisitor;

/**
 * 代表一个指定的节点
 *
 * @author fanjr@vip.qq.com
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

    /**
     * 目标对象的该节点移除
     *
     * @param ctx 对象
     */
    void removeNode(Object ctx);

    /**
     * 判断当前表达式是否为变量
     *
     * @return 如果当前表达式是变量，则返回true；否则返回false。
     */
    boolean isVariable();

    /**
     * 接受ELVisitor访问者
     *
     * @param visitor 访问者对象
     */
    void accept(ELVisitor visitor);
}
