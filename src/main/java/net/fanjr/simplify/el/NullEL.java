package net.fanjr.simplify.el;

/**
 * 空实现，容错处理
 *
 * @author fanjr@vip.qq.com
 * @since 2021/6/28 下午4:08
 */
public class NullEL implements EL {

    public final static NullEL INSTANCE = new NullEL();

    @Override
    public Object invoke(Object ctx) {
        return null;
    }

    @Override
    public void accept(ELVisitor visitor) {
        // 访问自身
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "[EL: null]";
    }
}
