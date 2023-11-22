package org.fanjr.simplify.el.builder;

import org.fanjr.simplify.el.ELExecutor;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELTokenUtils;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.node.Node;
import org.fanjr.simplify.el.invoker.statement.ForIterationStatementInvoker;

import java.util.function.Supplier;

import static org.fanjr.simplify.el.ELTokenUtils.findNextCharToken;

public class ForEachBuilder implements Supplier<ELInvoker> {

    private ELInvoker iteration;
    private Node item;
    private ELInvoker forBlock;
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
            int forEnd = findNextCharToken(chars, ')', start, end);
            int nextSemicolonToken = findNextCharToken(chars, ';', start, forEnd, false);
            if (-1 != nextSemicolonToken) {
                // 还不支持index模式
                throw new ElException("暂时不支持for index模式");
            } else {
                // 迭代器模式
                int nextColonToken = findNextCharToken(chars, ':', start, forEnd, false);
                target.item = ELExecutor.compileNode(new String(chars, start, nextColonToken - start));
                target.iteration = ELExecutor.resolve(chars, nextColonToken + 1, forEnd);
            }
            start = forEnd + 1;
        }

        // 解析循环内执行语句
        {
            start += ELTokenUtils.findHeadSpace(chars, start, end);
            if (chars[start] != '{') {
                throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，for语句后缺少可执行语句，应该为for(...){...}");
            }
            start += 1;
            int nextToken = findNextCharToken(chars, '}', start, end);
            target.end = nextToken;
            target.forBlock = ELExecutor.resolve(chars, start, nextToken);
        }

        return target;
    }


    @Override
    public ELInvoker get() {
        // 暂时只有迭代器
        return ForIterationStatementInvoker.buildFor(iteration, item, forBlock);
    }

    public int getEnd() {
        return end;
    }
}