package unit;

import org.fanjr.simplify.el.ELTokenUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("内部方法单元测试")
public class UnitTest {

    @Test
    public void spaceTest() {
        Assertions.assertEquals(ELTokenUtils.findHeadSpace("  12  11  sad".toCharArray(), 0, 13), 2);
        Assertions.assertEquals(ELTokenUtils.findHeadSpace("   12 11  sad".toCharArray(), 0, 13), 3);
        Assertions.assertEquals(ELTokenUtils.findEndSpace("  12 11  sad ".toCharArray(), 0, 13), 1);
        Assertions.assertEquals(ELTokenUtils.findEndSpace("  12 11   sad".toCharArray(), 0, 13), 0);
    }

}
