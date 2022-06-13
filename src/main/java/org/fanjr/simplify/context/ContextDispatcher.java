package org.fanjr.simplify.context;


import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ContextDispatcher {

    private static final String CONTEXT_ROOT_KEY = "$context";

    private static final char ARRAY_END_CHAR = ']';

    private static final char ARRAY_START_CHAR = '[';

    private static final String SEPARATOR_STRING = "\\.";

    private static final Map<String, ContextDispatcher> POOL = new ConcurrentHashMap<>();

    private static final Map<Integer, ArrayPathProcessor> ARRAY_POOL = new ConcurrentHashMap<>();

    private static final Map<String, MapPathProcessor> MAP_POOL = new ConcurrentHashMap<>();

    private final IContextPathProcessor[] processors;

    private ContextDispatcher(String path) {
        List<IContextPathProcessor> list = new LinkedList<IContextPathProcessor>();
        if (CONTEXT_ROOT_KEY.equals(path)) {
            list.add(new RootProcessor());
        } else {
            String[] paths = path.split(SEPARATOR_STRING);
            for (String subPath : paths) {
                int arrIndex = getArrayStartCharIndex(subPath);
                if (arrIndex > 0) {
                    list.add(newMapProcessorInstance(subPath.substring(0, arrIndex)));
                    String[] arrPaths = subPath.substring(arrIndex + 1, subPath.length() - 1).split("]\\[");
                    for (String arrPaht : arrPaths) {
                        list.add(newArrayProcessorInstance(Integer.parseInt(arrPaht)));
                    }
                } else {
                    list.add(newMapProcessorInstance(subPath));
                }
            }
        }
        processors = list.toArray(new IContextPathProcessor[list.size()]);
    }

    public static ContextDispatcher getInstance(String path) {
        return POOL.computeIfAbsent(path, ContextDispatcher::new);
    }

    private static MapPathProcessor newMapProcessorInstance(String path) {
        return MAP_POOL.computeIfAbsent(path, MapPathProcessor::new);
    }

    private static ArrayPathProcessor newArrayProcessorInstance(int index) {
        return ARRAY_POOL.computeIfAbsent(index, ArrayPathProcessor::new);
    }

    private static int getArrayStartCharIndex(String path) {
        if (ARRAY_END_CHAR == path.charAt(path.length() - 1)) {
            return path.indexOf(ARRAY_START_CHAR);
        }
        return -1;
    }

    public Object doGet(Object source) {
        Object target = source;
        int i = 0;
        do {
            target = processors[i].doGetProcess(target);
            i++;
        } while (target != null && i < processors.length);
        return target;
    }

    public Object doPut(Object source, Object value, boolean ordered) {
        Object curr = source;
        int i = 0;
        Deque<Object> valueStack = new LinkedList<>();
        while (i < (processors.length - 1)) {
            valueStack.addLast(curr);
            curr = processors[i].doGetProcess(curr);
            if (null == curr) {
                Object currContainer = value;
                for (int j = processors.length - 1; j > i; j--) {
                    currContainer = processors[j].newContainer(currContainer, ordered);
                }
                return putAndCheckStatus(i, ordered, currContainer, valueStack);
            }
            i++;
        }
        valueStack.addLast(curr);
        return putAndCheckStatus(i, ordered, value, valueStack);
    }

    private Object putAndCheckStatus(int nowIndex, boolean ordered, Object value, Deque<Object> valueStack) {
        ContextProcessorResult result = processors[nowIndex].doPutProcess(valueStack.removeLast(), value, ordered);
        if (ContextProcessorStatus.SUCCESS == result.status) {
            return result.value;
        } else if (ContextProcessorStatus.NEED_PUT_PERENT == result.status) {
            return putAndCheckStatus(nowIndex - 1, ordered, result.value, valueStack);
        } else {
            throw new ContextException("未支持的返回状态!" + result.status);
        }
    }
}
