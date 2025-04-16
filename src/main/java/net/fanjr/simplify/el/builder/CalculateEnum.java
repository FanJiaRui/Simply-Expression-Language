package net.fanjr.simplify.el.builder;


import net.fanjr.simplify.el.ELExecutor;
import net.fanjr.simplify.el.ELInvoker;
import net.fanjr.simplify.el.ELTokenUtils;
import net.fanjr.simplify.el.invoker.calculate.*;
import net.fanjr.simplify.utils.SimplifyException;

import java.util.LinkedList;
import java.util.function.Function;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/5 下午3:33
 */
public enum CalculateEnum {

    ASSIGNMENT(0, "=", SetValueInvoker::buildInstance) {
        /**
         * 防止和!=、==、+=、-=、*=、/=、>=、<=冲突判断
         * @param chars
         * @param index
         * @return
         */
        @Override
        protected boolean doMatch(char[] chars, int index, int start, int end) {
            if (chars[index] == '=') {
                if (index > start && (
                        chars[index - 1] == '!' ||
                                chars[index - 1] == '=' ||
                                chars[index - 1] == '~' ||
                                chars[index - 1] == '+' ||
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
    },

    AND(1, "&&", AndInvoker::buildInstance),

    OR(1, "||", OrInvoker::buildInstance),

    UNEQUAL(2, "!=", EqualsInvoker::buildNegationInstance),

    EQUAL(2, "==", EqualsInvoker::buildInstance),

    REGEXP(2, "~=", RegExpInvoker::buildInstance),

    EQUAL_OR_GREATER(2, ">=", EqualOrGreaterInvoker::buildInstance),

    GREATER(2, ">", GreaterInvoker::buildInstance),

    EQUAL_OR_LESS(2, "<=", EqualOrLessInvoker::buildInstance),

    LESS(2, "<", LessInvoker::buildInstance),

    ADD(3, "+", AddInvoker::buildInstance) {
        /**
         * 防止和++\+=冲突判断
         * 特殊情况+++ 则最后一个+为加法计算
         * @param chars
         * @param index
         * @return
         */
        @Override
        protected boolean doMatch(char[] chars, int index, int start, int end) {
            if (chars[index] == '+') {
                if (index - 2 >= start && chars[index - 1] == '+' && chars[index - 2] == '+') {
                    if (index - 3 >= start) {
                        if (chars[index - 3] == '+'){
                            //存在++++，语法异常！
                            throw new SimplifyException("解析表达式【" + String.valueOf(chars) + "】发生异常,存在多余的【+】");
                        }else {
                            return true;
                        }
                    }
                }

                if (index > start && chars[index - 1] == '+') {
                    return false;
                }
                return index + 1 >= end || (chars[index + 1] != '+' && chars[index + 1] != '=');
            }
            return false;
        }
    },

    SUBTRACT(3, "-", SubtractInvoker::buildInstance) {
        /**
         * 防止和--/-=冲突判断
         * @param chars
         * @param index
         * @return
         */
        @Override
        protected boolean doMatch(char[] chars, int index, int start, int end) {
            if (chars[index] == '-') {
                if (index - 2 >= start && chars[index - 1] == '-' && chars[index - 2] == '-') {
                    if (index - 3 >= start) {
                        if (chars[index - 3] == '-'){
                            //存在----，语法异常！
                            throw new SimplifyException("解析表达式【" + String.valueOf(chars) + "】发生异常,存在多余的【-】");
                        }else {
                            return true;
                        }
                    }
                }

                if (index > start && chars[index - 1] == '-') {
                    return false;
                }
                return index + 1 >= end || (chars[index + 1] != '-' && chars[index + 1] != '=');
            }
            return false;
        }
    },

    MULTIPLY(4, "*", MultiplyInvoker::buildInstance) {
        /**
         * 防止和*=冲突判断
         * @param chars
         * @param index
         * @return
         */
        @Override
        protected boolean doMatch(char[] chars, int index, int start, int end) {
            if (chars[index] == '*') {
                return index + 1 >= end || chars[index + 1] != '=';
            }
            return false;
        }
    },

    DIVIDE(4, "/", DivideInvoker::buildInstance) {
        /**
         * 防止和/=冲突判断
         * @param chars
         * @param index
         * @return
         */
        @Override
        protected boolean doMatch(char[] chars, int index, int start, int end) {
            if (chars[index] == '/') {
                return index + 1 >= end || chars[index + 1] != '=';
            }
            return false;
        }
    },

    REMAINDER(4, "%", RemainderInvoker::buildInstance),

    DIVIDE_SET(5, "/=", SetAndDivideValueInvoker::buildInstance),

    MULTIPLY_SET(5, "*=", SetAndMultiplyValueInvoker::buildInstance),

    ADD_SET(5, "+=", SetAndAddValueInvoker::buildInstance),

    SUBTRACT_SET(5, "-=", SetAndSubtractValueInvoker::buildInstance),

    ;

    private final char[] keyword;
    private final int order;
    private final Function<LinkedList<ELInvoker>, ELInvoker> buildFunction;

    CalculateEnum(int order, String keyword, Function<LinkedList<ELInvoker>, ELInvoker> buildFunction) {
        this.order = order;
        this.keyword = keyword.toCharArray();
        this.buildFunction = buildFunction;
    }

    public static BinocularBuilder matchNextBuildByOrder(char[] chars, int start, int end, int order) {
        for (int i = end - 1; i >= start; i--) {
            //匹配token
            for (CalculateEnum calculateEnum : CalculateEnum.values()) {
                if (calculateEnum.order == order && calculateEnum.doMatch(chars, i, start, end)) {
                    return calculateEnum.getBuilder(chars, i, start, end);
                }
            }


            //匹配层级
            switch (chars[i]) {
                case '"':
                    i = ELTokenUtils.findStringStartDouble(chars, start, i);
                    break;
                case '\'':
                    i = ELTokenUtils.findStringStartSingle(chars, start, i);
                    break;
                case ')':
                    i = ELTokenUtils.findLastCharToken(chars, '(', start, i);
                    break;
                case ']':
                    i = ELTokenUtils.findLastCharToken(chars, '[', start, i);
                    break;
                case '}':
                    i = ELTokenUtils.findLastCharToken(chars, '{', start, i);
                    break;
                default:
                    break;
            }
        }

        //未匹配到
        return null;
    }

    protected BinocularBuilder getBuilder(char[] chars, int tokenIndex, int start, int end) {
        BinocularBuilder binocularBuilder = new BinocularBuilder(tokenIndex - keyword.length + 1, tokenIndex + 1, buildFunction);
        binocularBuilder.setLeft(ELExecutor.resolve(chars, start, tokenIndex - keyword.length + 1));
        binocularBuilder.setRight(ELExecutor.resolve(chars, tokenIndex + 1, end));
        return binocularBuilder;
    }

    protected boolean doMatch(char[] chars, int index, int start, int end) {
        if (index - start + 1 >= keyword.length) {
            for (int i = keyword.length - 1, j = index; (i >= 0); i--, j--) {
                if (keyword[i] != chars[j]) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}
