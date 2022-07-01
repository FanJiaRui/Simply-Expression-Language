package org.fanjr.simplify.el.invoker.calculate;

import com.hundsun.gaps.flowexecutor.el.ELInvoker;
import com.hundsun.gaps.flowexecutor.el.ElUtils;
import com.hundsun.gaps.flowexecutor.el.invoker.node.NodeHolder;
import com.hundsun.gaps.flowexecutor.el.invoker.node.NodeInvoker;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @author fanjr15662@hundsun.com
 * @file SetAndAddValueInvoker.java
 * @since 2021/7/9 下午2:52
 */
public class SetAndAddValueInvoker implements ELInvoker {

    private final NodeInvoker nodeInvoker;

    private final ELInvoker elInvoker;

    private SetAndAddValueInvoker(NodeInvoker nodeInvoker, ELInvoker elInvoker) {
        this.nodeInvoker = nodeInvoker;
        this.elInvoker = elInvoker;
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return new SetAndAddValueInvoker((NodeInvoker) stack.pollFirst(), stack.pollFirst());
    }

    @Override
    public Object invoke(Object ctx) {
        //先计算value2,避免因为++引起混乱
        Object val2 = elInvoker.invoke(ctx);
        NodeHolder nodeHolder = nodeInvoker.getNodeHolder(ctx);
        Object val1 = nodeHolder.getValue();
        BigDecimal target = ElUtils.castToBigDecimal(val1).add(ElUtils.castToBigDecimal(val2));
        nodeHolder.setValue(target);
        return target;
    }

    @Override
    public String toString() {
        return nodeInvoker.toString() + " = (" + nodeInvoker.toString() + " + " + elInvoker.toString() + ")";
    }
}
