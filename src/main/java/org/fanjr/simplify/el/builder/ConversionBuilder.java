package org.fanjr.simplify.el.builder;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.invoker.ClassInvoker;
import org.fanjr.simplify.el.invoker.ConversionInvoker;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;


/**
 * 转换器构造者
 *
 * @author fanjr15662@hundsun.com
 * @file ConversionBuilder.java
 * @since 2021/7/9 下午12:00
 */
public class ConversionBuilder {

    private static final Map<String, Supplier<Supplier<ELInvoker>>> POOL = new ConcurrentHashMap<>();

    static {
        addBuilderByType("(int)", int.class);
        addBuilderByType("(long)", long.class);
        addBuilderByType("(boolean)", boolean.class);
        addBuilderByType("(char)", char.class);
        addBuilderByType("(byte)", byte.class);
        addBuilderByType("(short)", short.class);
        addBuilderByType("(float)", float.class);
        addBuilderByType("(double)", double.class);
        addBuilderByType("(String)", String.class);
        addBuilderByType("(BigDecimal)", BigDecimal.class);
        addBuilder("(class)", () -> new ELInvokerBuilder(1, ClassInvoker::buildInstance));
    }

    public static Supplier<ELInvoker> matchBuild(char[] chars, int start, int end) {
        String matchStr = new String(chars, start, end - start);
        Supplier<Supplier<ELInvoker>> builder = POOL.get(matchStr);
        if (null != builder) {
            return builder.get();
        } else {
            return null;
        }
    }

    /**
     * 用于提供扩展能力
     *
     * @param key     关键字，必须用()包括
     * @param builder 构造方法
     */
    public static void addBuilder(String key, Supplier<Supplier<ELInvoker>> builder) {
        POOL.put(key, builder);
    }

    /**
     * 用于提供扩展能力
     *
     * @param key  关键字，必须用()包括
     * @param type 类型
     */
    public static void addBuilderByType(String key, Type type) {
        POOL.put(key, () -> new ELInvokerBuilder(1, (stack) -> ConversionInvoker.buildInstance(key, stack, type)));
    }

    public static Supplier<Supplier<ELInvoker>> getBuilder(String key) {
        return POOL.get(key);
    }

}
