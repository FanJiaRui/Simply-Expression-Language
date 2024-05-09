package org.fanjr.simplify.el;

import java.lang.annotation.*;

/**
 * 用于标识表达式内置方法，标记了注解的方法在产生方法名冲突时优先级更高
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface ELMethod {

    /**
     * 当方法名存在冲突时，以排序决定是否生效，数值大的生效优先级低，标记注解默认为-1，未标记注解默认为0，表达式内置方法优先级默认为最低(MAX_INT)
     */
    int order() default -1;

    /**
     * 注册的方法名称，若为空值，则以实际方法名为准
     */
    String functionName() default "";

}
