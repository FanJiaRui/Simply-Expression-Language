package net.fanjr.simplify.el.invoker.calculate;

import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.utils.$;
import net.fanjr.simplify.utils.SimplifyCache;

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

    private final SimplifyCache<String, Pattern> cache = new SimplifyCache<>(1000);

    private RegExpInvoker() {
        //skip
    }

    public static ELInvoker buildInstance(LinkedList<ELInvoker> stack) {
        return BinocularInvoker.buildInstance("~=", stack, new RegExpInvoker());
    }

    @Override
    protected Object doOperation(Object val1, Object val2) {
        String matchStr = $.cast(val1, String.class);
        String regExpStr = $.cast(val2, String.class);
        if (null == matchStr || null == regExpStr) {
            return false;
        }
        Pattern pattern = cache.computeIfAbsent(regExpStr, Pattern::compile);
        return Objects.requireNonNull(pattern).matcher(matchStr).find();
    }

}
