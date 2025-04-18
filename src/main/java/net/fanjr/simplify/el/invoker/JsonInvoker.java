package net.fanjr.simplify.el.invoker;


import com.alibaba.fastjson2.JSONObject;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.utils.Pair;
import net.fanjr.simplify.utils.SimplifyCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/6 下午1:51
 */
public class JsonInvoker implements ELInvoker {
    private static final SimplifyCache<String, JsonInvoker> POOL = new SimplifyCache<>(10000);
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
