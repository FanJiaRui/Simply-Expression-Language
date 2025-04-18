package net.fanjr.simplify.el.invoker.calculate;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.invoker.node.NodeHolder;
import net.fanjr.simplify.el.invoker.node.NodeInvoker;
import net.fanjr.simplify.utils.$;

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
        BigDecimal oldVal = $.castToBigDecimal(nodeHolder.getValue());
        BigDecimal target = oldVal.subtract(BigDecimal.ONE);
        nodeHolder.setValue(target);
        //--i操作返回的是自减后的新值，先自增然后再返回
        return target;
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + "++";
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            nodeInvoker.accept(visitor);
        }
    }
}
