package org.fanjr.simplify.el;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 内部函数，表达式通过$.方法名来调用，例如$.println(xxx)
 * 在这里配置 /src/main/resources/META-INF/simplify-el.functions
 *
 * @author fanjr@vip.qq.com
 * @since 2024年4月17日 12:14:07
 */
public class InnerFunctions {

    private static final Logger logger = LoggerFactory.getLogger(InnerFunctions.class);

    /**
     * 日志框架输出
     * $.log(xxxxx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void log(Object object) {
        logger.info("{}", object);
    }

    /**
     * 控制台输出
     * $.println(xxxxx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void println(Object object) {
        System.out.println(object);
    }

    /**
     * 取两数大值
     * $.max(a,b)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static BigDecimal max(BigDecimal a, BigDecimal b) {
        return a.max(b);
    }

    /**
     * 取两数小值
     * $.min(a,b)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static BigDecimal min(BigDecimal a, BigDecimal b) {
        return a.min(b);
    }

    /**
     * 关闭资源
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

    /**
     * 获取枚举映射
     * $.getEnumMap(xx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static Map<String, Enum<?>> getEnumMap(Object enumObject) {
        Map<String, Enum<?>> target = new HashMap<>();
        Class<?> enumClass;
        if (enumObject instanceof String) {
            try {
                enumClass = Class.forName((String) enumObject);
            } catch (ClassNotFoundException e) {
                throw new ElException("无法转换为枚举类型", e);
            }
        } else if (enumObject instanceof Class) {
            enumClass = (Class<?>) enumObject;
        } else if (enumObject instanceof Enum) {
            enumClass = ((Enum<?>) enumObject).getDeclaringClass();
        } else {
            throw new ElException("无法转换为枚举类型，无法获取对应映射关系！");
        }

        if (enumClass.isEnum()) {
            Object[] enumConstants = enumClass.getEnumConstants();
            for (Object e : enumConstants) {
                String name = ((Enum<?>) e).name();
                target.put(name, (Enum<?>) e);
            }
            return target;
        } else {
            throw new ElException("无法转换为枚举类型，无法获取对应映射关系！");
        }
    }

    /**
     * 深度合并多个MAP，若其中存在JSON数组则按照index进行合并，后面的MAP覆盖前面的
     * $.merge([map1,map2,map3...])
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static Map<String, Object> merge(Map<?, ?>... maps) {
        JSONObject target = new JSONObject();
        for (Map<?, ?> map : maps) {
            Properties properties = PropertiesUtils.toProperties(map);
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                ELExecutor.putNode(target, String.valueOf(entry.getKey()), entry.getValue());
            }
        }
        return target;
    }

}
