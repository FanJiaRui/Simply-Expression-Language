package net.fanjr.simplify.el.builder;


import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.invoker.ConstantInvoker;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/14 上午10:23
 */
public class BinocularBuilder extends ELInvokerBuilder {


    private final int tokenStart;
    private final int tokenEnd;
    private ELInvoker left;
    private ELInvoker right;

    public BinocularBuilder(int tokenStart, int tokenEnd, Function<LinkedList<ELInvoker>, ELInvoker> buildFunction) {
        super(2, buildFunction);
        this.tokenStart = tokenStart;
        this.tokenEnd = tokenEnd;
    }

    public void setLeft(ELInvoker left) {
        if (left == null) {
            this.left = ConstantInvoker.newInstance("null", null);
        } else {
            this.left = left;
        }
    }

    public void setRight(ELInvoker right) {
        if (right == null) {
            this.right = ConstantInvoker.newInstance("null", null);
        } else {
            this.right = right;
        }
    }

    @Override
    @Deprecated
    public void pushInvoker(ELInvoker invoker) {
        //skip
    }

    @Override
    public ELInvoker get() {
        super.pushInvoker(left);
        super.pushInvoker(right);
        return super.get();
    }

    public int getTokenStart() {
        return tokenStart;
    }

    public int getTokenEnd() {
        return tokenEnd;
    }
}
