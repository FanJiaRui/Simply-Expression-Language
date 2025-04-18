package net.fanjr.simplify.el;

import net.fanjr.simplify.el.invoker.node.Node;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * 检索变量Visitor
 * @author fanjr@vip.qq.com
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
