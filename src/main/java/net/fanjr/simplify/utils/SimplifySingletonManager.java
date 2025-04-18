package net.fanjr.simplify.utils;

/**
 * 简易单例管理器
 *
 * @author fanjr@vip.qq.com
 */
public class SimplifySingletonManager {
    private static final SimplifyCache<String, Object> SIMPLIFY_CACHE = new SimplifyCache<>(10000);

    public static Object getSingleton(String id) {
        return SIMPLIFY_CACHE.get(id);
    }

    /**
     * @noinspection unchecked
     */
    public static <T> T getSingleton(Class<T> type) {
        return (T) SIMPLIFY_CACHE.computeIfAbsent(type.getName(), k -> {
            try {
                return type.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new SimplifyException("初始化单例[" + type.getName() + "]发生异常!", e);
            }
        });
    }

    public static void registerSingleton(Class<?> type, Object instance) {
        if (type.isInstance(instance)) {
            SIMPLIFY_CACHE.put(type.getName(), instance);
        } else {
            throw new SimplifyException("注册单例[" + type.getName() + "]失败, 因为实例类型不匹配!");
        }
    }

    public static void registerSingleton(Object instance) {
        Class<?> type = instance.getClass();
        SIMPLIFY_CACHE.put(type.getName(), instance);
    }

    public static void registerSingleton(String id, Object instance) {
        Class<?> type = instance.getClass();
        SIMPLIFY_CACHE.put(id, instance);
    }

}
