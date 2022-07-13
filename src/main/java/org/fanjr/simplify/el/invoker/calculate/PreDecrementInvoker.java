package org.fanjr.simplify.el.invoker.calculate;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;
import org.fanjr.simplify.el.invoker.node.NodeHolder;
import org.fanjr.simplify.el.invoker.node.NodeInvoker;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午9:36
 */
public class PreDecrementInvoker implements ELInvoker {

    private final NodeInvoker nodeInvoker;

    private PreDecrementInvoker(NodeInvoker nodeInvoker) {
        this.nodeInvoker = nodeInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new PreDecrementInvoker((NodeInvoker) stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        NodeHolder nodeHolder = nodeInvoker.getNodeHolder(ctx);
        BigDecimal oldVal = ElUtils.castToBigDecimal(nodeHolder.getValue());
        BigDecimal target = oldVal.subtract(BigDecimal.ONE);
        nodeHolder.setValue(target);
        //--i操作返回的是自减后的新值，先自增然后再返回
        return target;
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + "++";
    }
}
