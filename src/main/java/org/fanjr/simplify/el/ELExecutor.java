package org.fanjr.simplify.el;


import org.fanjr.simplify.el.builder.*;
import org.fanjr.simplify.el.invoker.*;
import org.fanjr.simplify.el.invoker.calculate.*;
import org.fanjr.simplify.el.invoker.node.*;
import org.fanjr.simplify.utils.ElUtils;
import org.fanjr.simplify.utils.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.fanjr.simplify.el.ELTokenUtils.*;

/**
 * EL表达式计算工具
 *
 * @author fanjr@vip.qq.com
 * @file ElUtils.java
 * @since 2021/6/29 上午10:50
 */
public class ELExecutor {
    private static final Map<String, EL> COMPILES = new ConcurrentHashMap<>();

    public static <T> T eval(String el, Object vars, Class<T> type) {
        return ElUtils.cast(eval(el, vars), type);
    }

    public static Object eval(String el, Object vars, Type type) {
        return ElUtils.cast(eval(el, vars), type);
    }

    public static Object eval(String el, Object vars) {
        EL elInstance = COMPILES.get(el);
        if (null == elInstance) {
            elInstance = compile(el);
        }
        return elInstance.invoke(vars);
    }

    public static EL compile(final String el) {
        return COMPILES.computeIfAbsent(el, ELExecutor::doCompile);
    }

    private static EL doCompile(String el) {
        EL elInstance;

        char[] chars = el.toCharArray();
        int start = 0;
        int end = el.length();

        //Trim
        start += findHeadSpace(chars, start, end);
        end -= findEndSpace(chars, start, end);

        int elStart = findElStart(chars, start, end);
        if (-1 == elStart) {
            elInstance = new SimpleEL(resolve(chars, start, end));
            return elInstance;
        }

        if (elStart == start && chars[end - 1] == '}' && -1 == findElStart(chars, start + 1, end)) {
            start += 2;
            elInstance = new SimpleEL(resolve(chars, start, end - 1));
            return elInstance;
        }

        List<ELInvoker> targets = new ArrayList<>();
        while (start < end) {
            int elEnd = findNextCharToken(chars, '}', elStart + 2, end);
            if (elStart != start) {
                targets.add(StringInvoker.newInstance(new String(chars, start, elStart - start)));
            }
            elStart += 2;
            targets.add(resolve(chars, elStart, elEnd));
            start = elEnd + 1;
            elStart = findElStart(chars, start, end);
            if (-1 == elStart) {
                //最后是一段字符串
                targets.add(StringInvoker.newInstance(new String(chars, start, end - start)));
                start = end;
            }
        }
        elInstance = new SpliceEL(targets);
        return elInstance;
    }

    private static int findElStart(char[] chars, int start, int end) {
        int elStart = findChar(chars, '$', start, end);
        if (elStart == -1) {
            return -1;
        } else {
            if (elStart + 1 < end && '{' == chars[elStart + 1]) {
                return elStart;
            } else {
                return findChar(chars, '$', elStart + 1, end);
            }
        }
    }

    private static int findChar(char[] chars, char c, int start, int end) {
        for (int i = start; i < end; i++) {
            if (c == chars[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param chars 要解析的字符数组
     * @param start 从哪开始，第一位为index为0
     * @param end   在哪结束，最后一位index为end - 1
     * @return
     */
    public static ELInvoker resolve(char[] chars, int start, int end) {
        try {
            checkEL(chars, start, end);
            LinkedList<Supplier<ELInvoker>> builderStack = new LinkedList<>();
            while (start < end) {
                //trim
                start += findHeadSpace(chars, start, end);
                end -= findEndSpace(chars, start, end);

                //匹配分隔符;
                int nextSemicolonToken = findNextCharToken(chars, ';', start, end, false);
                if (nextSemicolonToken != -1) {
                    List<ELInvoker> targetInvokers = new ArrayList<>();
                    do {
                        ELInvoker subInvoker = resolve(chars, start, nextSemicolonToken);
                        if (null != subInvoker) {
                            targetInvokers.add(subInvoker);
                        }
                        start = nextSemicolonToken + 1;
                        nextSemicolonToken = findNextCharToken(chars, ';', start, end, false);
                        if (nextSemicolonToken == -1) {
                            ELInvoker lastInvoker = resolve(chars, start, end);
                            if (null != lastInvoker) {
                                targetInvokers.add(lastInvoker);
                            }
                            break;
                        }
                    } while ((start < end));
                    return CompositeInvoker.newInstance(targetInvokers);
                }

                //匹配赋值指令
                {
                    BinocularBuilder binocularBuilder = AssignmentBuilder.matchNextBuildByOrder(chars, start, end, 0);
                    if (null != binocularBuilder) {
                        return binocularBuilder.get();
                    }
                }

                //匹配三元表达式
                {
                    int nextQuestion = findNextCharToken(chars, '?', start, end, false);
                    if (nextQuestion != -1) {
                        ELInvokerBuilder elInvokerBuilder = new ELInvokerBuilder(3, TernaryInvoker::buildInstance);
                        elInvokerBuilder.pushInvoker(resolve(chars, start, nextQuestion));
                        int nextToken = findNextCharToken(chars, ':', nextQuestion + 1, end);
                        elInvokerBuilder.pushInvoker(resolve(chars, nextQuestion + 1, nextToken));
                        elInvokerBuilder.pushInvoker(resolve(chars, nextToken + 1, end));
                        return elInvokerBuilder.get();
                    }
                }

                //匹配二元表达式
                for (int i = 1; i <= 5; i++) {
                    BinocularBuilder binocularBuilder = CalculateEnum.matchNextBuildByOrder(chars, start, end, i);
                    if (null != binocularBuilder) {
                        return binocularBuilder.get();
                    }
                }

                //匹配一元表达式 (++、--、!)
                if ('!' == chars[start]) {
                    //取反操作
                    builderStack.addLast(new ELInvokerBuilder(1, NegationInvoker::buildInstance));
                    start++;
                    continue;
                } else if (end - start > 2) {
                    if (chars[start] == '+' && chars[start + 1] == '+') {
                        ELInvokerBuilder elInvokerBuilder = new ELInvokerBuilder(1, PreIncrementInvoker::buildInstance);
                        elInvokerBuilder.pushInvoker(resolve(chars, start + 2, end));
                        return elInvokerBuilder.get();
                    }
                    if (chars[start] == '-' && chars[start + 1] == '-') {
                        ELInvokerBuilder elInvokerBuilder = new ELInvokerBuilder(1, PreDecrementInvoker::buildInstance);
                        elInvokerBuilder.pushInvoker(resolve(chars, start + 2, end));
                        return elInvokerBuilder.get();
                    }
                    if (chars[end - 1] == '+' && chars[end - 2] == '+') {
                        ELInvokerBuilder elInvokerBuilder = new ELInvokerBuilder(1, IncrementInvoker::buildInstance);
                        elInvokerBuilder.pushInvoker(resolve(chars, start, end - 2));
                        return elInvokerBuilder.get();
                    }
                    if (chars[end - 1] == '-' && chars[end - 2] == '-') {
                        ELInvokerBuilder elInvokerBuilder = new ELInvokerBuilder(1, DecrementInvoker::buildInstance);
                        elInvokerBuilder.pushInvoker(resolve(chars, start, end - 2));
                        return elInvokerBuilder.get();
                    }
                }

                //第一个有效字符为"，这是一段字符串
                if ('"' == chars[start]) {
                    int nextToken = findStringEndDouble(chars, start + 1, end);
                    String value = new String(chars, start + 1, nextToken - start - 1);
                    if (checkDot(chars, nextToken + 1, end)) {
                        pushNotBuild(builderStack, StringInvoker.newInstance(value));
                    } else {
                        pushOrBuild(builderStack, StringInvoker.newInstance(value));
                    }
                    start = nextToken + 1;
                    continue;
                }

                //第一个有效字符为'，这是一段字符串
                if ('\'' == chars[start]) {
                    int nextToken = findStringEndSingle(chars, start + 1, end);
                    String value = new String(chars, start + 1, nextToken - start - 1);
                    if (checkDot(chars, nextToken + 1, end)) {
                        pushNotBuild(builderStack, StringInvoker.newInstance(value));
                    } else {
                        pushOrBuild(builderStack, StringInvoker.newInstance(value));
                    }
                    start = nextToken + 1;
                    continue;
                }

                if ('(' == chars[start]) {
                    int nextToken = findNextCharToken(chars, ')', start + 1, end);
                    Supplier<ELInvoker> builder = ConversionBuilder.matchBuild(chars, start, nextToken + 1);
                    if (null != builder) {
                        builderStack.addLast(builder);
                    } else {
                        if (checkDot(chars, nextToken + 1, end)) {
                            pushNotBuild(builderStack, resolve(chars, start + 1, nextToken));
                        } else {
                            pushOrBuild(builderStack, resolve(chars, start + 1, nextToken));
                        }
                    }
                    start = nextToken + 1;
                    continue;
                }

                if ('[' == chars[start]) {
                    int nextToken = findNextCharToken(chars, ']', start + 1, end);
                    if (checkDot(chars, nextToken + 1, end)) {
                        pushNotBuild(builderStack, resolveList(chars, start, nextToken + 1));
                    } else {
                        pushOrBuild(builderStack, resolveList(chars, start, nextToken + 1));
                    }
                    start = nextToken + 1;
                    continue;
                }

                if ('{' == chars[start]) {
                    int nextToken = findNextCharToken(chars, '}', start + 1, end);
                    if (checkDot(chars, nextToken + 1, end)) {
                        pushNotBuild(builderStack, resolveJson(chars, start, nextToken + 1));
                    } else {
                        pushOrBuild(builderStack, resolveJson(chars, start, nextToken + 1));
                    }
                    start = nextToken + 1;
                    continue;
                }

                //没有下一个token了，这可能是一个单独可运行的EL
                //判断是否为关键字
                {
                    Supplier<ELInvoker> matchBuild = KeywordBuilder.matchBuild(chars, start, end);
                    if (null != matchBuild) {
                        builderStack.addLast(matchBuild);
                        start = end;
                        continue;
                    }
                }

                //判断是否为数字
                if (chars[start] <= '9' && chars[start] >= '0') {
                    String numStr = new String(chars, start, end - start);
                    if (ElUtils.isNumber(numStr)) {
                        pushOrBuild(builderStack, NumberInvoker.newInstance(numStr));
                        start = end;
                        continue;
                    } else {
                        throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常,错误的数字或变量：" + numStr);
                    }
                }

                //判断是否为new 关键字开头,例如 new A(a,b,c)
                if (end - start >= 7 && "new ".equals(String.valueOf(chars, start, 4))) {
                    //定位括号的位置
                    int nextCharTokenStart = findNextCharToken(chars, '(', start + 4, end);
                    int nextCharTokenEnd = findNextCharToken(chars, ')', nextCharTokenStart + 1, end);
                    ArrayInvoker paramsInvoker = resolveList(chars, nextCharTokenStart, nextCharTokenEnd + 1);
                    pushOrBuild(builderStack,
                            NewObjectNodeInvoker.newInstance(
                                    String.valueOf(chars, start, nextCharTokenEnd + 1 - start),
                                    String.valueOf(chars, start + 4, nextCharTokenStart - start - 4),
                                    paramsInvoker));
                    start = nextCharTokenEnd + 1;
                    continue;
                }


                //判断取值&赋值
                int nextDot = findNextCharToken(chars, '.', start, end, false);
                if (nextDot == -1) {
                    //没有.分割
                    NodeInvoker curr = resolveNode(chars, start, end);
                    pushOrBuild(builderStack, curr);
                    start = end + 1;
                    continue;
                }

                NodeInvoker parent = null;
                if (start == nextDot) {
                    //先取左边的表达式
                    ELInvoker left = builderStack.removeLast().get();
                    parent = FirstNodeInvoker.newInstance(left);
                    start++;
                } else {
                    //判断是否为this.开头
                    String key = new String(chars, start, nextDot - start);
                    if ("this".equals(key)) {
                        parent = RootNodeInvoker.INSTANCE;
                        start += 5;
                    }
                }
                do {
                    nextDot = findNextCharToken(chars, '.', start, end, false);
                    if (-1 == nextDot) {
                        nextDot = end;
                    }
                    NodeInvoker curr = resolveNode(chars, start, nextDot);
                    if (parent != null) {
                        curr.setParentNodeInvoker(parent);
                    }
                    parent = curr;
                    start = nextDot + 1;
                } while (start < end);
                pushOrBuild(builderStack, parent);
            }

            return buildAll(builderStack);
        } catch (Exception e) {
            if (e instanceof ElException) {
                throw e;
            } else {
                throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常,问题可能存在于[" + start + "," + end + "]", e);
            }
        }
    }

    private static boolean checkDot(char[] chars, int index, int end) {
        if (index < end) {
            return chars[index] == '.';
        }
        return false;
    }

    private static NodeInvoker resolveNode(char[] chars, int start, int end) {
        checkEL(chars, start, end);

        //判断是否为数组
        int nextToken = findNextCharToken(chars, '[', start, end, false);
        String nodeName = new String(chars, start, end - start);
        if (nextToken != -1) {
            if (chars[end - 1] != ']') {
                throw new ElException("解析错误！错误的节点:" + nodeName);
            } else {
                int lastArrayIndex = findLastCharToken(chars, '[', start, end - 1, false);
                NodeInvoker parent = resolveNode(chars, start, lastArrayIndex);
                return IndexNodeInvoker.newInstance(nodeName, parent, resolve(chars, lastArrayIndex + 1, end - 1));
            }
        }

        //判断是否为方法
        nextToken = findNextCharToken(chars, '(', start, end, false);
        if (-1 != nextToken) {
            if (chars[end - 1] != ')') {
                throw new ElException("解析错误！错误的节点:" + nodeName);
            } else {
                return MethodNodeInvoker.newInstance(nodeName, new String(chars, start, nextToken - start), resolveList(chars, nextToken, end));
            }
        }

        //其他情况为普通取节点值
        return MapNodeInvoker.newInstance(nodeName);
    }

    private static ELInvoker resolveJson(char[] chars, int start, int end) {
        String jsonStr = new String(chars, start, end - start);
        List<Pair<ELInvoker, ELInvoker>> itemInvokers = new ArrayList<>();
        //排除括号
        start += 1;
        end -= 1;
        while (start < end) {
            int nextComma = findNextCharToken(chars, ',', start, end, false);
            if (-1 == nextComma) {
                nextComma = end;
            }

            //在下一个逗号前寻找冒号
            int nextColon = findNextCharToken(chars, ':', start, nextComma, false);
            if (-1 == nextColon) {
                throw new ElException("解析错误！错误的JSON:" + jsonStr);
            }
            Pair<ELInvoker, ELInvoker> pair = new Pair<>(resolve(chars, start, nextColon), resolve(chars, nextColon + 1, nextComma));
            itemInvokers.add(pair);
            start = nextComma + 1;
        }
        return JsonInvoker.newInstance(jsonStr, itemInvokers);
    }

    private static ArrayInvoker resolveList(char[] chars, int start, int end) {
        String arrStr = new String(chars, start, end - start);
        List<ELInvoker> itemInvoker = new ArrayList<>();
        //排除括号
        start += 1;
        end -= 1;
        while (start < end) {
            int nextToken = findNextCharToken(chars, ',', start, end, false);
            if (-1 == nextToken) {
                nextToken = end;
            }
            itemInvoker.add(resolve(chars, start, nextToken));
            start = nextToken + 1;

        }
        return ArrayInvoker.newInstance(arrStr, itemInvoker);
    }

    private static ELInvoker buildAll(LinkedList<Supplier<ELInvoker>> builderStack) {
        if (builderStack.size() == 0) {
            return null;
        } else if (builderStack.size() == 1) {
            return builderStack.getLast().get();
        } else {
            LinkedList<ELInvoker> target = new LinkedList<>();
            while (null != builderStack.peekFirst()) {
                Supplier<ELInvoker> supplier = builderStack.removeLast();
                if (supplier instanceof ELInvokerBuilder) {
                    if (!((ELInvokerBuilder) supplier).check()) {
                        for (int i = 0; i < ((ELInvokerBuilder) supplier).needNum(); i++) {
                            ((ELInvokerBuilder) supplier).pushInvoker(target.pollFirst());
                        }
                    }
                }
                target.addLast(supplier.get());
            }
            if (target.size() == 1) {
                return target.get(0);
            } else {
                throw new ElException("表达式解析错误！");
            }
        }
    }

    private static void pushNotBuild(LinkedList<Supplier<ELInvoker>> builderStack, ELInvoker invoker) {
        if (null == invoker) {
            return;
        }
        builderStack.offerLast(() -> invoker);
    }

    private static void pushOrBuild(LinkedList<Supplier<ELInvoker>> builderStack, ELInvoker invoker) {
        if (null == invoker) {
            return;
        }
        Supplier<ELInvoker> supplier = builderStack.peekLast();
        if (supplier instanceof ELInvokerBuilder) {
            if (!((ELInvokerBuilder) supplier).check()) {
                ((ELInvokerBuilder) supplier).pushInvoker(invoker);
                if (((ELInvokerBuilder) supplier).check()) {
                    ELInvoker inner = builderStack.removeLast().get();
                    pushOrBuild(builderStack, inner);
                }
            } else {
                ELInvoker inner = builderStack.removeLast().get();
                pushOrBuild(builderStack, inner);
                pushOrBuild(builderStack, invoker);
            }
        } else {
            builderStack.offerLast(() -> invoker);
        }
    }

}
