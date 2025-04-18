# Simply Expression Language

##### [📖 English Documentation](README_en.md) | 📖 中文文档

[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/)
[![GitHub release](https://img.shields.io/github/release/FanJiaRui/Simply-Expression-Language)](https://github.com/FanJiaRui/Simply-Expression-Language/releases)
[![Maven Central](https://img.shields.io/maven-central/v/net.fanjr.simplify/simplify-el?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/net.fanjr.simplify/simplify-el)
[![License](https://img.shields.io/github/license/FanJiaRui/Simply-Expression-Language?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 介绍

简易表达式，旨在解决判断、计算、取值、结构转换这些逻辑简单但编码繁复的工作<br>
主要应用场景：动态规则的计算场景、类型适配（报文转换），可以配合调度类、流程类引擎的动态计算

## 环境&依赖

* JDK 1.8
* fastjson2 2.0.56
* slf4j 1.7.30

## 特性

- 支持层级结构：快速操作、转换JSON或各类POJO等</br>
- 安全取值：告别频繁繁琐的空判断</br>
- 分支逻辑：支持复杂的复合语句，支持if else/for等相关分支语法</br>
- 方法调用：可以直接调用对象方法，支持自定义工具方法</br>
- 使用便捷：无需额外构建上下文对象进行运算，直接通过API计算原生对象</br>


## 引入配置
由于org域名国内不大好续期，后续版本均更换为net域名
#### 新版本依赖(1.1.1之后)
```xml
<dependency>
    <groupId>net.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.3.1</version>
</dependency>
```
#### 老版本依赖（1.1.0之前）
```xml
<dependency>
    <groupId>org.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.1.0</version>
</dependency>
```
## 文档
**↓↓↓↓↓**<br>
[**点这里**](docs/document.md)查看详细文档<br>
**↑↑↑↑↑**

## 功能点介绍

- 简单表达式计算、占位符计算、四则运算、正则匹配
- 支持赋值运算、类型转换，可支撑JSON格式转换等场景
- 支持复杂语句，多语句复合混用
- 支持java对象方法调用、自定义函数方法调用
- 支持条件分支、三元表达式、循环

## 表达式调用样例
``` java
// 基本调用样例
boolean result = ELExecutor.eval(
        // 表达式字符串，复合语句用;隔开，返回最后执行的语句结果
        "a==100",
        // 预期计算的上下文或者对象，主要支持javabean、Map、字符串等等
        "{'a':100}",
        // 预期返回类型，支持泛型
        boolean.class);
Assertions.assertTrue(result);

// 四则运算样例
int result = ELExecutor.eval("2 * a - (b + c) * d", "{'a':1,'b':2,'c':3,'d':4}", int.class);
// 2*1-(2+3)*4 = -18
Assertions.assertEquals(-18, result);

// 赋值&计算样例
Map<String, Object> context = new HashMap<>();
context.put("val", 10);
ELExecutor.eval("a=10;val=val+1;val++;result=a+val;", context);
// a=10;
Assertions.assertEquals(10, ElUtils.cast(context.get("a"), int.class));
// val=val+1;val++;
Assertions.assertEquals(12, ElUtils.cast(context.get("val"), int.class));
// result=a+val;
Assertions.assertEquals(22, ElUtils.cast(context.get("result"), int.class));

// 通过表达式给POJO中子对象属性赋值
TestReq req = new TestReq();
ELExecutor.eval("head.serialId='123456'", req);
System.out.println(req.getHead().getSerialId());

// 调用java方法
Map<String, Object> context = new HashMap<>();
context.put("val", "010");
// 将字符串"010"转换为int类型，调用toString()方法，再调用length()方法，预期返回值为2
Assertions.assertEquals(2, ELExecutor.eval("((int)val).toString().length()", context, int.class));

```

## 三元表达式

``` java
Map<String, Object> context = new HashMap<>();
context.put("flag", 100);
context.put("val", 10);
// flag满足大于等于100，返回val*10，这里结果是100
Assertions.assertEquals(100, ELExecutor.eval("flag>=100?val*10:val/10", context, int.class));
// flag不满足小于100，返回val/10，这里结果是1
Assertions.assertEquals(1, ELExecutor.eval("flag<100?val*10:val/10", context, int.class));
```

## 正则匹配

``` java
Assertions.assertTrue(ELExecutor.eval("18888888888 ~= '^[0-9]{11}$'", null, boolean.class));
Assertions.assertTrue(ELExecutor.eval("phone ~= '^[0-9]{11}$'", JSONObject.of("phone","18888888888"), boolean.class));
Assertions.assertFalse(ELExecutor.eval("phone ~= '^[0-9]{11}$'", JSONObject.of("phone","18888xx8888"), boolean.class));
```

## if-else分支语法

``` java
// 创建计算用的上下文
Map<String, Object> context = new HashMap<>();

// 分支1(IF)  a=1
context.put("flag", 1);
ELExecutor.eval("if(flag==1){\n a=1;\n }", context);
Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 1);
context.clear();

// 分支2(IF-ELSE-IF) a=3
context.put("flag", 2);
ELExecutor.eval("if(flag==1){a=1; }else if(flag==2){ a=2;a++; }", context);
Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 3);
context.clear();

// 分支3(IF-ELSE-IF-ELSE) a=0
context.put("flag", 3);
ELExecutor.eval("if(flag==1){\n a=1;\n }else if(flag==2){ a=2;a++; }else{ a=0; }", context);
Assertions.assertEquals(ElUtils.cast(context.get("a"), int.class), 0);
context.clear();
```

## for循环语法

``` java
// 对照组：java代码循环
int[] arr = new int[10];
for (int i = 0; i < 10; i++) {
    arr[i] = 3 * (i + 1234) - i;
}

// 创建计算用的上下文
Map<String, Object> context = new HashMap<>();

// 类C模式
ELExecutor.eval("for(i=0;i<10;i++){arr[i]=3 * (i + 1234) - i}", context);
// 计算结果正确性断言
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));
context.clear();

// 迭代器模式，可以遍历数组、List、Map等等
context.put("num", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
ELExecutor.eval("for(i:num){arr[i]=3 * (i + 1234) - i}", context);
// 计算结果正确性断言
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));
context.clear();

// 数字模式 等价于i=0;i<10;i++
ELExecutor.eval("for(i:10){arr[i]=3 * (i + 1234) - i}", context);
// 计算结果正确性断言
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));
context.clear();

// 嵌套循环将结果输出到控制台，输出九九乘法表
ELExecutor.eval("   " +
        "for(i=1;i<=9;i++){" +
        "   for(j=1;j<=9;j++){" +
        "       $.printf(i+'*'+j+'='+(i*j)+'\t');" +
        "   }" +
        "   $.printf('\n')" +
        "}" , 
        new HashMap<>());
```

## 自定义函数&内置函数

### 自定义函数扩展

```properties
# 在META-INF目录下新建simplify-el.functions，添加Function挂载
# 工具名=类全限定名，多个类用','隔开，会将这些类中所有public static方法收集并挂载到工具名下
# 同一个工具名下方法名+参数表相同的方法根据@ELMethod中的order属性进行排序生效，order数字小的优先级高
$=net.fanjr.simplify.el.InnerFunctions
```

以下为`InnerFunctions`内容，表达式中通过工具名+方法名对其调用。<br>
例如:`$.println('hello world')`

```java
public class InnerFunctions {
    // 省略了一些不重要的内容

    /**
     * 控制台输出
     * $.println(xxxxx)
     */
    @ELMethod(order = Integer.MAX_VALUE)
    public static void println(Object object) {
        System.out.println(object);
    }
}
```

### 内置函数

- `$.println(xxxx)` 将传入对象输出到控制台
- `$.printf(xxxx)` 将传入对象输出到控制台(结尾不换行)
- `$.log(xxxx)` 将传入对象用日志框架输出到控制台和日志文件
- `$.max(a,b)` 比较a,b的大小，返回更大的
- `$.min(a,b)` 比较a,b的大小，返回更小的
- `$.merge([map1,map2,map3...])` 深度合并多个map
- `$.getEnumMap(xx)` 获取枚举映射MAP

## 后续规划

- 分析JAVA自带库方法，考虑部分可能经常使用的方法集成到内置函数中
- 持续优化使用方式，考虑添加部分工具类