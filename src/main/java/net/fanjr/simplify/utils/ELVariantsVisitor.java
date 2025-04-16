package net.fanjr.simplify.utils;

import net.fanjr.simplify.el.EL;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.el.invoker.node.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 检索变量Visitor
 */
public class ELVariantsVisitor implements ELVisitor {

    private final Map<String, String> vars;

    public ELVariantsVisitor() {
        vars = new LinkedHashMap<>();
    }

    public Set<String> getVars() {
        return vars.keySet();
    }

    @Override
    public boolean visit(EL el) {
        return true;
    }

    @Override
    public boolean visit(ELInvoker invoker) {
        if (invoker instanceof Node) {
            if (((Node) invoker).isVariable()) {
                vars.put(invoker.toString(), "");
                return false;
            }
        }
        return true;
    }
}
