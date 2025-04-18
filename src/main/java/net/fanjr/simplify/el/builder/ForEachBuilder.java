package net.fanjr.simplify.el.builder;

import net.fanjr.simplify.el.ELException;
import net.fanjr.simplify.el.ELExecutor;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELTokenUtils;
import net.fanjr.simplify.el.invoker.node.Node;
import net.fanjr.simplify.el.invoker.statement.ForIndexStatementInvoker;
import net.fanjr.simplify.el.invoker.statement.ForIterationStatementInvoker;

import java.util.function.Supplier;

public class ForEachBuilder implements Supplier<ELInvoker> {

    // 迭代循环
    private boolean iterationMode;
    private Supplier<ELInvoker> iteration;
    private Supplier<Node> item;

    // 类c循环
    private Supplier<ELInvoker> preEL;
    private Supplier<ELInvoker> condition;
    private Supplier<ELInvoker> endEL;

    private Supplier<ELInvoker> forBlock;
    private int end;

    private ForEachBuilder() {
        // skip
    }

    public static ForEachBuilder matchBuild(char[] chars, int start, int end) {

        // 至少10个字符或以上才可能构成for语句块 例如for(a:b){}
        if (end - start < 10) {
            return null;
        }
        // 关键字检测
        if (chars[start] != 'f' || chars[start + 1] != 'o' || chars[start + 2] != 'r') {
            return null;
        }

        start += 3;
        start += ELTokenUtils.findHeadSpace(chars, start, end);

        if (end <= start || chars[start] != '(') {
            return null;
        }

        // 解析循环条件内表达式
        ForEachBuilder target = new ForEachBuilder();
        {
            start += 1;
            int forEnd = ELTokenUtils.findNextCharToken(chars, ')', start, end);
            int firstSemicolonToken = ELTokenUtils.findNextCharToken(chars, ';', start, forEnd, false);
            if (-1 == firstSemicolonToken) {
                // 迭代器模式
                target.iterationMode = true;
                int nextColonToken = ELTokenUtils.findNextCharToken(chars, ':', start, forEnd, false);
                int pre = start;
                target.item = () -> ELExecutor.compileNode(new String(chars, pre, nextColonToken - pre));
                target.iteration = () -> ELExecutor.resolve(chars, nextColonToken + 1, forEnd);

            } else {
                // 类c模式
                int secondSemicolonToken = ELTokenUtils.findNextCharToken(chars, ';', firstSemicolonToken + 1, forEnd, false);
                if (secondSemicolonToken == -1) {
                    throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，for语句中语法错误，应该为for(x;y;z){...}或者for(a:b){...}");
                }
                int pre = start;
                target.preEL = () -> ELExecutor.resolve(chars, pre, firstSemicolonToken);
                target.condition = () -> ELExecutor.resolve(chars, firstSemicolonToken + 1, secondSemicolonToken);
                target.endEL = () -> ELExecutor.resolve(chars, secondSemicolonToken + 1, forEnd);
            }
            start = forEnd + 1;
        }

        // 解析循环内执行语句
        {
            start += ELTokenUtils.findHeadSpace(chars, start, end);
            if (chars[start] != '{') {
                throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，for语句后缺少可执行语句，应该为for(...){...}");
            }
            start += 1;
            int nextToken = ELTokenUtils.findNextCharToken(chars, '}', start, end);
            target.end = nextToken;
            int pre = start;
            target.forBlock = () -> ELExecutor.resolve(chars, pre, nextToken);
        }

        return target;
    }


    @Override
    public ELInvoker get() {
        if (iterationMode) {
            // 暂时只有迭代器
            return ForIterationStatementInvoker.buildFor(iteration.get(), item.get(), forBlock.get());
        } else {
            return ForIndexStatementInvoker.buildFor(preEL.get(), condition.get(), endEL.get(), forBlock.get());
        }
    }

    public int getEnd() {
        return end;
    }
}