package net.fanjr.simplify.el;


import java.util.stream.IntStream;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/9 上午11:50
 */
public abstract class ELTokenUtils {

    private ELTokenUtils() {
        //skip
    }

    /**
     * 在给定的字符数组中查找下一个标记（Token）的开始位置。
     *
     * @param chars 字符数组，包含要分析的字符串。
     * @param start 开始查找的位置索引。
     * @param end   结束查找的位置索引（不包括）。
     * @return 如果找到下一个标记的开始位置，则返回该位置的索引；否则返回-1。
     */
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

    /**
     * 在给定的字符数组中，按照指定的顺序查找下一个标记的起始位置。
     *
     * @param chars 字符数组
     * @param start 搜索起始位置
     * @param end   搜索结束位置
     * @param order 查找顺序
     * @return 找到的标记的起始位置，如果未找到则返回-1
     */
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


    /**
     * 在字符数组中查找下一个特定字符的位置。
     *
     * @param chars 字符数组
     * @param c     要查找的字符
     * @param start 查找的起始位置（包含）
     * @param end   查找的结束位置（不包含）
     * @return 下一个特定字符在字符数组中的位置，如果未找到则返回-1
     */
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
        for (char c : ")]}".toCharArray()) {
            int index = findNextCharToken(chars, c, start, end, false);
            if (-1 != index) {
                throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常,存在多余的【" + c + "】");
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
     * @param throwException 若未找到指定字符是否抛出异常
     * @return 如果找到指定字符，则返回其索引位置；若未找到且throwException为false，则返回-1；若未找到且throwException为true，则抛出异常
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
            throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，缺少【" + c + "】");
        } else {
            return -1;
        }
    }

    /**
     * 在给定的字符数组中查找最后一个出现的指定字符。
     *
     * @param chars 字符数组
     * @param c     需要查找的字符
     * @param start 开始查找的位置（包含）
     * @param end   结束查找的位置（不包含）
     * @return 返回指定字符在字符数组中最后一次出现的位置索引，如果不存在则返回 -1
     */
    public static int findLastCharToken(char[] chars, char c, int start, int end) {
        return findLastCharToken(chars, c, start, end, true);
    }

    /**
     * 从后往前有效的同级token
     *
     * @param chars          字符数组，表示需要解析的字符串
     * @param c              需要查找的字符
     * @param start          不能包括寻找的char
     * @param end            结束位置（不包括）
     * @param throwException 找不到char抛出异常
     * @return 返回找到的字符的索引，如果没有找到并且throwException为false，则返回-1
     * @throws ELException 如果没有找到指定的字符且throwException为true时抛出此异常
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
            throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，缺少【" + c + "】");
        } else {
            return -1;
        }
    }

    /**
     * 查找字符数组中所有指定字符的位置索引，并忽略字符串中的引号、括号内的内容。
     *
     * @param chars 字符数组
     * @param c     要查找的字符
     * @param start 查找的起始索引
     * @param end   查找的结束索引
     * @return 包含所有指定字符位置的索引数组
     */
    public static int[] findAllCharToken(char[] chars, char c, int start, int end) {
        IntStream.Builder builder = IntStream.builder();
        for (int i = start; i < end; i++) {
            if (chars[i] == c) {
                builder.add(i);
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
        return builder.build().toArray();
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
        throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，未关闭的字符串！开启index:" + start);
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
        throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，未关闭的字符串！开启index:" + start);
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
        throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，未找到字符串起始位置！关闭index:" + end);
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
        throw new ELException("解析表达式【" + String.valueOf(chars) + "】发生异常，未找到字符串起始位置！关闭index:" + end);
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

    /**
     * 用于寻找 ${ 或者 #{
     *
     * @return $ 或者 # 的index
     */
    public static int findElStart(char[] chars, int start, int end) {
        // 通过下一个花括号位置来判断是否存在 ${ 或者 #{
        int nextCurly = findChar(chars, '{', start, end);
        if (nextCurly == -1) {
            return -1;
        } else {
            if (nextCurly > start && nextCurly < end && ('$' == chars[nextCurly - 1] || '#' == chars[nextCurly - 1])) {
                return nextCurly - 1;
            } else {
                return findElStart(chars, nextCurly + 2, end);
            }
        }
    }

    /**
     * 直接找某个字符串，非token标准
     */
    public static int findChar(char[] chars, char c, int start, int end) {
        for (int i = start; i < end; i++) {
            if (c == chars[i]) {
                return i;
            }
        }
        return -1;
    }

}
