package org.fanjr.simplify.el.invoker.calculate;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;
import org.fanjr.simplify.el.invoker.node.NodeHolder;
import org.fanjr.simplify.el.invoker.node.NodeInvoker;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @file IncrementInvoker.java
 * @since 2021/7/9 下午9:36
 */
public class IncrementInvoker implements ELInvoker {

    private final NodeInvoker nodeInvoker;

    private IncrementInvoker(NodeInvoker nodeInvoker) {
        this.nodeInvoker = nodeInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new IncrementInvoker((NodeInvoker) stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        NodeHolder nodeHolder = nodeInvoker.getNodeHolder(ctx);
        BigDecimal oldVal = ElUtils.castToBigDecimal(nodeHolder.getValue());
        BigDecimal target = oldVal.add(BigDecimal.ONE);
        nodeHolder.setValue(target);
        //i++操作返回的是i的原值，先返回然后在自增
        return oldVal;
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + "++";
    }
}
