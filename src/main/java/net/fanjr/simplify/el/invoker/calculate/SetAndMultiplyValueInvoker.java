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
 * @since 2021/7/9 下午8:41
 */
public class SetAndMultiplyValueInvoker implements ELInvoker {

    private final NodeInvoker nodeInvoker;

    private final ELInvoker elInvoker;

    private SetAndMultiplyValueInvoker(NodeInvoker nodeInvoker, ELInvoker elInvoker) {
        this.nodeInvoker = nodeInvoker;
        this.elInvoker = elInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new SetAndMultiplyValueInvoker((NodeInvoker) stack.pollFirst(), stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        //先计算value2,避免因为++引起混乱
        Object val2 = elInvoker.invoke(ctx);
        NodeHolder nodeHolder = nodeInvoker.getNodeHolder(ctx);
        Object val1 = nodeHolder.getValue();
        BigDecimal num1 = $.castToBigDecimal(val1);
        BigDecimal num2 = $.castToBigDecimal(val2);
        BigDecimal target = num1.multiply(num2);
        nodeHolder.setValue(target);
        return target;
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            nodeInvoker.accept(visitor);
            elInvoker.accept(visitor);
        }
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + " = (" + nodeInvoker + " * " + elInvoker.toString() + ")";
    }
}
