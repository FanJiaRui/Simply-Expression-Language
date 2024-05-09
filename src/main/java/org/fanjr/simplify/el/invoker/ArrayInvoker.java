package org.fanjr.simplify.el.invoker;


import com.alibaba.fastjson2.JSONArray;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.cache.ConcurrentCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/5 下午2:23
 */
public class ArrayInvoker implements ELInvoker {

    private static final ConcurrentCache<String, ArrayInvoker> POOL = new ConcurrentCache<>(10000);
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
    public List<Object> invoke(Object ctx) {
        List<Object> array = new JSONArray();
        for (ELInvoker itemInvoker : itemInvokers) {
            array.add(itemInvoker.invoke(ctx));
        }
        return array;
    }

    @Override
    public String toString() {
        return elString;
    }


}
