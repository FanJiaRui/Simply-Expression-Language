package net.fanjr.simplify.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.FieldReader;
import com.alibaba.fastjson2.reader.ObjectReader;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import com.alibaba.fastjson2.util.TypeUtils;
import com.alibaba.fastjson2.writer.FieldWriter;
import com.alibaba.fastjson2.writer.ObjectWriter;
import com.alibaba.fastjson2.writer.ObjectWriterProvider;
import net.fanjr.simplify.context.SContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static net.fanjr.simplify.context.SContext.OBJECT_READER;

public class $ {

    public static final String EMPTY = "";
    private static final Logger logger = LoggerFactory.getLogger($.class);

    private static final Set<Class<?>> SIMPLE_TYPES = new HashSet<>();

    static {
        JSONFactory.getDefaultObjectReaderProvider().register(SContext.class, OBJECT_READER);
    }

    static {
        // 封装基础类型
        SIMPLE_TYPES.add(Byte.class);
        SIMPLE_TYPES.add(Short.class);
        SIMPLE_TYPES.add(Integer.class);
        SIMPLE_TYPES.add(Character.class);
        SIMPLE_TYPES.add(Long.class);
        SIMPLE_TYPES.add(Float.class);
        SIMPLE_TYPES.add(Double.class);
        SIMPLE_TYPES.add(Boolean.class);

        // 不可变类型
        SIMPLE_TYPES.add(BigDecimal.class);
        SIMPLE_TYPES.add(BigInteger.class);
        SIMPLE_TYPES.add(String.class);
        SIMPLE_TYPES.add(Class.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T cast(Object obj, Type targetType) {
        if (targetType.getClass() == Class.class) {
            return (T) cast(obj, (Class<?>) targetType);
        }
        if (null == obj) {
            return null;
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        if (obj instanceof Collection) {
            return (T) provider.getObjectReader(targetType).createInstance((Collection) obj);
        }

        if (obj instanceof Map) {
            return (T) provider.getObjectReader(targetType).createInstance((Map) obj, 0L);
        }
        Class<?> objClass = obj.getClass();
        Function typeConvert = provider.getTypeConvert(objClass, targetType);
        if (typeConvert != null) {
            return (T) typeConvert.apply(obj);
        }

        if (String.class == objClass) {
            return JSON.parseObject((String) obj, targetType);
        } else {
            return JSON.parseObject(JSON.toJSONString(obj), targetType);
        }
    }

    public static boolean isSimpleType(Class<?> type) {
        if (type.isPrimitive()) {
            return true;
        }
        return SIMPLE_TYPES.contains(type);
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj, Class<T> targetClass) {
        if (null == obj) {
            if (targetClass.isPrimitive()) {
                // 基础类型为空时需要返回默认值,避免出现异常
                return (T) TypeUtils.getDefaultValue(targetClass);
            }
            return null;
        }
        if (targetClass == String.class) {
            if (obj instanceof byte[]) {
                return (T) new String((byte[]) obj);
            }
            if (obj instanceof char[]) {
                return (T) new String((char[]) obj);
            }
        }
        if (targetClass.isInstance(obj)) {
            return (T) obj;
        }

        return TypeUtils.cast(obj, targetClass);
    }

    public static BigDecimal castToBigDecimal(Object obj) {
        if (null == obj || (obj instanceof String) && isBlank((String) obj)) {
            return BigDecimal.ZERO;
        }
        if (obj instanceof BigDecimal) {
            return (BigDecimal) obj;
        } else {
            return new BigDecimal(String.valueOf(obj));
        }
    }

    public static Object getFieldByPojo(Object pojo, String fieldKey) {
        if (null == pojo) {
            return null;
        }
        Class<?> pojoClass;
        if (pojo instanceof Class) {
            pojoClass = (Class<?>) pojo;
        } else {
            pojoClass = pojo.getClass();
        }

        //从javaBean中取值
        ObjectWriterProvider provider = JSONFactory.getDefaultObjectWriterProvider();
        // 优先采用方法获取，其次采用Field
        {
            ObjectWriter<?> reader = provider.getObjectWriter(pojoClass);
            FieldWriter<?> fieldReader = reader.getFieldWriter(fieldKey);
            if (null != fieldReader) {
                Method method = fieldReader.method;
                if (method != null) {
                    try {
                        return method.invoke(pojo);
                    } catch (Exception e) {
                        // skip
                    }
                }
            }
        }
        {
            ObjectWriter<?> reader = provider.getObjectWriter(pojoClass, pojoClass, true);
            FieldWriter<?> fieldReader = reader.getFieldWriter(fieldKey);
            if (null != fieldReader) {
                Field field = fieldReader.field;
                if (field != null) {
                    try {
                        return field.get(pojo);
                    } catch (IllegalAccessException e) {
                        // skip
                    }
                }
            }
        }
        logger.warn("无法从类型[{}]中获取属性[{}]", pojoClass.getName(), fieldKey);
        return null;
    }

    /**
     * <p>Checks if a String is whitespace, empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     *
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>Checks if a String is not empty (""), not null and not whitespace only.</p>
     *
     * <pre>
     * StringUtils.isNotBlank(null)      = false
     * StringUtils.isNotBlank("")        = false
     * StringUtils.isNotBlank(" ")       = false
     * StringUtils.isNotBlank("bob")     = true
     * StringUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is
     * not empty and not null and not whitespace
     * @since 2.0
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     *
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * <p>Checks whether the String a valid Java number.</p>
     *
     * <p>Valid numbers include hexadecimal marked with the <code>0x</code>
     * qualifier, scientific notation and numbers marked with a type
     * qualifier (e.g. 123L).</p>
     *
     * <p><code>Null</code> and empty String will return
     * <code>false</code>.</p>
     *
     * @param str the <code>String</code> to check
     * @return <code>true</code> if the string is a correctly formatted number
     */
    public static boolean isNumber(String str) {
        if (isEmpty(str)) {
            return false;
        }
        char[] chars = str.toCharArray();
        int sz = chars.length;
        boolean hasExp = false;
        boolean hasDecPoint = false;
        boolean allowSigns = false;
        boolean foundDigit = false;
        // deal with any possible sign up front
        int start = (chars[0] == '-') ? 1 : 0;
        if (sz > start + 1) {
            if (chars[start] == '0' && chars[start + 1] == 'x') {
                int i = start + 2;
                if (i == sz) {
                    return false; // str == "0x"
                }
                // checking hex (it can't be anything else)
                for (; i < chars.length; i++) {
                    if ((chars[i] < '0' || chars[i] > '9') && (chars[i] < 'a' || chars[i] > 'f') && (chars[i] < 'A' || chars[i] > 'F')) {
                        return false;
                    }
                }
                return true;
            }
        }
        sz--; // don't want to loop to the last char, check it afterwords
        // for type qualifiers
        int i = start;
        // loop to the next to last char or to the last char if we need another digit to
        // make a valid number (e.g. chars[0..5] = "1234E")
        while (i < sz || (i < sz + 1 && allowSigns && !foundDigit)) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                foundDigit = true;
                allowSigns = false;

            } else if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                hasDecPoint = true;
            } else if (chars[i] == 'e' || chars[i] == 'E') {
                // we've already taken care of hex.
                if (hasExp) {
                    // two E's
                    return false;
                }
                if (!foundDigit) {
                    return false;
                }
                hasExp = true;
                allowSigns = true;
            } else if (chars[i] == '+' || chars[i] == '-') {
                if (!allowSigns) {
                    return false;
                }
                allowSigns = false;
                foundDigit = false; // we need a digit after the E
            } else {
                return false;
            }
            i++;
        }
        if (i < chars.length) {
            if (chars[i] >= '0' && chars[i] <= '9') {
                // no type qualifier, OK
                return true;
            }
            if (chars[i] == 'e' || chars[i] == 'E') {
                // can't have an E at the last byte
                return false;
            }
            if (chars[i] == '.') {
                if (hasDecPoint || hasExp) {
                    // two decimal points or dec in exponent
                    return false;
                }
                // single trailing decimal point after non-exponent is ok
                return foundDigit;
            }
            if (!allowSigns && (chars[i] == 'd' || chars[i] == 'D' || chars[i] == 'f' || chars[i] == 'F')) {
                return foundDigit;
            }
            if (chars[i] == 'l' || chars[i] == 'L') {
                // not allowing L with an exponent
                return foundDigit && !hasExp;
            }
            // last character is illegal
            return false;
        }
        // allowSigns is true iff the val ends in 'E'
        // found digit it to make sure weird stuff like '.' and '1E-' doesn't pass
        return !allowSigns && foundDigit;
    }

    public static boolean putFieldByPojo(Object pojo, String fieldKey, Object value) {
        Class<?> pojoClass;
        if (pojo instanceof Class) {
            pojoClass = (Class<?>) pojo;
        } else {
            pojoClass = pojo.getClass();
        }

        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        // 优先采用方法获取，其次采用Field
        {
            ObjectReader<?> objectReader = provider.getObjectReader(pojoClass);
            FieldReader<?> fieldReader = objectReader.getFieldReader(fieldKey);
            if (null != fieldReader) {
                Method method = fieldReader.method;
                if (method != null) {
                    try {
                        method.invoke(pojo, cast(value, fieldReader.fieldType));
                        return true;
                    } catch (Exception e) {
                        // skip
                    }
                }
            }
        }
        {
            ObjectReader<?> objectReader = provider.getObjectReader(pojoClass, true);
            FieldReader<?> fieldReader = objectReader.getFieldReader(fieldKey);
            if (null != fieldReader) {
                Field field = fieldReader.field;
                if (field != null) {
                    try {
                        field.set(pojo, cast(value, fieldReader.fieldType));
                        return true;
                    } catch (IllegalAccessException e) {
                        // skip
                    }
                }
            }
        }

        return false;

    }

    /**
     * <p>Removes control characters (char &lt;= 32) from both
     * ends of this String returning an empty String ("") if the String
     * is empty ("") after the trim or if it is <code>null</code>.
     *
     * <p>The String is trimmed using {@link String#trim()}.
     * Trim removes start and end characters &lt;= 32.</p>
     *
     * <pre>
     * StringUtils.trimToEmpty(null)          = ""
     * StringUtils.trimToEmpty("")            = ""
     * StringUtils.trimToEmpty("     ")       = ""
     * StringUtils.trimToEmpty("abc")         = "abc"
     * StringUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     * @since 2.0
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }


}
