package org.fanjr.simplify.el;

import com.alibaba.fastjson2.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * 具体EL的实现类
 *
 * @author fanjr@vip.qq.com
 * @file Invoker.java
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
}
