package net.fanjr.simplify.el.invoker;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;

import java.util.List;

import static net.fanjr.simplify.el.builder.KeywordBuilder.BREAK_FLAG;

/**
 * 组合计算，用于支持多个语句组合的情况用分号隔开多个语句，例如 el1;el2;el3
 *
 * @author fanjr@vip.qq.com
 * @since 2021/7/12 上午9:29
 */
public class CompositeInvoker implements ELInvoker {

    private final List<ELInvoker> subInvokers;

    private CompositeInvoker(List<ELInvoker> subInvokers) {
        this.subInvokers = subInvokers;
    }

    public static CompositeInvoker newInstance(List<ELInvoker> subInvokers) {
        return new CompositeInvoker(subInvokers);
    }

    @Override
    public Object invoke(Object ctx) {
        Object target = null;
        for (ELInvoker elInvoker : subInvokers) {
            //覆盖之前的计算结果，只需要最后一个结果进行返回
            target = elInvoker.invoke(ctx);
            // break; 直接跳过语句块后面内容
            if (BREAK_FLAG == target) {
                return target;
            }
        }
        return target;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ELInvoker elInvoker : subInvokers) {
            sb.append(elInvoker.toString()).append(';');
        }
        return sb.toString();
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            for (ELInvoker itemInvoker : subInvokers){
                itemInvoker.accept(visitor);
            }
        }
    }

}
