package net.fanjr.simplify.el.invoker;


import com.alibaba.fastjson2.JSONArray;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELVisitor;
import net.fanjr.simplify.utils.SimplifyCache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/5 下午2:23
 */
public class ArrayInvoker implements ELInvoker {

    private static final SimplifyCache<String, ArrayInvoker> POOL = new SimplifyCache<>(10000);
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

    @Override
    public void accept(ELVisitor visitor) {
        if (visitor.visit(this)) {
            for (ELInvoker itemInvoker : itemInvokers){
                itemInvoker.accept(visitor);
            }
        }
    }

}
