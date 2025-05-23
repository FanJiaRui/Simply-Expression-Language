package net.fanjr.simplify.el.invoker.calculate;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.invoker.node.NodeHolder;
import net.fanjr.simplify.el.invoker.node.NodeInvoker;

import java.util.LinkedList;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午2:20
 */
public class SetValueInvoker implements ELInvoker {

    private final NodeInvoker nodeInvoker;

    private final ELInvoker elInvoker;


    private SetValueInvoker(NodeInvoker nodeInvoker, ELInvoker elInvoker) {
        this.nodeInvoker = nodeInvoker;
        this.elInvoker = elInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new SetValueInvoker((NodeInvoker) stack.pollFirst(), stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        //先计算value,避免因为++引起混乱
        Object value = elInvoker.invoke(ctx);
        NodeHolder nodeHolder = nodeInvoker.getNodeHolder(ctx);
        nodeHolder.setValue(value);
        return value;
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + " = " + elInvoker.toString();
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            nodeInvoker.accept(visitor);
        }
    }

}
