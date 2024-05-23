package org.fanjr.simplify.el;


import org.fanjr.simplify.el.builder.*;
import org.fanjr.simplify.el.cache.ELCacheManager;
import org.fanjr.simplify.el.invoker.*;
import org.fanjr.simplify.el.invoker.calculate.*;
import org.fanjr.simplify.el.invoker.node.*;
import org.fanjr.simplify.el.reflect.ELFunctionInvokeUtils;
import org.fanjr.simplify.utils.ElUtils;
import org.fanjr.simplify.utils.Pair;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.fanjr.simplify.el.ELTokenUtils.*;

/**
 * EL表达式计算工具
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/29 上午10:50
 */
public class ELExecutor {
    private static final String LOCK_KEY = "E_L_KEY.";

    /**
     * 生成表达式对象
     *
     * @param el 表达式字符串
     * @return 可执行的表达式对象
     */
    public static EL compile(final String el) {
        if (ElUtils.isBlank(el)) {
            // 容错处理
            return NullEL.INSTANCE;
        }

        EL target = ELCacheManager.getEL(el);
        if (null != target) {
            return target;
        }

        return doCompile(el);
    }

    /**
     * 生成节点对象
     *
     * @param nodeName 节点名
     * @return 可执行的节点对象
     */
    public static Node compileNode(String nodeName) {
        if (ElUtils.isBlank(nodeName)) {
            // 容错处理
            return NullNodeInvoker.INSTANCE;
        }

        Node target = ELCacheManager.getNode(nodeName);
        if (null != target) {
            return target;
        }

        return doCompileNode(nodeName.toCharArray(), 0, nodeName.length());
    }

    /**
     * 计算表达式，返回结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @return 计算结果
     */
    public static Object eval(String el, Object vars) {
        return compile(el).invoke(vars);
    }

    /**
     * 计算表达式，返回指定类型结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @param type 预期结果的类型
     * @return 计算结果
     */
    public static <T> T eval(String el, Object vars, Class<T> type) {
        return ElUtils.cast(eval(el, vars), type);
    }

    /**
     * 计算表达式，返回指定类型结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @param type 预期结果的类型
     * @return 计算结果
     */
    public static Object eval(String el, Object vars, Type type) {
        return ElUtils.cast(eval(el, vars), type);
    }

    /**
     * 从对象中获取某个属性或节点
     *
     * @param ctx      上下文/POJO
     * @param nodeName 属性名或节点名
     * @return 对应属性或节点的值
     */
    public static Object getNode(Object ctx, String nodeName) {
        Node node = compileNode(nodeName);
        return node.getNode(ctx);
    }

    /**
     * 将某个值放入对象指定属性或节点
     *
     * @param ctx      上下文/POJO
     * @param nodeName 属性名或节点名
     * @param value    要放入的值
     */
    public static void putNode(Object ctx, String nodeName, Object value) {
        Node node = compileNode(nodeName);
        node.putNode(ctx, value);
    }

    /**
     * 将某个值或节点移除或置空
     *
     * @param ctx      上下文/POJO
     * @param nodeName 要移除或置空的节点名
     */
    public static void removeNode(Object ctx, String nodeName) {
        Node node = compileNode(nodeName);
        node.removeNode(ctx);
    }

    /**
     * @param chars 要解析的字符数组
     * @param start 从哪开始，第一位为index为0
     * @param end   在哪结束，最后一位index为end - 1
     * @return 解析后的可执行表达式
     */
    public static ELInvoker resolve(char[] chars, int start, int end) {
        try {
            checkEL(chars, start, end);

            // 开始拆解当前层级复合语句块
            {
                int preStart = start;
                int preEnd = end;
                List<ELInvoker> targetInvokers = new ArrayList<>();
                do {
                    //trim
                    preStart += findHeadSpace(chars, preStart, preEnd);
                    preEnd -= findEndSpace(chars, preStart, preEnd);

                    // 匹配IF ELSE
                    IfElseBuilder ifElseBuilder = IfElseBuilder.matchBuild(chars, preStart, preEnd);
                    if (null != ifElseBuilder) {
                        preStart = ifElseBuilder.getEnd() + 1;
                        targetInvokers.add(ifElseBuilder.get());
                        if (preStart >= preEnd) {
                            break;
                        }
                        continue;
                    }

                    // 匹配for循环
                    ForEachBuilder forEachBuilder = ForEachBuilder.matchBuild(chars, preStart, preEnd);
                    if (null != forEachBuilder) {
                        preStart = forEachBuilder.getEnd() + 1;
                        targetInvokers.add(forEachBuilder.get());
                        if (preStart >= preEnd) {
                            break;
                        }
                        continue;
                    }

                    int nextSemicolonToken = findNextCharToken(chars, ';', preStart, preEnd, false);
                    if (nextSemicolonToken != -1) {
                        ELInvoker subInvoker = resolve(chars, preStart, nextSemicolonToken);
                        if (null != subInvoker) {
                            targetInvokers.add(subInvoker);
                        }
                        preStart = nextSemicolonToken + 1;
                        if (preStart >= preEnd) {
                            break;
                        }
                    } else {
                        if (targetInvokers.size() == 0) {
                            // 当前层级不是复合语句，跳出复合语句解析循环，进入后续解析
                            break;
                        }
                        ELInvoker subInvoker = resolve(chars, preStart, preEnd);
                        if (null != subInvoker) {
                            targetInvokers.add(subInvoker);
                        }
                        preStart = preEnd + 1;
                    }
                } while (preStart < preEnd);
                if (targetInvokers.size() == 1) {
                    // 单层直接返回第一个
                    return targetInvokers.get(0);
                } else if (targetInvokers.size() > 1) {
                    // 需要拆解为复合语句，结束返回
                    return CompositeInvoker.newInstance(targetInvokers);
                }
            }
            // 结束拆解当前层级复合语句块

            LinkedList<Supplier<ELInvoker>> builderStack = new LinkedList<>();
            while (start < end) {
                //trim
                start += findHeadSpace(chars, start, end);
                end -= findEndSpace(chars, start, end);

                // 匹配赋值指令
                {
                    BinocularBuilder binocularBuilder = AssignmentBuilder.matchNextBuildByOrder(chars, start, end, 0);
                    if (null != binocularBuilder) {
                        return binocularBuilder.get();
                    }
                }

                // 匹配三元表达式
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

                // 匹配二元表达式
                for (int i = 1; i <= 5; i++) {
                    BinocularBuilder binocularBuilder = CalculateEnum.matchNextBuildByOrder(chars, start, end, i);
                    if (null != binocularBuilder) {
                        return binocularBuilder.get();
                    }
                }

                // 匹配一元表达式 (++、--、!)
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

                // 第一个有效字符为"，这是一段字符串
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

                // 第一个有效字符为'，这是一段字符串
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

                NodeInvoker parent;
                if (start == nextDot) {
                    //先取左边的表达式
                    ELInvoker left = builderStack.removeLast().get();
                    parent = FirstNodeInvoker.newInstance(left);
                    start++;
                    do {
                        nextDot = findNextCharToken(chars, '.', start, end, false);
                        if (-1 == nextDot) {
                            nextDot = end;
                        }
                        NodeInvoker curr = resolveNode(chars, start, nextDot);
                        curr.setParentNodeInvoker(parent);
                        parent = curr;
                        start = nextDot + 1;
                    } while (start < end);
                } else {
                    // 判断是否包含自定义函数  eg : XX.fun(...)
                    int nextFunctionToken = findNextCharToken(chars, '(', start, end, false);
                    if (nextFunctionToken > 0) {
                        int nextFunctionEnd = findNextCharToken(chars, ')', nextFunctionToken + 1, end, true);
                        // 是方法，需要判断是否为自定义函数
                        int[] dots = findAllCharToken(chars, '.', start, nextFunctionToken);
                        if (dots.length == 1) {
                            // 已判断形式为XX.fun(...)形式，进一步判断是否为注册的function
                            String functionUtilsName = String.valueOf(chars, start, dots[0] - start);
                            if (ELFunctionInvokeUtils.hasUtils(functionUtilsName)) {
                                String methodName = String.valueOf(chars, dots[0] + 1, nextFunctionToken - dots[0] - 1);
                                parent = FunctionMethodNodeInvoker.newInstance(new String(chars, start, nextFunctionEnd + 1 - start), functionUtilsName, methodName, resolveList(chars, nextFunctionToken, nextFunctionEnd + 1));
                                // 位移到方法后
                                start = nextFunctionEnd + 1;
                                if (start >= end) {
                                    // 末尾退出解析并压栈
                                    pushOrBuild(builderStack, parent);
                                    continue;
                                }
                                // 寻找下一个非空字符
                                int spaceNum = findHeadSpace(chars, start, end);
                                // 位移到下一个非空白字符'.'
                                start += spaceNum;
                                if (chars[start] == '.') {
                                    // 跳过分隔符
                                    start++;
                                } else {
                                    throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，错误的方法后缀：" + String.valueOf(chars, start, end - start));
                                }
                                while (start < end) {
                                    nextDot = findNextCharToken(chars, '.', start, end, false);
                                    if (-1 == nextDot) {
                                        nextDot = end;
                                    }
                                    NodeInvoker curr = resolveNode(chars, start, nextDot);
                                    curr.setParentNodeInvoker(parent);
                                    parent = curr;
                                    start = nextDot + 1;
                                }
                                pushOrBuild(builderStack, parent);
                                continue;
                            }
                        }
                    }

                    parent = doCompileNode(chars, start, end);
                    //解析完节点，一次性移动到末尾
                    start = end + 1;
                }
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

    private static boolean checkDot(char[] chars, int index, int end) {
        if (index < end) {
            return chars[index] == '.';
        }
        return false;
    }

    private static EL doCompile(String el) {
        synchronized (LOCK_KEY + el) {
            EL elInstance = ELCacheManager.getEL(el);
            if (null != elInstance) {
                return elInstance;
            }

            char[] chars = el.toCharArray();
            int start = 0;
            int end = el.length();

            //Trim
            int startSpace = findHeadSpace(chars, start, end);
            int endSpace = findEndSpace(chars, start, end);
            start += startSpace;
            end -= endSpace;
            boolean trim = (0 != start + end);

            if (start >= end) {
                elInstance = NullEL.INSTANCE;
                ELCacheManager.putEL(el, elInstance);
                return elInstance;
            }
            String trimStr = new String(chars, start, end - start);
            elInstance = ELCacheManager.getEL(trimStr);
            if (null != elInstance) {
                ELCacheManager.putEL(el, elInstance);
                return elInstance;
            }

            int elStart = findElStart(chars, start, end);
            if (-1 == elStart) {
                elInstance = new SimpleEL(resolve(chars, start, end));
                ELCacheManager.putEL(el, elInstance);
                if (trim) {
                    ELCacheManager.putEL(trimStr, elInstance);
                }
                return elInstance;
            }

            if (elStart == start && chars[end - 1] == '}' && -1 == findElStart(chars, start + 1, end)) {
                start += 2;
                if ('#' == chars[elStart]) {
                    elInstance = new SimpleEL(doCompileNode(chars, start, end - 1));
                } else if ('$' == chars[elStart]) {
                    elInstance = new SimpleEL(resolve(chars, start, end - 1));
                } else {
                    throw new ElException("解析错误！错误的token:" + chars[elStart] + "{");
                }
                ELCacheManager.putEL(el, elInstance);
                if (trim) {
                    ELCacheManager.putEL(trimStr, elInstance);
                }
                return elInstance;
            }

            List<ELInvoker> targets = new ArrayList<>();
            while (start < end) {
                int elEnd = findNextCharToken(chars, '}', elStart + 2, end);
                if (elStart != start) {
                    // 非表达式部分，解析为字符串
                    targets.add(StringInvoker.newInstance(new String(chars, start, elStart - start)));
                }
                if ('#' == chars[elStart]) {
                    elStart += 2;
                    targets.add(doCompileNode(chars, elStart, elEnd));
                } else if ('$' == chars[elStart]) {
                    elStart += 2;
                    targets.add(resolve(chars, elStart, elEnd));
                } else {
                    throw new ElException("解析错误！错误的token:" + chars[elStart] + "{");
                }
                start = elEnd + 1;
                elStart = findElStart(chars, start, end);
                if (-1 == elStart) {
                    //最后是一段字符串
                    targets.add(StringInvoker.newInstance(new String(chars, start, end - start)));
                    start = end;
                }
            }
            elInstance = new SpliceEL(targets);
            ELCacheManager.putEL(el, elInstance);
            if (trim) {
                ELCacheManager.putEL(trimStr, elInstance);
            }
            return elInstance;
        }
    }

    private static NodeInvoker doCompileNode(char[] chars, int start, int end) {
        String nodeName = new String(chars, start, end - start);
        checkEL(chars, start, end);
        synchronized (LOCK_KEY + nodeName) {
            NodeInvoker node = ELCacheManager.getNode(nodeName);
            if (null != node) {
                return node;
            }

            //Trim
            int startSpace = findHeadSpace(chars, start, end);
            int endSpace = findEndSpace(chars, start, end);
            start += startSpace;
            end -= endSpace;
            boolean trim = (0 != start + end);

            if (start >= end) {
                node = NullNodeInvoker.INSTANCE;
                ELCacheManager.putNode(nodeName, node);
                return NullNodeInvoker.INSTANCE;
            }
            String trimStr = new String(chars, start, end - start);
            node = ELCacheManager.getNode(trimStr);
            if (null != node) {
                ELCacheManager.putNode(nodeName, node);
                return node;
            }

            //判断取值&赋值
            int nextDot = findNextCharToken(chars, '.', start, end, false);
            if (nextDot == -1) {
                //没有.分割
                node = resolveNode(chars, start, end);
                ELCacheManager.putNode(nodeName, node);
                if (trim) {
                    ELCacheManager.putNode(trimStr, node);
                }
                return node;
            }

            if (start == nextDot) {
                // 节点表达式错误
                throw new ElException("解析错误！错误的节点:" + nodeName);
            } else {
                //判断是否为this.开头
                String key = new String(chars, start, nextDot - start);
                if ("this".equals(key)) {
                    node = RootNodeInvoker.INSTANCE;
                    start += 5;
                }
            }

            do {
                nextDot = findNextCharToken(chars, '.', start, end, false);
                if (-1 == nextDot) {
                    nextDot = end;
                }
                NodeInvoker curr = resolveNode(chars, start, nextDot);
                if (node != null) {
                    curr.setParentNodeInvoker(node);
                }
                node = curr;
                start = nextDot + 1;
            } while (start < end);

            ELCacheManager.putNode(nodeName, node);
            if (trim) {
                ELCacheManager.putNode(trimStr, node);
            }
            return node;
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

    private static NodeInvoker resolveNode(char[] chars, int start, int end) {
        //判断是否为数组
        int nextToken = findNextCharToken(chars, '[', start, end, false);
        String nodeName = new String(chars, start, end - start);
        if (nextToken != -1) {
            if (chars[end - 1] != ']') {
                throw new ElException("解析错误！错误的节点:" + nodeName);
            } else {
                int lastArrayIndex = findLastCharToken(chars, '[', start, end - 1, false);
                NodeInvoker parent = null;
                if (start != lastArrayIndex) {
                    // 没有上一级，可能是多维数组
                    parent = resolveNode(chars, start, lastArrayIndex);
                }
                ELInvoker indexEl = resolve(chars, lastArrayIndex + 1, end - 1);
                if (indexEl instanceof StringInvoker) {
                    return IndexMapNodeInvoker.newInstance(nodeName, parent, (String) indexEl.invoke(null));
                } else {
                    return IndexNodeInvoker.newInstance(nodeName, parent, indexEl);
                }
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

        if ("this".equals(nodeName)) {
            // this关键字
            return RootNodeInvoker.INSTANCE;
        }

        //其他情况为普通取节点值
        return MapNodeInvoker.newInstance(nodeName);
    }
}
