package org.fanjr.simplify.utils;

import com.alibaba.fastjson2.util.BeanUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用于表达式反射执行方法时使用
 */
public class ELMethodInvokeUtils {

//    private static final Map<Class<?>, Map<String, List<Pair<Method, List<Type>>>>> MAPPING = new ConcurrentHashMap<>();

    /**
     * 用于存储内置函数的映射关系
     */
    private static final Map<String, Map<String, Set<ELFunction>>> FUNCTION_MAPPING = new ConcurrentHashMap<>();


    public static void addFunctionClass(String utilsName, Class<?> clazz) {
        synchronized ("EL_F_K_L" + utilsName) {
            Method[] allMethods = clazz.getMethods();
            for (Method m : allMethods) {
                if (!Modifier.isStatic(m.getModifiers())) {
                    // 跳过非类方法注册
                    continue;
                }
                ELFunction elFunction;
                org.fanjr.simplify.el.ELMethod annotation = BeanUtils.findAnnotation(m, org.fanjr.simplify.el.ELMethod.class);
                if (null != annotation) {
                    if (annotation.skip()) {
                        // 跳过该方法注册
                        continue;
                    }
                    String methodName;
                    if (ElUtils.isBlank(annotation.functionName())) {
                        methodName = m.getName();
                    } else {
                        methodName = annotation.functionName();
                    }
                    elFunction = new ELFunction(utilsName, methodName, clazz, m, annotation.order(), Arrays.asList(annotation.userDefinedExceptions()));
                } else {
                    elFunction = new ELFunction(utilsName, m.getName(), clazz, m, 0);
                }

                Map<String, Set<ELFunction>> utilsMapping = FUNCTION_MAPPING.computeIfAbsent(utilsName, (k) -> new ConcurrentHashMap<>());
                Set<ELFunction> pairs = utilsMapping.computeIfAbsent(elFunction.getMethodName(), (k) -> new TreeSet<>());
                pairs.add(elFunction);

            }
        }
    }

    public static boolean hasUtils(String utilName) {
        return FUNCTION_MAPPING.containsKey(utilName);
    }

    public static ELFunction findFunction(String utilName, String functionName, Object... args) {
        Map<String, Set<ELFunction>> stringSetMap = FUNCTION_MAPPING.get(utilName);
        if (null == stringSetMap) {
            return null;
        }

        Set<ELFunction> elFunctions = stringSetMap.get(functionName);
        if (null == elFunctions) {
            return null;
        }


        for (ELFunction method : elFunctions) {
            if (method.match(args)) {
                return method;
            }
        }

        if (elFunctions.size() == 0) {
            return null;
        }

        return elFunctions.iterator().next();
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
