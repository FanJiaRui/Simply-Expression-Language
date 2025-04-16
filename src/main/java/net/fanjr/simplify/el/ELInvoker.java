package net.fanjr.simplify.el;

/**
 * EL表达式调用接口
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 上午11:53
 */
public interface ELInvoker {

    /**
     * 执行表达式并返回结果
     *
     * @param ctx 上下文对象
     * @return 表达式执行结果
     */
    Object invoke(Object ctx);

    /**
     * 接受访问者对象
     *
     * @param visitor 访问者对象
     */
    void accept(ELVisitor visitor);
}
