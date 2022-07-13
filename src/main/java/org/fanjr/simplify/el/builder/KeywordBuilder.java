package org.fanjr.simplify.el.builder;


import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.invoker.ConstantInvoker;
import org.fanjr.simplify.el.invoker.calculate.EqualsInvoker;
import org.fanjr.simplify.el.invoker.node.RootNodeInvoker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


/**
 * 关键字构造者
 *
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 下午12:00
 */
public class KeywordBuilder {

    private static final Map<String, Supplier<ELInvoker>> POOL = new ConcurrentHashMap<>();

    static {
        addBuilder("blank", () -> ConstantInvoker.newInstance("blank", EqualsInvoker.BLANK_FLAG));
        addBuilder("empty", () -> ConstantInvoker.newInstance("empty", EqualsInvoker.EMPTY_FLAG));
        addBuilder("null", () -> ConstantInvoker.newInstance("null", null));
        addBuilder("true", () -> ConstantInvoker.newInstance("true", true));
        addBuilder("false", () -> ConstantInvoker.newInstance("false", false));
        addBuilder("this", () -> RootNodeInvoker.INSTANCE);
    }


    public static Supplier<ELInvoker> matchBuild(char[] chars, int start, int end) {
        String matchStr = new String(chars, start, end - start);
        return POOL.get(matchStr);
    }

    /**
     * 用于提供扩展能力
     *
     * @param key     关键字
     * @param builder 构造方法
     */
    public static void addBuilder(String key, Supplier<ELInvoker> builder) {
        POOL.put(key, builder);
    }
}
