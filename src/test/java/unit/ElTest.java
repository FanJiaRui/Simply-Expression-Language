package unit;

import com.alibaba.fastjson2.JSONObject;
import org.fanjr.simplify.el.EL;
import org.fanjr.simplify.el.ELExecutor;
import org.fanjr.simplify.el.invoker.node.Node;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

enum TestEnum {
    SERIAL_ID("SERIAL_ID", "流水号"),
    ;

    public final String NODE_NAME;
    public final String TITLE;

    TestEnum(String nodeName, String title) {
        this.NODE_NAME = nodeName;
        this.TITLE = title;
    }
}

/**
 * el表达式工具测试类
 *
 * @author fanjr@vip.qq.com
 * @date 2022年6月6日
 */
@DisplayName("EL测试")
public class ElTest {

    @Test
    @DisplayName("基础测试")
    public void baseTest() {
        int num = 0;
        Assertions.assertEquals(num-- + ++num, ELExecutor.eval("num=0;num-- + ++num", new HashMap<>(), int.class));

        EL el1 = ELExecutor.compile("!(!(boolean) \"true\")?(\"22\"):(23)");
        Assertions.assertEquals("22", el1.invoke("!(!(boolean) \"true\")?(\"22\"):(23)", String.class));
        Assertions.assertTrue(ELExecutor.eval("(''!=\"\")==false", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("('\"'!='\"')==false", null, boolean.class));
        Assertions.assertEquals("34", ELExecutor.eval("!(\"23\"!=(int)\"23\")?34:(\"23\")", null, String.class));
        Assertions.assertEquals("2", ELExecutor.eval("a=1;a++;a", new HashMap<>(), String.class));
        Assertions.assertEquals("1", ELExecutor.eval("i=1;arr[i++]=i", new HashMap<>(), String.class));
        Assertions.assertEquals("[0,1,2]", ELExecutor.eval("i=0;[i++,i++,i++]", new HashMap<>(), String.class));
        Assertions.assertFalse(ELExecutor.eval("a=1;true&&1+a*2<2", new HashMap<>(), boolean.class));
        Assertions.assertEquals("{\"c\":1,\"b\":1,\"a\":1}", ELExecutor.eval("map.a=map.b=map.c=1;map", new HashMap<>(), String.class));
        Assertions.assertFalse(ELExecutor.eval("\"true\"!='true'", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("false&&true==false", null, boolean.class));
        Assertions.assertEquals("{\"0\":1,\"str\":2,\"3\":\"str2\"}", ELExecutor.eval("i=0;{i++:i++,'str':i++,i++:\"str2\"}", new HashMap<>(), String.class));
        Assertions.assertTrue(ELExecutor.eval("(1!='1')==false", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("123456==\"123456\"", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("9==(123456789).toString().length()", null, boolean.class));
        Assertions.assertEquals("[true,{\"1.2\":2.55,\"true\":{\"1\":true,\"2\":[0,1,2,3]}},\" false!=(1==1)\",213]", ELExecutor.eval("[ false!=(1 == 1) ,{1.2 : 2.55, false!=(1 == 1):{1:false!=(1 == 1),2:[0,1,2,3]},2:this}, \" false!=(1==1)\" , 213]", null, String.class));
        Assertions.assertEquals("333", ELExecutor.eval("a=(true;false)?123456:33;a+300", new HashMap<>(), String.class));
        Assertions.assertEquals("123456", ELExecutor.eval("true?123456:33", null, String.class));
        Assertions.assertEquals(BigDecimal.class, ELExecutor.eval("str=\"123456\";dec=(BigDecimal)str;dec.getClass()", new HashMap<>(), Object.class));

        // null\empty\blank等关键字
        Assertions.assertTrue(ELExecutor.eval("null==empty", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("\"\"==empty", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("\" \"==empty", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("\" \"!=empty", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("\"\"==blank", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("\"  \"==blank", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("'  '==blank", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("null==blank", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("empty==blank", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("blank==empty", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("empty==empty", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("blank==blank", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("!(\"\"==empty)?true:(false?true:false)", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("!(''==empty)?true:(false?true:false)", null, boolean.class));

        // 常量计算
        Assertions.assertTrue(ELExecutor.eval("true!=false", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("true==true", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("false==false", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("(false!=(1==1))", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("-1>0", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("-1>=0", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("0<-1", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("0<=-1+1", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("-1+1>=0", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("-1+1>0", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("0==-1+1", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("0<-1+1", null, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("'[0,  1,2]'==[0,1,2]", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("[0,  1,2]==[0,1,2]", null, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("this==[0,1,2]", new byte[]{0, 1, 2}, boolean.class));

        Assertions.assertEquals("6", ELExecutor.eval("(1==1?'123456':33).length()", null, String.class));
        Assertions.assertEquals("2", ELExecutor.eval("(1==1?\"123456\":33).charAt(1)", null, String.class));
        Assertions.assertEquals("456", ELExecutor.eval("(1==1?\"123456\":33).substring(2).substring(\"1\")", null, String.class));
        Assertions.assertEquals("23", ELExecutor.eval("(1==1?\"123456\":33).substring(\"1\",3)", null, String.class));
        Assertions.assertEquals("{\"2\":3,\"4\":5", ELExecutor.eval("{2:3,4:5,6:7}.toString().substring(\"0\",12)", null, String.class));

        JSONObject context = new JSONObject();
        Assertions.assertEquals("1234567890", ELExecutor.eval("a.b.c.d=(String)1234567890", context, String.class));
        Assertions.assertEquals("t1", ELExecutor.eval("a.b.c.d=='1234567890'?'t1':'t2'", context, String.class));
        Assertions.assertEquals("t1", ELExecutor.eval("a.b.c.d=='1234567890'?\"t1\":\"t2\"", context, String.class));
        Assertions.assertEquals("true", ELExecutor.eval("a.b.c.boo=true", context, String.class));
        Assertions.assertEquals("true", ELExecutor.eval("a.b.c.arr[3][1]=true", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("a.index=1", context, String.class));
        Assertions.assertEquals("{\"d\":\"", ELExecutor.eval("a.b.c.toString().substring(\"0\",6)", context, String.class));
        Assertions.assertEquals("{\"c\":{\"d\":\"1234567890\",\"boo\":true,\"arr\":[null,null,null,[null,true]]}}", ELExecutor.eval("a.b", context, String.class));
        Assertions.assertFalse(ELExecutor.eval("str1=\"123456\";strClass=(class)\"org.fanjr.simplify.utils.ElUtils\";strClass.isBlank(str1)", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("a.b.c.boo?true:false", context, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("!a.b.c.boo?true:false", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("\"1234567890\"==(((a).b).c).d", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("1234567890==(a.b.c).d", context, boolean.class));
        Assertions.assertNull(ELExecutor.eval("a.b.c.arr[2]", context, Object.class));
        Assertions.assertEquals("[null,true]", ELExecutor.eval("a.b.c.arr[3]", context, String.class));
        Assertions.assertEquals("rue", ELExecutor.eval("((String)a.b.c.arr[3][a.index]).substring(1)", context, String.class));
        Assertions.assertEquals("10012345678901", ELExecutor.eval("((String)1100).substring(1) + (a.b.c.d + 1)", context, String.class));
        Assertions.assertEquals("10012345678901", ELExecutor.eval("(((String)1100).substring(1) + a.b.c.d) + 1", context, String.class));
        Assertions.assertEquals("6", ELExecutor.eval("a.index = (a.index + 5)", context, String.class));
        Assertions.assertEquals("11", ELExecutor.eval("+ a.index += 5", context, String.class));
        Assertions.assertEquals("11", ELExecutor.eval("a.index2[a.index] = a.index", context, String.class));
        Assertions.assertEquals("6", ELExecutor.eval("a.index = (a.index - 5)", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("a.index -= 5", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("a.index2[this.a.index] = a.index", context, String.class));
        Assertions.assertEquals("-2", ELExecutor.eval("a.index = (a.index * (-2))", context, String.class));
        Assertions.assertEquals("4", ELExecutor.eval("a.index *= \"-2\"", context, String.class));
        Assertions.assertEquals("-4", ELExecutor.eval("-a.index", context, String.class));
        Assertions.assertEquals("4", ELExecutor.eval("a.index2[a.index] = a.index", context, String.class));
        Assertions.assertEquals("[null,1,null,null,4,null,null,null,null,null,null,11]", ELExecutor.eval("a.index2", context, String.class));
        Assertions.assertTrue(ELExecutor.eval("true||false", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("false||true", context, boolean.class));
        Assertions.assertFalse(ELExecutor.eval("true&&false", context, boolean.class));
        System.out.println(ELExecutor.eval("9%2", context, String.class));
        System.out.println(ELExecutor.eval("\"33.3333333333333333333\"-33", context, String.class));
        System.out.println(ELExecutor.eval("33.3333333333333333333-33", context, String.class));
        System.out.println(ELExecutor.eval("1/3", context, String.class));
        System.out.println(ELExecutor.eval("测试除法：-100 / 3 = ${-100 / 3}", context, String.class));

        System.out.println(ELExecutor.eval("a.i=0", context, String.class));
        System.out.println(ELExecutor.eval("this.array[a.i++]=a.i", context, String.class));
        System.out.println(ELExecutor.eval("array[this.a.i++]=a.i", context, String.class));
        System.out.println(ELExecutor.eval("array[a.i++]=a.i", context, String.class));
        System.out.println(ELExecutor.eval("this.array[a.i--]=-a.i", context, String.class));
        System.out.println(ELExecutor.eval("this.array[a.i--]=-a.i", context, String.class));
        System.out.println(ELExecutor.eval("this.array[a.i--]=-a.i", context, String.class));
        System.out.println(ELExecutor.eval("this.array", context, String.class));

        Assertions.assertTrue(ELExecutor.eval("num=0;num++==0", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num==1", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num=0;++num==1", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num==1", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num=0;num++ + ++num == 2", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num=0;num-- + --num == -2", context, boolean.class));
        Assertions.assertTrue(ELExecutor.eval("num=0;num-- + ++num == 0", context, boolean.class));

        Assertions.assertEquals("4", (ELExecutor.eval("this.num=1;num++;num*=3;num-=2;", context, String.class)));
        Assertions.assertEquals("4", ELExecutor.eval("num", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("num=1;", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("${num}", context, String.class));
        Assertions.assertEquals("1", ELExecutor.eval("#{num}", context, String.class));
        Assertions.assertEquals("1-1x11", ELExecutor.eval("#{num}${-num}x#{num}${num++}", context, String.class));
        Assertions.assertEquals("2xxx", ELExecutor.eval("${num}xxx", context, String.class));
        Assertions.assertEquals("2xxx", ELExecutor.eval("#{num}xxx", context, String.class));
        Assertions.assertEquals("xxx2", ELExecutor.eval("xxx${num}", context, String.class));
        Assertions.assertEquals("xxx2", ELExecutor.eval("xxx#{num}", context, String.class));
        Assertions.assertEquals("xxx2xx", ELExecutor.eval("xxx${num}xx", context, String.class));
        Assertions.assertEquals("xxx2xx", ELExecutor.eval("xxx#{num}xx", context, String.class));
        Assertions.assertEquals("true", ELExecutor.eval("(4/2).floatValue()==2", context, String.class));
        Assertions.assertEquals("{\"b\":2,\"a\":1}", ELExecutor.eval("map=new com.alibaba.fastjson2.JSONObject();map.b=2;map.a=1;map", context, String.class));
        System.out.println(ELExecutor.eval("this", context, String.class));

        ELExecutor.putNode(context, "pojo", new TestPojo());
        ELExecutor.eval("pojo.abc=123", context);
        ELExecutor.eval("p.pojo=pojo", context);
        Assertions.assertEquals("123456", ELExecutor.eval("pojo.abc=123456;pojo.abc", context, String.class));
        Assertions.assertEquals("123", ELExecutor.eval("p.pojo.abc=123;pojo.abc", context, String.class));
        Assertions.assertEquals("1234", ELExecutor.eval("pojo.abc=pojo.abc=1234;p.pojo.abc", context, String.class));

        ELExecutor.putNode(context, "arr", new String[]{"1", null, "3"});
        Assertions.assertEquals("3", ELExecutor.eval("arr.size()", context, String.class));
    }

    @Test
    @Disabled
    @DisplayName("单次测试")
    public void sigTest() {
        // 这里用于临时测试，把测试用例追加到基础测试中
        TestReq req = new TestReq();
        ELExecutor.eval("head.serialId=123456", req);
        ELExecutor.putNode(req, "head.date", "20230330");
        System.out.println(req);

        System.out.println(ELExecutor.eval("head.serialId", req));
        System.out.println(ELExecutor.eval("head.date", req));
        System.out.println(ELExecutor.eval("body.data", req));

        System.out.println(req.getHead().getSerialId());
        System.out.println(req.getHead().getDate());
        try{
            // 这里会抛出空指针异常
            System.out.println(req.getBody().getData());
        }catch (Exception e){

        }
    }

    /**
     * 速度测试
     */
    @Test
    @DisplayName("运行效率测试")
    public void speedTest() {
        Assertions.assertEquals("3704", ELExecutor.eval("3 * (this + 1234) - this", 1, String.class));
        long start1 = System.currentTimeMillis();
        int[] arr1 = new int[1000000];
        int[] arr2 = new int[1000000];
        for (int i = 0; i < 1000000; i++) {
            arr1[i] = 3 * (i + 1234) - i;
        }
        long end1 = System.currentTimeMillis();
        System.out.println(end1 - start1);

        long start2 = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            arr2[i] = ELExecutor.eval("3 * (this + 1234) - this", i, int.class);
        }
        long end2 = System.currentTimeMillis();
        System.out.println(end2 - start2);

        //计算结果正确性断言
        Assertions.assertArrayEquals(arr1, arr2);
    }

    /**
     * 测试枚举取值
     */
    @Test
    @DisplayName("枚举测试")
    public void testEnum() {
        JSONObject context = new JSONObject();
        Assertions.assertEquals("SERIAL_ID", ELExecutor.eval("((class) \"unit.TestEnum\").SERIAL_ID.NODE_NAME", context, Object.class));
        Assertions.assertEquals("流水号", ELExecutor.eval("((class) \"unit.TestEnum\").SERIAL_ID.TITLE", context, Object.class));
    }

    @Test
    @DisplayName("节点测试")
    public void testNode() {
        JSONObject ctx = new JSONObject();
        Node a = ELExecutor.compileNode("a.b.c.d");
        Node b = ELExecutor.compileNode("a.b.c.d2");
        Node c = ELExecutor.compileNode("a.b.c");
        a.putNode(ctx, "testStr1");
        b.putNode(ctx, "testStr2");
        Assertions.assertEquals(a.getNode(ctx), "testStr1");
        Assertions.assertEquals(b.getNode(ctx), "testStr2");
        Assertions.assertEquals(c.getNode(ctx).toString(), "{\"d\":\"testStr1\",\"d2\":\"testStr2\"}");
        c.removeNode(ctx);
        Assertions.assertEquals(ctx.toString(), "{\"a\":{\"b\":{}}}");

        Node arr = ELExecutor.compileNode("a.b.arr[1]");
        Node ov = ELExecutor.compileNode("a.b.arr.x");
        arr.putNode(ctx, "1");
        ov.putNode(ctx, "1");
        Assertions.assertEquals(ov.getNode(ctx), "1");
        arr.putNode(ctx, "2");
        Assertions.assertEquals(arr.getNode(ctx), "2");
        Assertions.assertEquals(ctx.toString(), "{\"a\":{\"b\":{\"arr\":[{\"x\":\"1\"},\"2\"]}}}");
    }

    @Test
    @DisplayName("自定义函数测试")
    public void testFunction() {
        ELExecutor.eval("MyUtils.noReturnNoParamFun()", null);
        Assertions.assertEquals("5", ELExecutor.eval("(String)MyUtils.strReturnNoParamFun().length()", null));
        Assertions.assertEquals("1", ELExecutor.eval("(String)((String)MyUtils.strReturnNoParamFun().length()).length()", null));
        Assertions.assertEquals("9", ELExecutor.eval("(String)MyUtils.strReturnOneParamFun('xxx').length()", null));
        Assertions.assertEquals("hello 100", ELExecutor.eval("(String)MyUtils.strReturnOneParamFun(a)", "{'a':100}"));
        Assertions.assertEquals("99", ELExecutor.eval("(String)(MyUtils.strReturnOneParamFun(a).substring(6) - 1)", "{'a':100}"));
    }


}
