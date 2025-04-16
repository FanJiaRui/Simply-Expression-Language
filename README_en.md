# Simply Expression Language

##### [📖 English Documentation](README_en.md) | 📖 Chinese Documentation

[![Java support](https://img.shields.io/badge/Java-8+-green?logo=java&logoColor=white)](https://openjdk.java.net/) [![GitHub release](https://img.shields.io/github/release/FanJiaRui/Simply-Expression-Language)](https://github.com/FanJiaRui/Simply-Expression-Language/releases) [![Maven Central](https://img.shields.io/maven-central/v/net.fanjr.simplify/simplify-el?logo=apache-maven&logoColor=white)](https://search.maven.org/artifact/net.fanjr.simplify/simplify-el) [![License](https://img.shields.io/github/license/FanJiaRui/Simply-Expression-Language?color=4D7A97&logo=apache)](https://www.apache.org/licenses/LICENSE-2.0.html)

## Introduction

Simply Expression Language (SEL) aims to simplify repetitive coding tasks involving judgment, computation, value
extraction, and structural transformation.  
Primary application scenarios: Dynamic rule evaluation, type adaptation (message conversion), and dynamic computation
with scheduling/process engines.

## Environment & Dependencies

• JDK 1.8+
• fastjson2 2.0.56+
• slf4j 1.7.30+

## Features

• **Hierarchical Structure Support**: Easily manipulate/transform JSON, POJOs, etc.  
• **Null-safe Access**: Eliminate tedious null checks  
• **Branch Logic**: Supports complex compound statements, if-else/for loops  
• **Method Invocation**: Directly call object methods and custom utility functions  
• **Ease of Use**: Compute native objects directly via API without building context objects

## Dependency Configuration

Due to domain renewal issues, new versions use the `net` domain.

#### New Versions (Post 1.1.1)

```xml

<dependency>
    <groupId>net.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.3.1</version>
</dependency>
```

#### Legacy Versions (Pre 1.1.0)

```xml

<dependency>
    <groupId>org.fanjr.simplify</groupId>
    <artifactId>simplify-el</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Documentation

**↓↓↓↓↓**  
[**Click Here**](docs/document.md) for detailed documentation  
**↑↑↑↑↑**

## Key Capabilities

• Basic expression evaluation, placeholder substitution, arithmetic operations, regex matching
• Assignment operations, type casting (supports JSON conversion scenarios)
• Complex statement composition
• Java method invocation and custom functions
• Conditional branching, ternary expressions, loops

## Expression Examples

``` java
// Basic example
boolean result = ELExecutor.eval(
        "a==100",
        "{'a':100}",
        boolean.class);
Assertions.assertTrue(result);

// Arithmetic operations
int result = ELExecutor.eval("2 * a - (b + c) * d", "{'a':1,'b':2,'c':3,'d':4}", int.class);
// 2*1-(2+3)*4 = -18
Assertions.assertEquals(-18, result);

// Assignment & computation
Map<String, Object> context = new HashMap<>();
context.put("val", 10);
ELExecutor.eval("a=10;val=val+1;val++;result=a+val;", context);
Assertions.assertEquals(10, ElUtils.cast(context.get("a"), int.class));
Assertions.assertEquals(12, ElUtils.cast(context.get("val"), int.class));
Assertions.assertEquals(22, ElUtils.cast(context.get("result"), int.class));

// Assign POJO property via expression
TestReq req = new TestReq();
ELExecutor.eval("head.serialId='123456'", req);
System.out.println(req.getHead().getSerialId());

// Call Java methods
Map<String, Object> context = new HashMap<>();
context.put("val", "010");
// Convert string "010" to int, call toString().length()
Assertions.assertEquals(2, ELExecutor.eval("((int)val).toString().length()", context, int.class));
```

## Ternary Expressions

``` java
Map<String, Object> context = new HashMap<>();
context.put("flag", 100);
context.put("val", 10);
Assertions.assertEquals(100, ELExecutor.eval("flag>=100?val*10:val/10", context, int.class));
Assertions.assertEquals(1, ELExecutor.eval("flag<100?val*10:val/10", context, int.class));
```

## If-Else Branching

``` java
Map<String, Object> context = new HashMap<>();

// Branch 1 (IF)
context.put("flag", 1);
ELExecutor.eval("if(flag==1){\n a=1;\n }", context);
Assertions.assertEquals(1, ElUtils.cast(context.get("a"), int.class));
context.clear();

// Branch 2 (IF-ELSE-IF)
context.put("flag", 2);
ELExecutor.eval("if(flag==1){a=1; }else if(flag==2){ a=2;a++; }", context);
Assertions.assertEquals(3, ElUtils.cast(context.get("a"), int.class));
context.clear();

// Branch 3 (IF-ELSE-IF-ELSE)
context.put("flag", 3);
ELExecutor.eval("if(flag==1){\n a=1;\n }else if(flag==2){ a=2;a++; }else{ a=0; }", context);
Assertions.assertEquals(0, ElUtils.cast(context.get("a"), int.class));
```

## For Loops

``` java
// Control group: Java loop
int[] arr = new int[10];
for (int i = 0; i < 10; i++) {
    arr[i] = 3 * (i + 1234) - i;
}

// C-style loop
Map<String, Object> context = new HashMap<>();
ELExecutor.eval("for(i=0;i<10;i++){arr[i]=3 * (i + 1234) - i}", context);
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));
context.clear();

// Iterator-style loop
context.put("num", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
ELExecutor.eval("for(i:num){arr[i]=3 * (i + 1234) - i}", context);
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));
context.clear();

// Numeric loop (equivalent to i=0;i<10;i++)
ELExecutor.eval("for(i:10){arr[i]=3 * (i + 1234) - i}", context);
Assertions.assertArrayEquals(arr, ElUtils.cast(context.get("arr"), int[].class));

// Nested loops (print 9x9 multiplication table)
ELExecutor.eval("for(i=1;i<=9;i++){" +
        "   for(j=1;j<=9;j++){" +
        "       $.printf(i+'*'+j+'='+(i*j)+'\t');" +
        "   }" +
        "   $.printf('\n')" +
        "}", new HashMap<>());
```

## Custom & Built-in Functions

### Custom Function Extension

```properties
# Create META-INF/simplify-el.functions
$=InnerFunctions
```

Sample inner functions (`InnerFunctions`):

``` java
public class InnerFunctions {
    @ELMethod(order = Integer.MAX_VALUE)
    public static void println(Object object) {
        System.out.println(object);
    }
}
```

### Built-in Functions

• `$.println(xxxx)`: Print to console
• `$.printf(xxxx)`: Print without newline
• `$.log(xxxx)`: Log using SLF4J
• `$.max(a,b)`: Return larger value
• `$.min(a,b)`: Return smaller value
• `$.merge([map1,map2...])`: Deep merge maps
• `$.getEnumMap(xx)`: Get enum mapping

## Roadmap

• Analyze Java standard libraries for common method integration
• Optimize usability with additional utility classes
• Improve documentation and error handling