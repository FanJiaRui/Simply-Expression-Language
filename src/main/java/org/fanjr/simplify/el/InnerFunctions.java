package org.fanjr.simplify.el;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * 内部函数，表达式通过$.方法名来调用，例如$.println(xxx)
 * 在这里配置 /src/main/resources/META-INF/simplify-el.functions
 * @author fanjr@vip.qq.com
 *
 * @since 2024年4月17日 12:14:07
 */
public class InnerFunctions {

    private static final Logger logger = LoggerFactory.getLogger(InnerFunctions.class);

    /**
     * $.log(xxxxx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void log(Object object) {
        logger.info("{}", object);
    }

    /**
     * $.println(xxxxx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void println(Object object) {
        System.out.println(object);
    }

    /**
     * $.max(a,b)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.max(b);
    }

    /**
     * $.min(a,b)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.min(b);
    }

    /**
     * $.close(xx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void close(Object obj) {
        if (obj instanceof Closeable) {
            try {
                ((Closeable) obj).close();
            } catch (IOException e) {
                // skip
            }
        }
    }
}
