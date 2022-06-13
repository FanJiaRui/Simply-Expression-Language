package org.fanjr.simplify.context;

import com.alibaba.fastjson2.util.TypeUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.fanjr.simplify.context.ContextProcessorResult.newNeedPutPerent;
import static org.fanjr.simplify.context.ContextProcessorResult.newSuccessResult;

public class ArrayPathProcessor implements IContextPathProcessor {

    /**
     * 数组维度
     */
    private final int index;

    public ArrayPathProcessor(int index) {
        this.index = index;
    }

    @Override
    public Object doGetProcess(Object input) {
        if (input instanceof List) {
            List<?> list = (List<?>) input;
            if (index < list.size()) {
                return list.get(index);
            }
        } else if (input instanceof Iterable) {
            int tmpIndex = 0;
            Iterable<?> currIter = (Iterable<?>) input;
            for (Object currTmp : currIter) {
                if (tmpIndex == index) {
                    return currTmp;
                }
                tmpIndex++;
            }
        } else if (input.getClass().isArray()) {
            int length = Array.getLength(input);
            if (index < length) {
                return Array.get(input, index);
            }
        }
        // 无法获取内容直接返回null
        return null;

    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ContextProcessorResult doPutProcess(Object input, Object value, boolean ordered) {
        if (input instanceof List) {
            List<Object> target = (List) input;
            int length = target.size();
            for (int i = index; i >= length; i--) {
                target.add(null);
            }
            return newSuccessResult(target.set(index, value));
        }
        Class<?> type = input.getClass();
        if (type.isArray()) {
            int length = Array.getLength(input);
            if (value == null) {
                value = TypeUtils.getDefaultValue(type.getComponentType());
            } else if (!type.getComponentType().isAssignableFrom(value.getClass())) {
                List<Object> newList = new ArrayList<>(Math.max(length, index + 1));
                int i = 0;
                for (; i < length; i++) {
                    newList.add(Array.get(input, i));
                }
                for (; i <= index; i++) {
                    newList.add(null);
                }
                newList.set(index, value);
                return newNeedPutPerent(newList);
            }
            if (index >= length) {
                Object newArr = Array.newInstance(type.getComponentType(), index + 1);
                for (int i = 0; i < length; i++) {
                    Array.set(newArr, i, Array.get(input, i));
                }
                return newNeedPutPerent(newArr);
            } else {
                Object old = Array.get(input, index);
                Array.set(input, index, value);
                return newSuccessResult(old);
            }
        }
        return newNeedPutPerent(newContainer(value, ordered));
    }

    @Override
    public Object newContainer(Object value, boolean ordered) {
        List<Object> newList = new ArrayList<>(index + 1);
        for (int i = 0; i < index; i++) {
            newList.add(null);
        }
        newList.add(value);
        return newList;
    }
}
