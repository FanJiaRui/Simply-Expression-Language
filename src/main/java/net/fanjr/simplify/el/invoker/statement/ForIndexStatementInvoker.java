package net.fanjr.simplify.el.invoker.statement;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.builder.KeywordBuilder;
import net.fanjr.simplify.utils.ElUtils;

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
            if (KeywordBuilder.BREAK_FLAG == forBlock.invoke(ctx)) {
                break;
            }
        }
        // 循环结束统一返回 null
        return null;
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)){
            preEL.accept(visitor);
            condition.accept(visitor);
            endEL.accept(visitor);
            forBlock.accept(visitor);
        }
    }

}
