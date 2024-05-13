package org.fanjr.simplify.el;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 上午11:53
 */
public interface ELInvoker {

    Object invoke(Object ctx);

    void accept(ELVisitor visitor);
}
