package unit;

import com.alibaba.fastjson2.JSONFactory;
import com.alibaba.fastjson2.reader.ObjectReaderProvider;
import org.fanjr.simplify.el.ElException;

import java.util.function.Function;

/**
 * @author fanjr@vip.qq.com
 * @file TestFastJson.java
 * @since 2022/7/1 上午11:01
 */
public class TestFastJson {

    public static void main(String[] args) {
        boolean b;
        Object targetObj = "true";
        ObjectReaderProvider provider = JSONFactory.getDefaultObjectReaderProvider();
        Function typeConvert = provider.getTypeConvert(targetObj.getClass(), boolean.class);
        if (typeConvert != null) {
            b = (boolean) typeConvert.apply(targetObj);
        } else {
            throw new ElException("无法将类型" + targetObj.getClass() + "转换为boolean类型");
        }
        System.out.println(b);

    }
}
