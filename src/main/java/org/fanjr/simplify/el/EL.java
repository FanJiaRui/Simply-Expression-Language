package org.fanjr.simplify.el;

import com.alibaba.fastjson.parser.ParserConfig;
import com.hundsun.gaps.flowexecutor.utils.GapsTypeUtils;

import java.lang.reflect.Type;

/**
 * @author fanjr15662@hundsun.com
 * @file EL.java
 * @since 2021/6/28 下午4:08
 */
public interface EL {

    Object invoke(Object ctx);

    default <T> T invoke(Object ctx, Type type) {
        return GapsTypeUtils.cast(invoke(ctx), type, ParserConfig.getGlobalInstance());
    }
}
