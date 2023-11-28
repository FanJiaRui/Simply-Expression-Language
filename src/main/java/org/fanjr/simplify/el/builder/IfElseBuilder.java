package org.fanjr.simplify.el.builder;

import org.fanjr.simplify.el.ELExecutor;
import org.fanjr.simplify.el.ELInvoker;
import org.fanjr.simplify.el.ELTokenUtils;
import org.fanjr.simplify.el.ElException;
import org.fanjr.simplify.el.invoker.statement.IfElseStatementInvoker;

import java.util.function.Supplier;

import static org.fanjr.simplify.el.ELTokenUtils.findNextCharToken;

/**
 * @author fanjr@vip.qq.com
 * @since 2023/9/28 上午11:10
 */
public class IfElseBuilder implements Supplier<ELInvoker> {

    private Supplier<ELInvoker> exp;
    private Supplier<ELInvoker> ifBlock;
    private Supplier<ELInvoker> elseBlock;
    private int firstEnd;
    private int end;

    private IfElseBuilder() {
        // skip
    }

    public static IfElseBuilder matchBuild(char[] chars, int start, int end) {
        // 至少7个字符或以上才可能构成if语句块 例如if(x){}
        if (end - start < 7) {
            return null;
        }
        // 关键字检测
        if (chars[start] != 'i' || chars[start + 1] != 'f') {
            return null;
        }
        start += 2;
        start += ELTokenUtils.findHeadSpace(chars, start, end);

        if (end <= start || chars[start] != '(') {
            return null;
        }

        // 解析条件表达式
        IfElseBuilder target = new IfElseBuilder();
        {
            start += 1;
            int nextToken = findNextCharToken(chars, ')', start, end);
            {
                int pre = start;
                target.exp = () -> ELExecutor.resolve(chars, pre, nextToken);
            }
            start = nextToken + 1;
        }
        // 解析第一段执行语句
        {
            start += ELTokenUtils.findHeadSpace(chars, start, end);
            if (chars[start] != '{') {
                throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，if语句后缺少可执行语句，应该为if(...){...}");
            }
            start += 1;
            int nextToken = findNextCharToken(chars, '}', start, end);
            target.firstEnd = nextToken;
            {
                int pre = start;
                target.ifBlock = () -> ELExecutor.resolve(chars, pre, nextToken);
            }
            start = nextToken + 1;
        }
        // 解析else
        {
            start += ELTokenUtils.findHeadSpace(chars, start, end);
            if (start >= end || end - start < 6) {
                // 结尾结束if分支
                target.end = target.firstEnd;
                return target;
            } else if (chars[start] == ';') {
                // 结束if分支
                target.end = start;
                return target;
            } else if (chars[start] != 'e'
                    || chars[start + 1] != 'l'
                    || chars[start + 2] != 's'
                    || chars[start + 3] != 'e') {
                target.end = target.firstEnd;
                return target;
            }
        }

        // 解析第二段执行语句
        {
            start += 4;
            start += ELTokenUtils.findHeadSpace(chars, start, end);
            if (start >= end || end - start < 2) {
                throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，else语句后缺少可执行语句，应该为else{...}或else if(...){...}");
            }
            if (chars[start] == '{') {
                start += 1;
                int nextToken = findNextCharToken(chars, '}', start, end);
                {
                    int pre = start;
                    target.elseBlock = () -> ELExecutor.resolve(chars, pre, nextToken);
                }
                target.end = nextToken;
                return target;
            } else if (chars[start] == 'i' && chars[start + 1] == 'f') {
                IfElseBuilder ifElseBuilder = IfElseBuilder.matchBuild(chars, start, end);
                if (null != ifElseBuilder) {
                    target.end = ifElseBuilder.getEnd();
                    target.elseBlock = ifElseBuilder;
                    return target;
                }
            }


            throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，else语句后缺少可执行语句，应该为else{...}或else if(...){...}");
        }
    }


    @Override
    public ELInvoker get() {
        if (null == elseBlock) {
            return IfElseStatementInvoker.buildIf(exp.get(), ifBlock.get());
        } else {
            return IfElseStatementInvoker.buildIfElse(exp.get(), ifBlock.get(), elseBlock.get());
        }
    }

    public int getEnd() {
        return end;
    }
}
