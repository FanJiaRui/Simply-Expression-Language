package org.fanjr.simplify.el.invoker.statement;

import com.alibaba.fastjson2.util.TypeUtils;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;

public class IfElseStatementInvoker implements ELInvoker {

    private final ELInvoker exp;

    private final ELInvoker ifBlock;

    private final ELInvoker elseBlock;


    private IfElseStatementInvoker(ELInvoker exp, ELInvoker ifBlock, ELInvoker elseBlock) {
        this.exp = exp;
        this.ifBlock = ifBlock;
        this.elseBlock = elseBlock;
    }

    public static ELInvoker buildIf(ELInvoker exp, ELInvoker ifBlock) {
        return new IfElseStatementInvoker(exp, ifBlock, null);
    }

    public static ELInvoker buildIfElse(ELInvoker exp, ELInvoker ifBlock, ELInvoker elseBlock) {
        return new IfElseStatementInvoker(exp, ifBlock, elseBlock);
    }

    @Override
    public Object invoke(Object ctx) {
        if (TypeUtils.cast(exp.invoke(ctx), boolean.class)) {
            return ifBlock.invoke(ctx);
        } else {
            if (null == elseBlock) {
                return null;
            }
            return elseBlock.invoke(ctx);
        }
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            exp.accept(visitor);
            ifBlock.accept(visitor);
            if (elseBlock != null) {
                elseBlock.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        if (elseBlock != null) {
            return "if (" + exp.toString() + ") {\n\t" + ifBlock.toString() + "\n} else {\n\t" + elseBlock + "\n}";
        } else {
            return "if (" + exp.toString() + ") {\n\t" + ifBlock.toString() + "\n}";
        }
    }

}
