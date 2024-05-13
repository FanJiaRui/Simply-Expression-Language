package org.fanjr.simplify.el.invoker;


import com.alibaba.fastjson2.JSONObject;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELVisitor;
import org.fanjr.simplify.el.cache.ConcurrentCache;
import org.fanjr.simplify.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/6 下午1:51
 */
public class JsonInvoker implements ELInvoker {
    private static final ConcurrentCache<String, JsonInvoker> POOL = new ConcurrentCache<>(10000);
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
    public JSONObject invoke(Object ctx) {
        JSONObject json = new JSONObject();
        for (Pair<ELInvoker, ELInvoker> itemInvoker : itemInvokers) {
            String key = String.valueOf(itemInvoker.k.invoke(ctx));
            Object value = itemInvoker.v.invoke(ctx);
            json.put(key, value);
        }
        return json;
    }

    @Override
    public String toString() {
        return elString;
    }

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            for (Pair<ELInvoker, ELInvoker> entry : itemInvokers) {
                entry.k.accept(visitor);
                entry.v.accept(visitor);
            }
        }
    }
}
