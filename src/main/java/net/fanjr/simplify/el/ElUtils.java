package net.fanjr.simplify.el;

import net.fanjr.simplify.el.reflect.ELFunctionInvokeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/13 上午10:23
 */
public class ElUtils {

    private static final String SIMPLIFY_EL_FUNCTIONS = "META-INF/simplify-el.functions";
    private static final Logger logger = LoggerFactory.getLogger(ElUtils.class);
    /**
     * 匹配#{xxx}和${xxx}两种形式的字符串
     */
    private static final Pattern PATTERN = Pattern.compile("[\\#\\$]\\{([^#{\\n}]*)([^${\\n}]*)\\}");

    static {
        init();
    }

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


    private synchronized static void init() {
        try {
            ClassLoader classLoader = ElUtils.class.getClassLoader();
            final Enumeration<URL> functionsUrl =
                    classLoader.getResources(SIMPLIFY_EL_FUNCTIONS);

            while (functionsUrl.hasMoreElements()) {
                Properties functionsProperties = new Properties();
                final URL url = functionsUrl.nextElement();
                try (InputStream inputStream = url.openStream()) {
                    functionsProperties.load(inputStream);
                }
                for (Map.Entry<?, ?> entry : functionsProperties.entrySet()) {
                    final String utilsName = ((String) entry.getKey()).trim();
                    final String utilsClassName = ((String) entry.getValue()).trim();
                    for (String s : utilsClassName.split(",")) {
                        try {
                            ELFunctionInvokeUtils.addFunctionClass(utilsName, classLoader.loadClass(s));
                        } catch (Exception e) {
                            // SKIP
                            logger.warn("加载表达式Functions发生异常，可能并不影响使用，但请排查是否存在预期外的加载。", e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // SKIP
            logger.warn("加载表达式Functions发生异常，可能并不影响使用，但请排查是否存在预期外的加载。", e);
        }
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
