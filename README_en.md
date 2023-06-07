# Simply Expression Language
##### 📖 English Documentation | [📖 中文文档](README.md)
## Introduction

Simple expression is designed to solve the judgment, calculation, value, structure conversion, which is simple in logic but complicated <br> in coding
Main application scenarios: dynamic rule calculation scenario and type adaptation (message conversion)<br>
_**The English document comes from the translation software, please let me know if there is any problem or you can't understand it.**_
## Environment & Dependencies

* JDK 1.8
* fastjson2 2.0.33
* slf4j 1.7.30

## Characteristic

- Support hierarchical structure: quick operation, conversion of JSON or various POJOs, etc.</br>
- Safe Value: Say Goodbye to Frequent and Complicated NULL Judgment</br>
- Direct method call: object methods can be called directly, and custom tool methods are supported</br>
- Easy to use: native objects are computed directly through the API without the need to build additional context objects for computation</br>

## Introduce the configuration


```xml

<dependency>
    <groupId>org.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.0.6</version>
</dependency>
```

## Documentation

[**Click Here**](docs/document_en.md) View detailed documentation

## Function point introduction

- Imple expression calculation, placeholder calculation,
- Ternary expression, binary operator, unary operator
- Assignment operation
- Precedence calculation, substring expression,
- Method call of Java object and user-defined function

## Common API-Value & Calculation


``` java
// Gets the serialId attribute in head from the context object
ELExecutor.eval("head.serialId", context);

// Take a, b, c, d from the context and perform four operations
ELExecutor.eval("2 * a - (b + c) * d", context);

// Supports direct invocation of java methods
ELExecutor.eval("head.serialId.length()", context);

// Concatenation of strings by placeholders (concatenation with operator '+' is also supported)
ELExecutor.eval("${head.serialId}XXXX${head.userName}", context);

// The context supports several forms
// Context supports Map(and its subclasses), such as JSONObject provided by fastjson
Object context = new JSONObject();
// Or use a POJO
Object context = new TestReq();
// Or use a JSON string
Object context = "{'head':{'serialId':'TESTID'}}}";
// You can also use null if you don't need a context variable
Object context = null;
```

## Common API-Assignment


``` java
// Assign values to subobject properties in POjos via expressions
TestReq req = new TestReq();
ELExecutor.eval("head.serialId=123456", req);
System.out.println(req.getHead().getSerialId());

// Is equivalent to the following
TestReq req = new TestReq();
if (req.getHead()==null){
    req.setHead(new TestReqHead());
}
req.getHead().setSerialId("123456");
System.out.println(req.getHead().getSerialId());
```