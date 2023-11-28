package unit;

import com.alibaba.fastjson2.JSONObject;
import org.fanjr.simplify.el.ELExecutor;
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
        Assertions.assertEquals(ELTokenUtils.findHeadSpace("12 11  sad".toCharArray(), 0, 13), 0);
        Assertions.assertEquals(ELTokenUtils.findEndSpace("  12 11  sad ".toCharArray(), 0, 13), 1);
        Assertions.assertEquals(ELTokenUtils.findEndSpace("  12 11   sad".toCharArray(), 0, 13), 0);
    }

    @Test
    public void ifElseTest() {
        Assertions.assertEquals(ELExecutor.eval("flag=1;if ( flag==1 ) { a=1 } else if(flag==2){a=2}else{a=0}x=a;++x;", JSONObject.of(), int.class), 2);
        Assertions.assertEquals(ELExecutor.eval("flag=2;if ( flag==1 ) { a=1 } else if(flag==2){a=2}else{a=0}x=a;++x;", JSONObject.of(), int.class), 3);
        Assertions.assertEquals(ELExecutor.eval("flag=3;if ( flag==1 ) { a=1 } else if(flag==2){a=2}else{a=0}x=a;++x+;", JSONObject.of(), int.class), 1);
    }

    @Test
    public void forTest() {
        Assertions.assertEquals(ELExecutor.eval("list=[1,2,3];for ( i:list ) { a+=i } a;", JSONObject.of(), int.class), 6);
        Assertions.assertEquals(ELExecutor.eval("list=['1','2'];for ( i:list ) { a=a+i } a;", JSONObject.of(), String.class), "12");
        Assertions.assertEquals(ELExecutor.eval("for ( i:[1,2,3,4]) { a+=i;if(i==2){break;} } a;", JSONObject.of(), int.class), 3);
        Assertions.assertEquals(ELExecutor.eval("for ( i:[1,2,3,4]) { a+=i;if(i==2){break;} a++; } a;", JSONObject.of(), int.class), 4);
        Assertions.assertEquals(ELExecutor.eval("for (i:3) { a=a+(String)i} a;", JSONObject.of(), String.class), "012");
    }

}
