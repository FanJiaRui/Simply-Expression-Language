package net.fanjr.simplify.utils;

/**
 * @author fanjr@vip.qq.com
 * @since 2022/5/20 下午4:17
 */
public class SimplifyException extends RuntimeException {
    public SimplifyException() {
        super();
    }

    public SimplifyException(String message) {
        super(message);
    }

    public SimplifyException(String message, Throwable cause) {
        super(message, cause);
    }
}
