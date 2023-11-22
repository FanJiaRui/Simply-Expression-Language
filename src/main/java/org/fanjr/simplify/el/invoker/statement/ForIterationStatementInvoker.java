package org.fanjr.simplify.el.invoker.statement;

import com.alibaba.fastjson2.JSONArray;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.invoker.node.Node;
import org.fanjr.simplify.utils.ElUtils;

import java.util.Collection;
import java.util.Map;

import static org.fanjr.simplify.el.builder.KeywordBuilder.BREAK_FLAG;

public class ForIterationStatementInvoker implements ELInvoker {

    private final ELInvoker iteration;

    private final Node item;

    private final ELInvoker forBlock;

    private ForIterationStatementInvoker(ELInvoker iteration, Node item, ELInvoker forBlock) {
        this.iteration = iteration;
        this.item = item;
        this.forBlock = forBlock;
    }

    public static ELInvoker buildFor(ELInvoker iteration, Node item, ELInvoker forBlock) {
        return new ForIterationStatementInvoker(iteration, item, forBlock);
    }

    @Override
    public Object invoke(Object ctx) {
        Object[] target = new Object[1];
        Object source = iteration.invoke(ctx);
        if (null == source) {
            return null;
        }
        if (source instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) source).entrySet()) {
                item.putNode(ctx, entry);
                target[0] = forBlock.invoke(ctx);
                if (BREAK_FLAG == target[0]) {
                    break;
                }
            }
        } else if (source instanceof Collection) {
            for (Object entry : (Collection<?>) source) {
                item.putNode(ctx, entry);
                target[0] = forBlock.invoke(ctx);
                if (BREAK_FLAG == target[0]) {
                    break;
                }
            }
        } else if (source.getClass().isArray()) {
            ElUtils.foreachArray(source, (itemValue) -> {
                item.putNode(ctx, itemValue);
                target[0] = forBlock.invoke(ctx);
                return BREAK_FLAG != target[0];
            });
        } else if (source instanceof String) {
            String str = ((String) source).trim();
            if (str.charAt(0) == '[') {
                JSONArray jsonArray = JSONArray.parse(str);
                for (Object entry : jsonArray) {
                    item.putNode(ctx, entry);
                    target[0] = forBlock.invoke(ctx);
                    if (BREAK_FLAG == target[0]) {
                        break;
                    }
                }
            }
        } else if (source instanceof Number) {
            int num = ((Number) source).intValue();
            for (int i = 0; i < num; i++) {
                item.putNode(ctx, i);
                target[0] = forBlock.invoke(ctx);
                if (BREAK_FLAG == target[0]) {
                    break;
                }
            }
        } else {
            item.putNode(ctx, source);
            target[0] = forBlock.invoke(ctx);
        }

        return target[0];
    }
}
