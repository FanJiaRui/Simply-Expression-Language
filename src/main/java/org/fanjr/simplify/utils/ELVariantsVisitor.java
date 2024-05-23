package org.fanjr.simplify.utils;

import org.fanjr.simplify.el.EL;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.invoker.node.Node;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 检索变量Visitor
 */
public class ELVariantsVisitor implements ELVisitor {

    private final Set<String> vars;

    public ELVariantsVisitor() {
        vars = new LinkedHashSet<>();
    }

    public Set<String> getVars() {
        return vars;
    }

    @Override
    public boolean visit(EL el) {
        return true;
    }

    @Override
    public boolean visit(ELInvoker invoker) {
        if (invoker instanceof Node) {
            if (((Node) invoker).isVariable()) {
                vars.add(invoker.toString());
                return false;
            }
        }
        return true;
    }
}
