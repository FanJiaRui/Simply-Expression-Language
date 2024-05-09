package unit;

import com.alibaba.fastjson2.JSONObject;
import org.fanjr.simplify.el.ELExecutor;
import org.fanjr.simplify.el.ELTokenUtils;
import org.fanjr.simplify.el.cache.ConcurrentCache;
import org.fanjr.simplify.el.cache.ELCacheManager;
import org.fanjr.simplify.utils.ElUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;

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
        Map<String, Object> context = new HashMap<>();

        context.put("flag", 100);
        context.put("val", 10);
        Assertions.assertEquals(100, ELExecutor.eval("flag>=100?val*10:val/10", context, int.class));
        Assertions.assertEquals(1, ELExecutor.eval("flag<100?val*10:val/10", context, int.class));
        context.clear();

        context.put("flag", 1);
        ELExecutor.eval("if(flag==1){\n a=1;\n }else if(flag==2){ a=2;a++; }else{ a=0; }", context);
        Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 1);
        context.clear();

        context.put("flag", 2);
        ELExecutor.eval("if(flag==1){\n a=1;\n }else if(flag==2){ a=2;a++; }else{ a=0; }", context);
        Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 3);
        context.clear();

        context.put("flag", 3);
        ELExecutor.eval("if(flag==1){\n a=1;\n }else if(flag==2){ a=2;a++; }else{ a=0; }", context);
        Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 0);
        context.clear();
    }

    @Test
    public void forTest() {
        Assertions.assertEquals(ELExecutor.eval("list=[1,2,3];for ( i:list ) { a+=i } a;", JSONObject.of(), int.class), 6);
        Assertions.assertEquals(ELExecutor.eval("list=['1','2'];for ( i:list ) { a=a+i } a;", JSONObject.of(), String.class), "12");
        Assertions.assertEquals(ELExecutor.eval("for ( i:[1,2,3,4]) { a+=i;if(i==2){break;} } a;", JSONObject.of(), int.class), 3);
        Assertions.assertEquals(ELExecutor.eval("for ( i:[1,2,3,4]) { a+=i;if(i==2){break;} a++; } a;", JSONObject.of(), int.class), 4);
        Assertions.assertEquals(ELExecutor.eval("for (i:3) { a=a+(String)i} a;", JSONObject.of(), String.class), "012");
    }


    @Test
    public void testCache() throws InterruptedException {
        ConcurrentCache<String, String> pool = ELCacheManager.getPool(String.class, 1000);
        String tmp = "T1+0";
        CountDownLatch latch = new CountDownLatch(3);
        pool.put(tmp,"0");
        new Thread(() -> {
            for (int i = 1; i < 10000; i++) {
//                pool.put("T1+" + i, String.valueOf(i));
                final int x = i;
                pool.computeIfAbsent("T1+" + i, (k) -> String.valueOf(x));
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            for (int i = 1; i < 10000; i++) {
                final int x = i;
//                pool.put("T1+" + i, String.valueOf(i));
                pool.computeIfAbsent("T1+" + i, (k) -> String.valueOf(x));
            }
            latch.countDown();
        }).start();
        new Thread(() -> {
            for (int i = 1; i < 10000; i++) {
                final int x = i;
//                pool.put("T1+" + i, String.valueOf(i));
                pool.computeIfAbsent("T1+" + i, (k) -> String.valueOf(x));
            }
            latch.countDown();
        }).start();

        latch.await();
        System.out.println(pool.get("T1+0"));
        System.out.println(pool.get("T1+999"));
        System.out.println(pool.get("T2+0"));
        System.out.println(pool.get("T2+999"));
    }


    //    @Test
    public void drawLots() {
        Random rand = new Random();
        LinkedList<String> queue = new LinkedList<>();
        List<String> target = new ArrayList<>();

        //添加名单

        // 随机取出人员放置到目标
        while (queue.size() > 0) {
            int nextIndex = rand.nextInt(queue.size());
            String name = queue.remove(nextIndex);
            target.add(name);
        }

        // 输出结果
        System.out.println("优先级:");
        for (int i = 0; i < target.size(); i++) {
            System.out.println((i + 1) + ": " + target.get(i));
        }
    }

}
