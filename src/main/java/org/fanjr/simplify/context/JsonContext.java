//package org.fanjr.simplify.context;
//
//import com.alibaba.fastjson2.JSONObject;
//import com.alibaba.fastjson2.JSONWriter;
//import com.alibaba.fastjson2.util.TypeUtils;
//import com.alibaba.fastjson2.writer.ObjectWriter;
//
//import java.io.Serializable;
//import java.lang.reflect.Array;
//import java.lang.reflect.Type;
//import java.math.BigDecimal;
//import java.math.BigInteger;
//import java.util.*;
//
///*
// * @author fanjr@vip.qq.com
// * @file MapContext.java
// * @since 2022/5/19 下午4:59
// */
//public class JsonContext extends InnerMapperAdapter<String, Object> implements IContext, Cloneable, Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    private static final char ARRAY_END_CHAR = ']';
//
//    private static final char ARRAY_START_CHAR = '[';
//
//    private static final String SEPARATOR_STRING = "\\.";
//
//    private static final int DEFAULT_INITIAL_CAPACITY = 16;
//
//    private final boolean ordered;
//
//    public JsonContext() {
//        this(DEFAULT_INITIAL_CAPACITY, false);
//    }
//
//    public JsonContext(JSONObject json) {
//        super(json);
//        this.ordered = false;
//    }
//
//    public JsonContext(boolean ordered) {
//        this(DEFAULT_INITIAL_CAPACITY, ordered);
//    }
//
//    public JsonContext(int initialCapacity) {
//        this(initialCapacity, false);
//    }
//
//    public JsonContext(int initialCapacity, boolean ordered) {
//        super(ordered ? new LinkedHashMap<>(initialCapacity) : new HashMap<>(initialCapacity));
//        this.ordered = ordered;
//    }
//
//    public static Object getNode4Object(String nodeName, Object source) {
//        return ContextDispatcher.getInstance(nodeName).doGet(source);
//    }
//
//    private static Object getObjectByIndex(int index, Object javaObject) {
//        if (null == javaObject) {
//            return null;
//        }
//        if (javaObject instanceof Collection) {
//            if (index >= ((Collection<?>) javaObject).size()) {
//                return null;
//            }
//            return ((Collection<?>) javaObject).toArray()[index];
//        }
//        if (javaObject.getClass().isArray()) {
//            if (index >= ((Object[]) javaObject).length) {
//                return null;
//            }
//            return ((Object[]) javaObject)[index];
//        }
//        return null;
//    }
//
//    /**
//     * 从对象中获取子属性</br>
//     * 这个操作可能被非常频繁地调用
//     *
//     * @param key        不能为空
//     * @param javaObject 不能为空
//     * @return
//     */
//    private static Object getObjectByKey(String key, Object javaObject) {
//        if (ARRAY_END_CHAR == key.charAt(key.length() - 1)) {
//            int lastStartCharIndex = key.lastIndexOf(ARRAY_START_CHAR);
//            javaObject = getObjectByKey(key.substring(0, lastStartCharIndex), javaObject);
//            return getObjectByIndex(Integer.parseInt(key.substring(lastStartCharIndex + 1, key.length() - 1)),
//                    javaObject);
//        }
//
//        if (javaObject instanceof Map) {
//            return ((Map<?, ?>) javaObject).get(key);
//        }
//
//        Class<?> clazz = javaObject.getClass();
//        if (!CLONE_UTIL.isNeedClone(clazz) || clazz.isArray() || Collection.class.isAssignableFrom(clazz)) {
//            return null;
//        }
//
//        ObjectSerializer serializer = DEFAULT_SERIAL_CONFIG.getObjectWriter(clazz);
//        if (serializer instanceof JavaBeanSerializer) {
//            JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;
//            try {
//                return javaBeanSerializer.getFieldValue(javaObject, key);
//            } catch (Exception e) {
//                throw new ContextException(String.format("无法从类型[%s]中拿取属性[%s]", clazz.getName(), key), e);
//            }
//        }
//
//        throw new ContextException(String.format("暂不支持从类型[%s]中拿取属性", clazz.getName()));
//    }
//
//    @Override
//    public JsonContext clone() {
//        return new JsonContext(new JSONObject(innerMap));
//    }
//
//    /**
//     * 将变量value放入上下文中对应节点
//     *
//     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
//     * @param value    需要存储的变量
//     * @return 执行put方法时对已经存储的对象进行了覆盖, 则返回这个对象, 否则返回null</ br>
//     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行put操作</br>
//     * key为"a.b"时将返回1</br>
//     * key为"b.d"时将返回null</br>
//     * key为"b.c"时将返回2
//     * @throws NullPointerException key为空时将会抛出这个异常
//     * @throws ContextException     上下文操作发生错误时将会抛出这个异常
//     */
//    @Override
//    public void putByNode(String nodeName, Object value) {
//        ContextDispatcher.getInstance(nodeName).doPut(innerMap, value, ordered);
//    }
//
//    public Object removeByNode(Object key) {
//        String[] keys = key.toString().split(SEPARATOR_STRING);
//        Object obj = innerMap;
//        for (int i = 0; i < keys.length - 1; i++) {
//            obj = getObjectByKey(keys[i], obj);
//            if (null == obj) {
//                return null;
//            }
//        }
//
//        return removeObjectByKey(keys[keys.length - 1], obj);
//    }
//
//    public Map<String, Object> findAllNode(String expression) {
//        return innerMap;
//    }
//
//    @Override
//    public String toString() {
//        try (JSONWriter writer = JSONWriter.of()) {
//            ObjectWriter<?> objectWriter = writer.getObjectWriter(JsonContext.class, JsonContext.class);
//            objectWriter.write(writer, this, null, null, 0);
//            return writer.toString();
//        } catch (NullPointerException | NumberFormatException ex) {
//            throw new ContextException("toJSONString error", ex);
//        }
//    }
//
//    /**
//     * 从对象中移除或者置空子属性</br>
//     * 这个操作可能被非常频繁地调用
//     *
//     * @param key        不能为空
//     * @param javaObject 不能为空
//     * @return
//     */
//    private Object removeObjectByKey(String key, Object javaObject) {
//        if (ARRAY_END_CHAR == key.charAt(key.length() - 1)) {
//            int lastStartCharIndex = key.lastIndexOf(ARRAY_START_CHAR);
//            javaObject = getObjectByKey(key.substring(0, lastStartCharIndex), javaObject);
//            if (null == javaObject) {
//                return;
//            }
//            int index = Integer.parseInt(key.substring(lastStartCharIndex + 1, key.length() - 1));
//            if (javaObject instanceof List) {
//                if (index >= ((List<?>) javaObject).size()) {
//                    return;
//                }
//                ((List<?>) javaObject).set(index, null);
//                return;
//            }
//            if (javaObject instanceof Collection) {
//                if (index >= ((Collection<?>) javaObject).size()) {
//                    return;
//                }
//                final Iterator<?> each = ((Collection<?>) javaObject).iterator();
//                int i = 0;
//                while (each.hasNext()) {
//                    each.next();
//                    if (index == i++) {
//                        each.remove();
//                        return;
//                    }
//                }
//            }
//
//            Class<?> type = javaObject.getClass();
//            if (type.isArray()) {
//                Array.getLength(javaObject);
//                if (index >= Array.getLength(javaObject)) {
//                    return;
//                }
//                // 置空或设置为基础类型初始值
//                Array.set(javaObject, index, TypeUtils.getDefaultValue(type.getComponentType()));
//                return;
//            }
//            return;
//        }
//
//        if (javaObject instanceof Map) {
//            ((Map<?, ?>) javaObject).remove(key);
//            return;
//        }
//        Class<?> clazz = javaObject.getClass();
//        if (!CLONE_UTIL.isNeedClone(clazz)) {
//            return;
//        }
//
//        {
//            ObjectDeserializer deserializer = DEFAULT_PARSER_CONFIG.getDeserializer(clazz);
//            if (deserializer instanceof JavaBeanDeserializer) {
//                FieldDeserializer fieldDeserializer = ((JavaBeanDeserializer) deserializer).getFieldDeserializer(key);
//                if (null != fieldDeserializer) {
//                    try {
//                        Object old;
//                        ObjectSerializer serializer = DEFAULT_SERIAL_CONFIG.getObjectWriter(clazz);
//                        if (serializer instanceof JavaBeanSerializer) {
//                            FieldSerializer fieldSerializer = ((JavaBeanSerializer) serializer).getFieldSerializer(key);
//                            if (null != fieldSerializer) {
//                                old = fieldSerializer.getPropertyValue(javaObject);
//                            } else {
//                                old = null;
//                            }
//                        } else {
//                            old = null;
//                        }
//                        fieldDeserializer.setValue(javaObject, GapsTypeUtils.cast(null, fieldDeserializer.fieldInfo.fieldType, DEFAULT_PARSER_CONFIG));
//                        return old;
//                    } catch (Exception e) {
//                        throw new ContextException(String.format("暂不支持将类型[%s]中属性[%s]置空", clazz.getName(), key), e);
//                    }
//                }
//            }
//            return null;
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> T toJavaBean(Class<T> clazz) {
//        if (clazz == String.class) {
//            return (T) this.toString();
//        }
//
//        if (clazz == JsonContext.class || clazz == Object.class) {
//            return (T) this;
//        }
//        return cast(innerMap, clazz, DEFAULT_PARSER_CONFIG);
//    }
//
//    @Override
//    public Object get(Object key) {
//        return this.innerMap.get(key);
//    }
//
//    /**
//     * 从上下文中获取节点
//     *
//     * @param nodeName 上下文节点名,支持多层,大小写敏感,以'.'作为层级分隔符,例如Pojo.a.B
//     * @return 当且仅当对应节点存在对象时, 则返回这个对象, 否则返回null</ br>
//     * 例如:对上下文{"a":1,"b":{"c":"2"}}执行get操作</br>
//     * key为"a"时将返回1</br>
//     * key为"c.b"时将返回null</br>
//     * key为"b"时将返回{"c":"2"}
//     * @throws ContextException 上下文操作发生错误时将会抛出这个异常
//     */
//    @Override
//    public Object getByNode(String nodeName) {
//        return ContextDispatcher.getInstance(nodeName).doGet(innerMap);
//    }
//
//    public IContext getContext(String nodeName) {
//        Object obj = getByNode(nodeName);
//        if (null == obj) {
//            return null;
//        }
//        return toContext(obj);
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> T getObject(String key, Class<T> clazz) {
//        Object obj = getByNode(key);
//        if (obj instanceof JsonContext) {
//            return ((JsonContext) obj).toJavaBean(clazz);
//        }
//        if (null != obj && clazz == JsonContext.class) {
//            return (T) toContext(obj);
//        }
//        return cast(obj, clazz, DEFAULT_PARSER_CONFIG);
//    }
//
//    @SuppressWarnings("unchecked")
//    public <T> T getObject(String key, Type type) {
//        Object obj = getByNode(key);
//        if (obj instanceof JsonContext) {
//            if (type instanceof Class) {
//                return (T) ((JsonContext) obj).toJavaBean((Class<?>) type);
//            }
//        }
//        return cast(obj, type, DEFAULT_PARSER_CONFIG);
//    }
//
//    public Boolean getBoolean(String key) {
//        Object value = getByNode(key);
//        if (value == null) {
//            return Boolean.FALSE;
//        }
//        return castToBoolean(value);
//    }
//
//    public byte[] getBytes(String key) {
//        Object value = getByNode(key);
//        if (value == null) {
//            return null;
//        }
//        return castToBytes(value);
//    }
//
//    public boolean getBooleanValue(String key) {
//        Object value = getByNode(key);
//        Boolean booleanVal = castToBoolean(value);
//        if (booleanVal == null) {
//            return false;
//        }
//        return booleanVal.booleanValue();
//    }
//
//    public Byte getByte(String key) {
//        Object value = getByNode(key);
//        return castToByte(value);
//    }
//
//    public byte getByteValue(String key) {
//        Object value = getByNode(key);
//        Byte byteVal = castToByte(value);
//        if (byteVal == null) {
//            return 0;
//        }
//        return byteVal.byteValue();
//    }
//
//    public Short getShort(String key) {
//        Object value = getByNode(key);
//        return castToShort(value);
//    }
//
//    public short getShortValue(String key) {
//        Object value = getByNode(key);
//        Short shortVal = castToShort(value);
//        if (shortVal == null) {
//            return 0;
//        }
//        return shortVal.shortValue();
//    }
//
//    public Integer getInteger(String key) {
//        Object value = getByNode(key);
//        return castToInt(value);
//    }
//
//    public int getIntValue(String key) {
//        Object value = getByNode(key);
//        Integer intVal = castToInt(value);
//        if (intVal == null) {
//            return 0;
//        }
//        return intVal.intValue();
//    }
//
//    public Long getLong(String key) {
//        Object value = getByNode(key);
//        return castToLong(value);
//    }
//
//    public long getLongValue(String key) {
//        Object value = getByNode(key);
//        Long longVal = castToLong(value);
//        if (longVal == null) {
//            return 0L;
//        }
//        return longVal.longValue();
//    }
//
//    public Float getFloat(String key) {
//        Object value = getByNode(key);
//        return castToFloat(value);
//    }
//
//    public float getFloatValue(String key) {
//        Object value = getByNode(key);
//        Float floatValue = castToFloat(value);
//        if (floatValue == null) {
//            return 0F;
//        }
//        return floatValue.floatValue();
//    }
//
//    public Double getDouble(String key) {
//        Object value = getByNode(key);
//        return castToDouble(value);
//    }
//
//    public double getDoubleValue(String key) {
//        Object value = getByNode(key);
//        Double doubleValue = castToDouble(value);
//        if (doubleValue == null) {
//            return 0D;
//        }
//        return doubleValue.doubleValue();
//    }
//
//    public BigDecimal getBigDecimal(String key) {
//        Object value = getByNode(key);
//        return castToBigDecimal(value);
//    }
//
//    public BigInteger getBigInteger(String key) {
//        Object value = getByNode(key);
//        return castToBigInteger(value);
//    }
//
//    public String getString(String key) {
//        Object value = getByNode(key);
//        if (value == null) {
//            return null;
//        }
//        return value.toString();
//    }
//
//    public Date getDate(String key) {
//        Object value = getByNode(key);
//        return castToDate(value);
//    }
//
//    public java.sql.Date getSqlDate(String key) {
//        Object value = getByNode(key);
//        return castToSqlDate(value);
//    }
//
//    public java.sql.Timestamp getTimestamp(String key) {
//        Object value = getByNode(key);
//        return castToTimestamp(value);
//    }
//
//    @SuppressWarnings("unchecked")
//    public List<Object> getList(String nodeName) {
//        Object obj = getByNode(nodeName);
//        if (null == obj) {
//            return new ArrayList<>();
//        }
//        if (obj instanceof List) {
//            return (List<Object>) obj;
//        } else if (obj instanceof Object[]) {
//            List<Object> target = new ArrayList<>();
//            Object[] arr = (Object[]) obj;
//            return Arrays.asList(arr);
//        } else {
//            List<Object> list = new ArrayList<>();
//            list.add(obj);
//            return list;
//        }
//    }
//
//    @Override
//    public Map<String, Object> toMap() {
//        return this;
//    }
//}
