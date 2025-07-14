package net.fanjr.simplify.el;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/13 上午10:23
 */
public class ElUtils {

    private static final Logger logger = LoggerFactory.getLogger(ElUtils.class);
    /**
     * 匹配#{xxx}和${xxx}两种形式的字符串
     */
    private static final Pattern PATTERN = Pattern.compile("[\\#\\$]\\{([^#{\\n}]*)([^${\\n}]*)\\}");

    /**
     * 获取表达式所有的变量节点名(不包括方法、常量、根节点等)
     *
     * @param el 表达式字符串
     * @return 变量列表
     */
    public static Set<String> getVariants(String el) {
        return getVariants(ELExecutor.compile(el));
    }

    /**
     * 获取表达式所有的变量节点名(不包括方法、常量、根节点等)
     *
     * @param el 表达式对象
     * @return 变量列表
     */
    public static Set<String> getVariants(EL el) {
        ELVariantsVisitor visitor = new ELVariantsVisitor();
        el.accept(visitor);
        return visitor.getVars();
    }


    public static boolean isElString(String str) {
        return PATTERN.matcher(str).find();
    }

    public static void foreachArray(Object array, Function<Object, Boolean> function) {
        int length = Array.getLength(array);
        for (int i = 0; i < length; i++) {
            Boolean result = function.apply(Array.get(array, i));
            if (null == result || !result) {
                // 返回为false时打破循环，否则执行到循环结束
                break;
            }
        }
    }
}
