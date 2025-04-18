package net.fanjr.simplify.el;

import net.fanjr.simplify.utils.SimplifyException;

/**
 * EL表达式执行相关异常
 * @author fanjr@vip.qq.com
 */
public class ELException extends SimplifyException {

    public ELException() {
        super();
    }

    public ELException(String message) {
        super(message);
    }

    public ELException(String message, Throwable cause) {
        super(message, cause);
    }

}
