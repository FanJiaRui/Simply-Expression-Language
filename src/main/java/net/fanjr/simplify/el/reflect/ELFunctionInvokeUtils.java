package net.fanjr.simplify.el.reflect;

import com.alibaba.fastjson2.util.BeanUtils;
import net.fanjr.simplify.el.ELMethod;
import net.fanjr.simplify.el.cache.ConcurrentCache;
import net.fanjr.simplify.utils.Pair;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于表达式反射执行方法时使用
 */
public class ELFunctionInvokeUtils {

    /**
     * 用于存储内置函数的映射关系，内置函数不适用缓存，原则上内置函数不被垃圾回收，也不应该存在类加载器被卸载的情况
     */
    private static final Map<String, Map<String, Set<ELInnerFunction>>> FUNCTION_MAPPING = new ConcurrentHashMap<>();

    /**
     * 用于存储类与其方法的映射关系
     */
    private static final ConcurrentCache<Class<?>, Map<String, Set<ELObjectFunction>>> CLASS_METHOD_MAPPING = new ConcurrentCache<>(10000);

    /**
     * 方法映射
     */
    private static final ConcurrentCache<Method, ELObjectFunction> METHOD_MAPPING = new ConcurrentCache<>(10000);


    /**
     * 用于添加自定义函数，将传入类的公共static方法全部分析提取并挂载到工具名下，用于表达式后续调用
     *
     * @param utilsName 自定义函数挂载的工具名(或类名)
     * @param clazz     要添加的类对象
     */
    public static void addFunctionClass(String utilsName, Class<?> clazz) {
        synchronized ("EL_F_K_L" + utilsName) {
            Method[] allMethods = clazz.getMethods();
            for (Method m : allMethods) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    // 跳过非类方法注册
                    continue;
                }
                List<ELInnerFunction> targetFunctions = new ArrayList<>();
                ELMethod annotation = BeanUtils.findAnnotation(m, ELMethod.class);
                if (null != annotation) {
                    if (annotation.skip()) {
                        // 跳过该方法注册
                        continue;
                    }
                    String[] methodNames;
                    if (annotation.functionName().length == 0) {
                        methodNames = new String[]{m.getName()};
                    } else {
                        methodNames = annotation.functionName();
                    }
                    for (String methodName : methodNames) {
                        targetFunctions.add(new ELInnerFunction(utilsName, methodName, clazz, m, annotation.order(), Arrays.asList(annotation.userDefinedExceptions())));
                    }
                } else {
                    targetFunctions.add(new ELInnerFunction(utilsName, m.getName(), clazz, m, 0));
                }

                for (ELInnerFunction elInnerFunction : targetFunctions) {
                    Map<String, Set<ELInnerFunction>> utilsMapping = FUNCTION_MAPPING.computeIfAbsent(utilsName, (k) -> new ConcurrentHashMap<>());
                    Set<ELInnerFunction> pairs = utilsMapping.computeIfAbsent(elInnerFunction.getMethodName(), (k) -> new TreeSet<>());
                    pairs.add(elInnerFunction);
                }
            }
        }
    }

    /**
     * 判断是否存在该工具
     *
     * @param utilName 工具名词
     * @return 返回true时表示存在，否则不存在
     */
    public static boolean hasUtils(String utilName) {
        return FUNCTION_MAPPING.containsKey(utilName);
    }

    public static ELObjectFunction getObjectFunction(Object instance, String methodName, Object... args) {
        if (null == instance) {
            return null;
        }

        Class<?> clazz = instance.getClass();
        Map<String, Set<ELObjectFunction>> targetMapping = CLASS_METHOD_MAPPING.get(clazz);
        if (null == targetMapping) {
            synchronized ("EL_M_K_L" + clazz.getName()) {
                targetMapping = CLASS_METHOD_MAPPING.get(clazz);
                if (null == targetMapping) {
                    targetMapping = new ConcurrentHashMap<>();
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        int mod = method.getModifiers();
                        if (Modifier.isAbstract(mod)) {
                            // 抽象方法，跳过
                            continue;
                        }
                        if (Modifier.isStatic(mod)) {
                            // 出于安全考虑，类方法也跳过
                            continue;
                        }

                        ELObjectFunction function = METHOD_MAPPING.computeIfAbsent(method, ELObjectFunction::new);
                        Set<ELObjectFunction> functions = targetMapping.computeIfAbsent(method.getName(), (k) -> new TreeSet<>());
                        functions.add(function);
                    }

                    CLASS_METHOD_MAPPING.put(clazz, targetMapping);
                }
            }
        }


        Set<ELObjectFunction> elObjectFunctions = targetMapping.get(methodName);

        if (null == elObjectFunctions) {
            return null;
        }

        if (elObjectFunctions.size() == 0) {
            return null;
        }

        for (ELObjectFunction method : elObjectFunctions) {
            // 找到第一个参数匹配上的方法
            if (method.match(args)) {
                return method;
            }
        }

        for (ELObjectFunction method : elObjectFunctions) {
            // 找到第一个参数个数匹配上的方法
            if (method.getParameterCount() == args.length) {
                return method;
            }
        }

        return null;
    }

    public static ELInnerFunction findFunction(String utilName, String functionName, Object... args) {
        Map<String, Set<ELInnerFunction>> stringSetMap = FUNCTION_MAPPING.get(utilName);
        if (null == stringSetMap) {
            return null;
        }

        Set<ELInnerFunction> elInnerFunctions = stringSetMap.get(functionName);
        if (null == elInnerFunctions) {
            return null;
        }

        if (elInnerFunctions.size() == 0) {
            return null;
        }

        for (ELInnerFunction method : elInnerFunctions) {
            if (method.match(args)) {
                return method;
            }
        }

        for (ELInnerFunction method : elInnerFunctions) {
            // 找到第一个参数个数匹配上的方法
            if (method.getParameterCount() == args.length) {
                return method;
            }
        }

        return null;
    }

    public static Pair<Class<?>[], Type[]> getMethodParameters(Method m) {
        Type[] genericParameterTypes = m.getGenericParameterTypes();
        Type[] targetArgsType = new Type[genericParameterTypes.length];
        for (int i = 0; i < genericParameterTypes.length; i++) {
            Type g = genericParameterTypes[i];
            Type canonicalize;
            if (g instanceof TypeVariable) {
                canonicalize = BeanUtils.getRawType(g);
            } else {
                canonicalize = BeanUtils.canonicalize(g);
            }
            targetArgsType[i] = canonicalize;
        }
        return new Pair<>(m.getParameterTypes(), targetArgsType);
    }


}
