package org.fanjr.simplify.el.invoker;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr15662@hundsun.com
 * @file ListInvoker.java
 * @since 2021/7/5 下午2:23
 */
public class ArrayInvoker implements ELInvoker {

    private static final Map<String, ArrayInvoker> POOL = new ConcurrentHashMap<>();
    private final List<ELInvoker> itemInvokers;
    private final String elString;

    private ArrayInvoker(String elString, List<ELInvoker> itemInvokers) {
        this.elString = elString;
        if (null == itemInvokers) {
            this.itemInvokers = new ArrayList<>(0);
        } else {
            this.itemInvokers = itemInvokers;
        }
    }

    public static ArrayInvoker newInstance(String elString, List<ELInvoker> itemInvokers) {
        return POOL.computeIfAbsent(elString, (k) -> new ArrayInvoker(elString, itemInvokers));
    }

    @Override
    public GapsArray invoke(Object ctx) {
        GapsArray array = new GapsArray(itemInvokers.size());
        int size = itemInvokers.size();
        for (int i = 0; i < size; i++) {
            array.add(itemInvokers.get(i).invoke(ctx));
        }
        return array;
    }

    @Override
    public String toString() {
        return elString;
    }


}
