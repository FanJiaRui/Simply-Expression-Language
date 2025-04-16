package net.fanjr.simplify.el.invoker.node;

import net.fanjr.simplify.el.ELVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author fanjr@vip.qq.com
 * @since 2021/7/6 下午4:28
 */
public class NullNodeInvoker extends NodeInvoker implements Node {

    public static final NullNodeInvoker INSTANCE = new NullNodeInvoker();
    private static final Logger logger = LoggerFactory.getLogger(NullNodeInvoker.class);

    protected NullNodeInvoker() {
        super("null");
    }

    @Override
    public Object getNode(Object ctx) {
        return null;
    }

    @Override
    public void putNode(Object ctx, Object value) {
        logger.info("无法将值【{}】放入无效节点", value);
    }

    @Override
    public void removeNode(Object ctx) {
        logger.info("无效节点，跳过移除操作!");
    }

    @Override
    public boolean isVariable() {
        // null是常量
        return false;
    }

    @Override
    void setValueByParent(NodeHolder parentNode, Object value, int index) {

    }

    @Override
    Object getValueByParent(Object ctx, NodeHolder parentNode) {
        return null;
    }

    @Override
    void removeValueByParent(NodeHolder parentNode, int index) {

    }

    @Override
    protected void acceptChild(ELVisitor visitor) {

    }

}
