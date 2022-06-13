package org.fanjr.simplify.context;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.FieldDeserializer;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.FieldSerializer;
import com.alibaba.fastjson.serializer.JavaBeanSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.hundsun.gaps.flowexecutor.GapsContext;
import com.hundsun.gaps.flowexecutor.exceptions.ContextException;
import com.hundsun.gaps.flowexecutor.utils.CloneUtils;
import com.hundsun.gaps.flowexecutor.utils.GapsTypeUtils;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static ContextProcessorResult.newNeedPutPerent;
import static ContextProcessorResult.newSuccessResult;

public class MapPathProcessor implements IContextPathProcessor {

    private static final CloneUtils CLONE_UTIL = CloneUtils.INSTANCE;

    private static final SerializeConfig DEFAULT_SERIAL_CONFIG = SerializeConfig.globalInstance;

    private static final ParserConfig DEFAULT_PARSER_CONFIG = ParserConfig.global;

    private final String key;

    public MapPathProcessor(String key) {
        this.key = key;
    }

    @Override
    public Object doGetProcess(Object input) {
        if (input instanceof Map) {
            return ((Map<?, ?>) input).get(key);
        }
        Class<?> clazz;
        if (input instanceof Class) {
            clazz = (Class<?>) input;
        } else {
            clazz = input.getClass();
        }

        if (clazz == String.class) {
            try (DefaultJSONParser parser = new DefaultJSONParser((String) input, DEFAULT_PARSER_CONFIG,
                    JSON.DEFAULT_PARSER_FEATURE)) {
                GapsContext targetContext = parser.parseObject(GapsContext.class, null);
                parser.handleResovleTask(targetContext);
                return targetContext.get(key);
            } catch (Exception e) {
                return null;
            }
        } else if ((clazz.isArray() && !(input instanceof Class)) && ("length".equals(key) || "size".equals(key))) {
            return Array.getLength(input);
        } else if ((input instanceof Collection) && ("length".equals(key) || "size".equals(key))) {
            return ((Collection<?>) input).size();
        } else if (!CLONE_UTIL.isNeedClone(clazz) || input instanceof Collection) {
            return null;
        } else {
            ObjectSerializer serializer = DEFAULT_SERIAL_CONFIG.getObjectWriter(clazz);
            if (serializer instanceof JavaBeanSerializer) {
                FieldSerializer fieldSerializer = ((JavaBeanSerializer) serializer).getFieldSerializer(key);
                if (null != fieldSerializer) {
                    try {
                        return fieldSerializer.getPropertyValue(input);
                    } catch (Exception e) {
                        throw new ContextException(String.format("无法从类型[%s]中拿取属性[%s]", clazz.getName(), key), e);
                    }
                }
            }

            //反射Field取属性
            Field field = FieldUtils.getDeclaredField(clazz, key, true);
            if (null != field) {
                try {
                    return field.get(input);
                } catch (Exception e) {
                    throw new ContextException(String.format("无法从类型[%s]中拿取属性[%s]", clazz.getName(), key), e);
                }
            }
            return null;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public ContextProcessorResult doPutProcess(Object input, Object value, boolean ordered) {
        if (input instanceof Map) {
            return newSuccessResult(((Map) input).put(key, value));
        }
        Class<?> clazz = input.getClass();
        if (clazz == String.class) {
            Map<String, Object> newMap;
            try (DefaultJSONParser parser = new DefaultJSONParser((String) input, DEFAULT_PARSER_CONFIG,
                    JSON.DEFAULT_PARSER_FEATURE)) {
                newMap = parser.parseObject(GapsContext.class, null);
                parser.handleResovleTask(newMap);
            } catch (Exception e) {
                newMap = new HashMap<>();
            }
            newMap.put(key, value);
            return newNeedPutPerent(newMap);
        } else if (!CLONE_UTIL.isNeedClone(clazz) || clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
            Map<String, Object> newMap = new HashMap<>();
            newMap.put(key, value);
            return newNeedPutPerent(newMap);
        } else {
            {
                ObjectDeserializer deserializer = DEFAULT_PARSER_CONFIG.getDeserializer(clazz);
                if (deserializer instanceof JavaBeanDeserializer) {
                    FieldDeserializer fieldDeserializer = ((JavaBeanDeserializer) deserializer).getFieldDeserializer(key);
                    if (null != fieldDeserializer) {
                        try {
                            Object old;
                            ObjectSerializer serializer = DEFAULT_SERIAL_CONFIG.getObjectWriter(clazz);
                            if (serializer instanceof JavaBeanSerializer) {
                                FieldSerializer fieldSerializer = ((JavaBeanSerializer) serializer).getFieldSerializer(key);
                                if (null != fieldSerializer) {
                                    old = fieldSerializer.getPropertyValue(input);
                                } else {
                                    old = null;
                                }
                            } else {
                                old = null;
                            }
                            fieldDeserializer.fieldInfo.set(input, GapsTypeUtils.cast(value, fieldDeserializer.fieldInfo.fieldType, DEFAULT_PARSER_CONFIG));
                            return newSuccessResult(old);
                        } catch (Exception e) {
                            throw new ContextException(
                                    String.format("暂不支持设置类型[%s]中属性[%s]", clazz.getName(), key), e);
                        }
                    }
                }
            }

            {
                //没有属性也没有set方法，新建MAP
                ObjectSerializer serializer = DEFAULT_SERIAL_CONFIG.getObjectWriter(clazz);
                if (serializer instanceof JavaBeanSerializer) {
                    Map<String, Object> fieldValues;
                    try {
                        fieldValues = ((JavaBeanSerializer) serializer).getFieldValuesMap(input);
                    } catch (Exception e) {
                        throw new ContextException("转换成上下文发生错误", e);
                    }
                    GapsContext newMap = new GapsContext(fieldValues);
                    newMap.put(key, value);
                    return newNeedPutPerent(newMap);
                }
                throw new ContextException(String.format("转换成上下文发生错误,类型[%s]无法直接进行转换", clazz.getName()));
            }
        }
    }

    @Override
    public Object newContainer(Object value, boolean ordered) {
        Map<String, Object> newMap;
        if (ordered) {
            newMap = new LinkedHashMap<>();
        } else {
            newMap = new HashMap<>();
        }
        newMap.put(key, value);
        return newMap;
    }
}
