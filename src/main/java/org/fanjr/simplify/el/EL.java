package org.fanjr.simplify.el;

import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Type;

/**
 * 可执行表达式实例
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午4:08
 */
public interface EL {

    /**
     * 执行表达式
     *
     * @param ctx 上下文
     * @return 执行结果
     */
    Object invoke(Object ctx);

    /**
     * 执行表达式，返回预期类型结果
     *
     * @param ctx  上下文
     * @param type 预期返回类型
     * @return 执行结果
     */
    default Object invoke(Object ctx, Type type) {
        return ElUtils.cast(invoke(ctx), type);
    }

    /**
     * 执行表达式，返回预期类型结果
     *
     * @param ctx  上下文
     * @param type 预期返回类型
     * @return 执行结果
     */
    default <T> T invoke(Object ctx, Class<T> type) {
        return ElUtils.cast(invoke(ctx), type);
    }


    default void accept(ELVisitor visitor) {
        // 默认只访问自身，不访问子节点
        visitor.visit(this);
    }
}
