package org.fanjr.simplify.el;

import org.fanjr.simplify.el.invoker.node.Node;

/**
 * 表达式节点树访问者
 */
public interface ELVisitor {

    /**
     * @return 是否继续向下访问，返回false时不再向下访问
     */
    boolean visit(EL el);

    /**
     * @return 是否继续向下访问，返回false时不再向下访问
     */
    boolean visit(ELInvoker invoker);

}
