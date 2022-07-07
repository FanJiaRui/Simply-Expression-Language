package org.fanjr.simplify.el;

import org.fanjr.simplify.utils.ElUtils;

import java.lang.reflect.Type;

/**
 * @author fanjr@vip.qq.com
 * @file EL.java
 * @since 2021/6/28 下午4:08
 */
public interface EL {

    Object invoke(Object ctx);

    default Object invoke(Object ctx, Type type) {
        return ElUtils.cast(invoke(ctx), type);
    }

    default <T> T invoke(Object ctx, Class<T> type) {
        return ElUtils.cast(invoke(ctx), type);
    }
}
