# Simply Expression Language

## 介绍

简易表达式，旨在解决判断、计算、取值、结构转换这些逻辑简单但编码繁复的工作<br>
主要应用场景：动态规则的计算场景、类型适配（报文转换）

## 环境&依赖

JDK 1.8 </br>
fastjson2 2.0.33</br>
slf4j 1.7.30

## 特性

- 支持层级结构：快速操作、转换JSON或各类POJO等</br>
- 安全取值：告别频繁繁琐的空判断</br>
- 直接方法调用：可以直接调用对象方法，支持自定义工具方法</br>
- 使用便捷：无需额外构建上下文对象进行运算，直接通过API计算原生对象</br>

## 引入配置

```xml

<dependency>
    <groupId>org.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.0.6</version>
</dependency>
```

## 文档

[**点这里**](docs/document.md)查看详细文档

## 功能点介绍

- 简单表达式计算、占位符计算
- 三元表达式、二元运算符、一元运算符
- 赋值运算
- 优先计算、子串表达式
- java对象方法调用、自定义函数方法调用

## 常用API-取值&计算

``` java
// 从上下文对象context中取head中的serialId属性
ELExecutor.eval("head.serialId", context);

// 从上下文中取a、b、c、d并进行四则运算
ELExecutor.eval("2 * a - (b + c) * d", context);

// 支持直接调用java方法
ELExecutor.eval("head.serialId.length()", context);

// 通过占位符进行拼接字符串(也支持用运算符'+'拼接)
ELExecutor.eval("${head.serialId}XXXX${head.userName}", context);

// 其中上下文context支持多种形式
// 计算用的上下文支持Map(及其子类)，例如fastjson提供的JSONObject
Object context = new JSONObject();
// 或者使用POJO
Object context = new TestReq();
// 或者使用JSON字符串
Object context = "{'head':{'serialId':'TESTID'}}}";
// 若不需要使用上下文变量也可以使用null
Object context = null;
```

## 常用API-赋值

``` java
// 通过表达式给POJO中子对象属性赋值
TestReq req = new TestReq();
ELExecutor.eval("head.serialId=123456", req);
System.out.println(req.getHead().getSerialId());

// 等价于下面的写法
TestReq req = new TestReq();
if (req.getHead()==null){
    req.setHead(new TestReqHead());
}
req.getHead().setSerialId("123456");
System.out.println(req.getHead().getSerialId());
```