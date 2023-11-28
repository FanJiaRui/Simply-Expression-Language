package org.fanjr.simplify.el.invoker.statement;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.ElUtils;

import static org.fanjr.simplify.el.builder.KeywordBuilder.BREAK_FLAG;

public class ForIndexStatementInvoker implements ELInvoker {

    private final ELInvoker preEL;
    private final ELInvoker condition;
    private final ELInvoker endEL;
    private final ELInvoker forBlock;

    private ForIndexStatementInvoker(ELInvoker preEl, ELInvoker condition, ELInvoker endEL, ELInvoker forBlock) {
        this.preEL = preEl;
        this.condition = condition;
        this.endEL = endEL;
        this.forBlock = forBlock;
    }

    public static ELInvoker buildFor(ELInvoker preEl, ELInvoker condition, ELInvoker endEl, ELInvoker forBlock) {
        return new ForIndexStatementInvoker(preEl, condition, endEl, forBlock);
    }

    @Override
    public Object invoke(Object ctx) {
        for (preEL.invoke(ctx); ElUtils.cast(condition.invoke(ctx), boolean.class); endEL.invoke(ctx)) {
            if (BREAK_FLAG == forBlock.invoke(ctx)) {
                break;
            }
        }
        // 循环结束统一返回 null
        return null;
    }

}
