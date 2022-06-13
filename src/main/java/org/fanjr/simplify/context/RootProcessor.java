package org.fanjr.simplify.context;

import java.util.Map;

import static org.fanjr.simplify.context.ContextProcessorResult.newSuccessResult;

public class RootProcessor implements IContextPathProcessor {

    @Override
    public Object doGetProcess(Object input) {
        return input;
    }

    @Override
    public Object newContainer(Object value, boolean ordered) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ContextProcessorResult doPutProcess(Object input, Object value, boolean ordered) {
        if (input instanceof Map && value instanceof Map) {
            ((Map<Object, Object>) input).putAll((Map<Object, Object>) value);
            return newSuccessResult(null);
        }
        if (null == value) {
            return newSuccessResult(null);
        }
        throw new ContextException("不支持将" + value + "直接放入上下文中!");
    }

}
