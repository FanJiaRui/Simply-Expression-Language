package org.fanjr.simplify.context;

/**
 * @author fanjr@vip.qq.com
 * @file ContextException.java
 * @since 2022/5/20 下午4:17
 */
public class ContextException extends RuntimeException {
    public ContextException() {
        super();
    }

    public ContextException(String message) {
        super(message);
    }

    public ContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
