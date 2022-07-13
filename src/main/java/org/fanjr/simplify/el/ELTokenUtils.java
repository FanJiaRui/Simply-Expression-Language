package org.fanjr.simplify.el;


/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 上午11:50
 */
public class ELTokenUtils {

    private ELTokenUtils() {
        //skip
    }

    public static int findNextTokenStart(char[] chars, int start, int end) {
        for (int i = start; i < end; i++) {
            switch (chars[i]) {
                case '&':
                    if (i + 1 < end && '&' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '|':
                    if (i + 1 < end && '|' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '!':
                    if (i + 1 < end && '=' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '=':
                case '+':
                case '-':
                case '*':
                case '/':
                case '%':
                case '>':
                case '<':
                    return i;
                default:
                    break;
            }

            switch (chars[i]) {
                case '"':
                    i = findStringEndDouble(chars, i + 1, end);
                    break;
                case '\'':
                    i = findStringEndSingle(chars, i + 1, end);
                    break;
                case '(':
                    i = findNextCharToken(chars, ')', i + 1, end);
                    break;
                case '[':
                    i = findNextCharToken(chars, ']', i + 1, end);
                    break;
                case '{':
                    i = findNextCharToken(chars, '}', i + 1, end);
                    break;
                default:
                    break;
            }
        }
        return -1;
    }

    public static int findNextTokenStartByOrder(char[] chars, int start, int end, int order) {
        for (int i = start; i < end; i++) {
            switch (chars[i]) {
                case '&':
                    if (i + 1 < end && '&' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '|':
                    if (i + 1 < end && '|' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '!':
                    if (i + 1 < end && '=' == chars[i + 1]) {
                        return i;
                    } else {
                        break;
                    }
                case '=':
                case '+':
                case '-':
                case '*':
                case '/':
                case '%':
                case '>':
                case '<':
                    return i;
                default:
                    break;
            }

            switch (chars[i]) {
                case '"':
                    i = findStringEndDouble(chars, i + 1, end);
                    break;
                case '\'':
                    i = findStringEndSingle(chars, i + 1, end);
                    break;
                case '(':
                    i = findNextCharToken(chars, ')', i + 1, end);
                    break;
                case '[':
                    i = findNextCharToken(chars, ']', i + 1, end);
                    break;
                case '{':
                    i = findNextCharToken(chars, '}', i + 1, end);
                    break;
                default:
                    break;
            }
        }
        return -1;
    }


    public static int findNextCharToken(char[] chars, char c, int start, int end) {
        return findNextCharToken(chars, c, start, end, true);
    }

    /**
     * 检测是否存在多余)}]
     *
     * @param chars 待检测串
     * @param start 开始index
     * @param end   结束index（不包括）
     */
    public static void checkEL(char[] chars, int start, int end) {
        for (int i = 0; i < 3; i++) {
            char c = ")]}".charAt(i);
            int index = findNextCharToken(chars, c, start, end, false);
            if (-1 != index) {
                throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常,存在多余的【" + c + "】");
            }
        }
    }

    /**
     * 寻找下一个有效的同级token
     *
     * @param chars          内容
     * @param c              寻找的char
     * @param start          不能包括寻找的char
     * @param end            结束位置（不包括）
     * @param throwException 找不到char抛出异常
     * @return 找到的index
     */
    public static int findNextCharToken(char[] chars, char c, int start, int end, boolean throwException) {
        for (int i = start; i < end; i++) {
            if (chars[i] == c) {
                return i;
            }
            switch (chars[i]) {
                case '"':
                    i = findStringEndDouble(chars, i + 1, end);
                    break;
                case '\'':
                    i = findStringEndSingle(chars, i + 1, end);
                    break;
                case '(':
                    i = findNextCharToken(chars, ')', i + 1, end);
                    break;
                case '[':
                    i = findNextCharToken(chars, ']', i + 1, end);
                    break;
                case '{':
                    i = findNextCharToken(chars, '}', i + 1, end);
                    break;
                default:
                    break;
            }
        }
        if (throwException) {
            throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，缺少【" + c + "】");
        } else {
            return -1;
        }
    }

    public static int findLastCharToken(char[] chars, char c, int start, int end) {
        return findLastCharToken(chars, c, start, end, true);
    }

    /**
     * 从后往前有效的同级token
     *
     * @param chars          内容
     * @param c              寻找的char
     * @param start          不能包括寻找的char
     * @param end            结束位置（不包括）
     * @param throwException 找不到char抛出异常
     * @return 找到的index
     */
    public static int findLastCharToken(char[] chars, char c, int start, int end, boolean throwException) {
        for (int i = end - 1; i >= start; i--) {
            if (chars[i] == c) {
                return i;
            }
            switch (chars[i]) {
                case '"':
                    i = findStringStartDouble(chars, start, i);
                    break;
                case '\'':
                    i = findStringStartSingle(chars, start, i);
                    break;
                case ')':
                    i = findLastCharToken(chars, '(', start, i);
                    break;
                case ']':
                    i = findLastCharToken(chars, '[', start, i);
                    break;
                case '}':
                    i = findLastCharToken(chars, '{', start, i);
                    break;
                default:
                    break;
            }
        }
        if (throwException) {
            throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，缺少【" + c + "】");
        } else {
            return -1;
        }
    }

    /**
     * 寻找下一个字符串结束符"，找不到则说明EL存在问题
     *
     * @param chars 内容
     * @param start 不能包括起始的"
     * @param end   结束位置（不包括）
     * @return 找到的index
     */
    public static int findStringEndDouble(char[] chars, int start, int end) {
        for (int i = start; i < end; i++) {
            if (chars[i] == '"') {
                // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                if (chars[i - 1] != '\\' || chars[i - 2] == '\\') {
                    return i;
                }
            }
        }
        throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，未关闭的字符串！开启index:" + start);
    }

    /**
     * 寻找下一个字符串结束符'，找不到则说明EL存在问题
     *
     * @param chars 内容
     * @param start 不能包括起始的'
     * @param end   结束位置（不包括）
     * @return 找到的index
     */
    public static int findStringEndSingle(char[] chars, int start, int end) {
        for (int i = start; i < end; i++) {
            if (chars[i] == '\'') {
                // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                if (chars[i - 1] != '\\' || chars[i - 2] == '\\') {
                    return i;
                }
            }
        }
        throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，未关闭的字符串！开启index:" + start);
    }

    /**
     * 反向查找"，找不到则说明EL存在问题
     *
     * @param chars 内容
     * @param start 不能包括起始的"
     * @param end   结束位置（不包括）
     * @return 找到的index
     */
    public static int findStringStartDouble(char[] chars, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (chars[i] == '"') {
                // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                if (i == start) {
                    return i;
                }

                if (i - start == 1 && chars[i - 1] != '\\') {
                    return i;
                }

                if (i - start >= 2) {
                    // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                    if (chars[i - 1] != '\\' || chars[i - 2] == '\\') {
                        return i;
                    }
                }
            }
        }
        throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，未找到字符串起始位置！关闭index:" + end);
    }

    /**
     * 反向查找'，找不到则说明EL存在问题
     *
     * @param chars 内容
     * @param start 不能包括起始的'
     * @param end   结束位置（不包括）
     * @return 找到的index
     */
    public static int findStringStartSingle(char[] chars, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            if (chars[i] == '\'') {
                // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                if (i == start) {
                    return i;
                }

                if (i - start == 1 && chars[i - 1] != '\\') {
                    return i;
                }

                if (i - start >= 2) {
                    // \"不作为结束符，\\"作为结束符，其他情况均为结束符
                    if (chars[i - 1] != '\\' || chars[i - 2] == '\\') {
                        return i;
                    }
                }
            }
        }
        throw new ElException("解析表达式【" + String.valueOf(chars) + "】发生异常，未找到字符串起始位置！关闭index:" + end);
    }

    /**
     * 寻找从start开始有多少个空白字符串<br>
     * <p>0 &lt;= start &lt; end</p>
     *
     * @param chars 字符数组
     * @param start 起始查找位置，
     * @param end   结束查找位置
     * @return 空白字符数量, 大于等于0
     */
    public static int findHeadSpace(char[] chars, int start, int end) {
        int s = 0;
        label:
        for (int i = start; i < end; i++) {
            switch (chars[i]) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                case '　':
                    s++;
                    break;
                default:
                    break label;
            }
        }
        return s;
    }

    /**
     * 寻找找end开始，有多少个空白字符<br>
     * 0 &lt;= start &lt; end
     *
     * @param chars 字符数组
     * @param start 起始查找位置，
     * @param end   结束查找位置
     * @return 空白字符数量, 大于等于0
     */
    public static int findEndSpace(char[] chars, int start, int end) {
        int s = 0;
        label:
        for (int i = end - 1; i >= start; i--) {
            switch (chars[i]) {
                case ' ':
                case '\r':
                case '\n':
                case '\t':
                case '　':
                    s++;
                    break;
                default:
                    break label;
            }
        }
        return s;
    }

}
