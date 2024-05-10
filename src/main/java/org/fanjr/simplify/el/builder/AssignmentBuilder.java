package org.fanjr.simplify.el.builder;


import org.fanjr.simplify.el.ELExecutor;
import org.fanjr.simplify.el.ELTokenUtils;
import org.fanjr.simplify.el.invoker.calculate.SetValueInvoker;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/8/4 下午3:13
 */
public class AssignmentBuilder {


    public static BinocularBuilder matchNextBuildByOrder(char[] chars, int start, int end, int order) {
        for (int i = start; i < end; i++) {
            //匹配token

            if (doMatch(chars, i, start, end)) {
                return getBuilder(chars, i, start, end);
            }

            //匹配层级
            switch (chars[i]) {
                case '"':
                    i = ELTokenUtils.findStringEndDouble(chars, i + 1, end);
                    break;
                case '\'':
                    i = ELTokenUtils.findStringEndSingle(chars, i + 1, end);
                    break;
                case '(':
                    i = ELTokenUtils.findNextCharToken(chars, ')', i + 1, end);
                    break;
                case '[':
                    i = ELTokenUtils.findNextCharToken(chars, ']', i + 1, end);
                    break;
                case '{':
                    i = ELTokenUtils.findNextCharToken(chars, '}', i + 1, end);
                    break;
                default:
                    break;
            }
        }

        //未匹配到
        return null;
    }

    /**
     * 防止和!=、==、+=、-=、*=、/=、>=、<=冲突判断
     *
     * @param chars
     * @param index
     * @return
     */
    private static boolean doMatch(char[] chars, int index, int start, int end) {
        if (chars[index] == '=') {
            if (index > start && (
                    chars[index - 1] == '!' ||
                            chars[index - 1] == '=' ||
                            chars[index - 1] == '+' ||
                            chars[index - 1] == '~' ||
                            chars[index - 1] == '-' ||
                            chars[index - 1] == '*' ||
                            chars[index - 1] == '/' ||
                            chars[index - 1] == '>' ||
                            chars[index - 1] == '<')) {
                return false;
            }
            return index + 1 >= end || chars[index + 1] != '=';
        }
        return false;
    }

    protected static BinocularBuilder getBuilder(char[] chars, int tokenIndex, int start, int end) {
        BinocularBuilder binocularBuilder = new BinocularBuilder(tokenIndex, tokenIndex + 1, SetValueInvoker::buildInstance);
        binocularBuilder.setLeft(ELExecutor.resolve(chars, start, tokenIndex));
        binocularBuilder.setRight(ELExecutor.resolve(chars, tokenIndex + 1, end));
        return binocularBuilder;
    }
}
