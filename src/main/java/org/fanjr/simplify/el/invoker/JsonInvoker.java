package org.fanjr.simplify.el.invoker;


import org.fanjr.simplify.context.JsonContext;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr15662@hundsun.com
 * @file JsonInvoker.java
 * @since 2021/7/6 下午1:51
 */
public class JsonInvoker implements ELInvoker {
    private static final Map<String, JsonInvoker> POOL = new ConcurrentHashMap<>();
    private final List<Pair<ELInvoker, ELInvoker>> itemInvokers;
    private final String elString;

    private JsonInvoker(String elString, List<Pair<ELInvoker, ELInvoker>> itemInvokers) {
        this.elString = elString;
        if (null == itemInvokers) {
            this.itemInvokers = new ArrayList<>(0);
        } else {
            this.itemInvokers = itemInvokers;
        }
    }

    public static ELInvoker newInstance(String elString, List<Pair<ELInvoker, ELInvoker>> itemInvokers) {
        return POOL.computeIfAbsent(elString, (k) -> new JsonInvoker(elString, itemInvokers));
    }


    @Override
    public Object invoke(Object ctx) {
        JsonContext context = new JsonContext();
        int size = itemInvokers.size();
        for (int i = 0; i < size; i++) {
            String key = String.valueOf(itemInvokers.get(i).k.invoke(ctx));
            Object value = itemInvokers.get(i).v.invoke(ctx);
            context.put(key, value);
        }
        return context;
    }

    @Override
    public String toString() {
        return elString;
    }
}
