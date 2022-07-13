package org.fanjr.simplify.el;

/**
 * @author fanjr@vip.qq.com
 * @since 2022/5/20 下午4:17
 */
public class ElException extends RuntimeException {
    public ElException() {
        super();
    }

    public ElException(String message) {
        super(message);
    }

    public ElException(String message, Throwable cause) {
        super(message, cause);
    }
}
