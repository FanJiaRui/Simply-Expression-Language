package org.fanjr.simplify.el;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 复合EL表达式
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/25 下午4:14
 */
public class SpliceEL implements EL {

    private final List<ELInvoker> invokers;

    public SpliceEL(List<ELInvoker> invokers) {
        this.invokers = invokers;
    }

    public Object invoke(Object ctx) {
        StringBuilder sb = new StringBuilder();
        for (ELInvoker invoker : invokers) {
            Object target = invoker.invoke(ctx);
            if (target instanceof Map) {
                sb.append(JSONObject.toJSONString(target));
            } else if (null != target) {
                sb.append(target);
            }
        }
        return sb.toString();
    }

    @Override
    public void accept(ELVisitor visitor) {
        // 访问自身
        if (visitor.visit(this)) {
            for (ELInvoker invoker : invokers) {
                // 访问子节点
                invoker.accept(visitor);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ELInvoker invoker : invokers) {
            sb.append("[EL: ").append(invoker.toString()).append("]");
        }
        return sb.toString();
    }
}
