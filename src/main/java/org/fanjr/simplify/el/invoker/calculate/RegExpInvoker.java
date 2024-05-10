package org.fanjr.simplify.el.invoker.calculate;

import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.cache.ConcurrentCache;
import org.fanjr.simplify.utils.ElUtils;

import java.util.LinkedList;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 正则计算
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午4:38
 */
public class RegExpInvoker extends BinocularInvoker {

    private final ConcurrentCache<String, Pattern> cache = new ConcurrentCache<>(1000);

    private RegExpInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("~=", stack, new RegExpInvoker());
    }

    @Override
    protected Object doOperation(Object val1, Object val2) {
        String matchStr = ElUtils.cast(val1, String.class);
        String regExpStr = ElUtils.cast(val2, String.class);
        if (null == matchStr || null == regExpStr) {
            return false;
        }
        Pattern pattern = cache.computeIfAbsent(regExpStr, Pattern::compile);
        return Objects.requireNonNull(pattern).matcher(matchStr).find();
    }

}
