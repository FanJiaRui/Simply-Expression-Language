package net.fanjr.simplify.el;

/**
 * 表达式节点树访问者
 *
 * <p>用于遍历和操作表达式节点树的接口。</p>
 *
 * @author fanjr@vip.qq.com
 */
public interface ELVisitor {

    /**
     * 访问表达式节点。
     *
     * @param el 表达式节点
     * @return 是否继续向下访问，返回false时不再向下访问
     */
    boolean visit(EL el);

    /**
     * 访问表达式调用节点。
     *
     * @param invoker 表达式调用节点
     * @return 是否继续向下访问，返回false时不再向下访问
     */
    boolean visit(ELInvoker invoker);

}
