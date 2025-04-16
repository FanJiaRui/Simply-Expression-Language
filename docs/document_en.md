# Simply Expression Language

***

## Introduction

Simple expression is designed to solve the judgment, calculation, value, structure conversion, which is simple in logic but complicated <br> in coding
Main application scenarios: dynamic rule calculation scenario and type adaptation (message conversion)

## Definition description

- Context: a set of running environment variables in the current tense. The context node can be evaluated, assigned, calculated, or deleted by the execution expression.
- Context node: refers to the "path" of the value stored in the context. If the context is regarded as a mapping, the context node maps its corresponding context node value.

## Primary API

The API in the class `ELExecutor` is introduced here.


``` java

    /**
     * Generating expression objects
     *
     * @param el Expression string
     * @return An executable expression object
     */
    public static EL compile(final String el);

    /**
     * Generating node objects
     *
     * @param nodeName Property name or node name
     * @return An executable node object
     */
    public static Node compileNode(String nodeName);

    /**
     * Evaluates the expression and returns the result
     *
     * @param el   Expression string
     * @param vars Context or POJO
     * @return Calculation results
     */
    public static Object eval(String el, Object vars);

    /**
     * Evaluates an expression and returns a result of the specified type
     *
     * @param el   Expression string
     * @param vars Context or POJO
     * @param type The type of result expected
     * @return Calculation results
     */
    public static <T> T eval(String el, Object vars, Class<T> type);

    /**
     * Evaluates an expression and returns a result of the specified type
     *
     * @param el   Expression string
     * @param vars Context or POJO
     * @param type The type of result expected
     * @return Calculation results
     */
    public static Object eval(String el, Object vars, Type type);

    /**
     * Retrieves a property or node from an object
     *
     * @param ctx      Context or POJO
     * @param nodeName Property name or node name
     * @return The value corresponding to an attribute or node
     */
    public static Object getNode(Object ctx, String nodeName);

    /**
     * Puts a value into an object to specify a property or node
     *
     * @param ctx      Context or POJO
     * @param nodeName Property name or node name
     * @param value    The value to put in
     */
    public static void putNode(Object ctx, String nodeName, Object value);

    /**
     * To remove or empty a value or node
     *
     * @param ctx      Context or POJO
     * @param nodeName The name of the node to remove or empty
     */
    public static void removeNode(Object ctx, String nodeName);

```

## Introduction to grammar

### Node

A node represents an address or location within a context and satisfies the following rules

- It begins with a letter and is hierarchically divided by the symbol [.].
- If its level ends with `[index]`, it means that the level refers to the value of the specified `index` bit in the array or List, and index is resolved to an integer.
- An index of 0 can also refer to a non-array-class object itself if its level ends with `[index]`.
- Except for special nodes (such as root nodes), constants and keywords, they are generally assignable nodes.

For example, if the context content is represented in JSON as `{"a":{"b":{"arr":[null,"100"],"x":{"d":"testStr3"}}}}`

- The node `a` takes the value `{"b":{"arr":[null,"100"],"x":{"d":"testStr3"}}}`.
- The node `a.b.arr` takes the value `[null,"100"]`.
- The node `a.b.arr[1]` takes the value `100`.
- The node `a.b.x.d` takes the value `testStr3`.
- The node `a.b.x.d[0]` takes the value `testStr3`.
- The node `a.b.x.d[1]` takes the value `null`.

### Keyword

-  `this` Represents the current context of the expression operation.
-  `null` Denotes in `null` Java semantics and is generally used to determine equality or inequality
-  `empty` Represents either an `null` empty string in Java semantics and is generally used to determine equality or inequality.
-  `blank` Represents an empty string (space) in `null` Java semantics, typically used to determine equality or inequality.
-  `true` Represents the `true` in Java semantics.
-  `false` Represents the `false` in Java semantics.

In addition, the execution results of these expressions `null==empty`, `null==blank`, `empty==blank` are all true.

### String

Starts with `"` and ends with `"` or `'` starts with and ends with `'` represents a string. The string satisfies the following rules:

- A string is a constant that can be assigned to a node but cannot be assigned to it.
- If you need to indicate double quotes in a field enclosed in double quotes, you need to add `\` as an escape character.
- In the same way, single quotation marks are required in fields enclosed by single quotation marks, and they are also required `\` as escape characters.
- The strings enclosed by double quotes and single quotes are the same string, and there is no difference except that the token is different.

For example

- Expression `' '==" "` execution result is true
- Expression `''==blank` execution result is true
- Expression `'123456'=="123456"` execution result is true
- Expression `'"123"'` execution result is `"123"`
- Expression `"\"123\""` execution result is `"123"`
- Expression `"'123'"` execution result is `'123'`
- Expression `'\'123\''` execution result is `'123'`

### Numeric and numerical values

Anything that starts with a number indicates a number or numeric value. Numbers are constants that can be assigned to nodes but cannot be assigned. <br>
For example, the correct way to write is `1000` or `88.888`. <br>
If an expression is `123xxx` or `123.1.1.1`, it is recognized as a number and fails to parse. <br>

### Arrays, collections,

An array beginning with `[` and ending with a `]` middle `,` delimiter, written as `[el1,el2,el3]`. It satisfies the following rules:

- An array expression is evaluated multiple times, with the subexpressions within the array evaluated separately, and then assembled into an array in order.
- Arrays support the size () method for length.

For example

- Assuming that I = 0, `[i++,i++,i++]` the result of the calculation is `[0,1,2]`, and I = 3 in the context after execution.
- Assuming that the expression `arr=['x',null,'y']` is executed, the evaluation result of the re-executed expression `arr.size()` is 3, `arr[0]` the evaluation result is `x`, and `arr[2]` the evaluation result is `y`;

### K-V Object (JSON Object)

Starts with `{` and ends with `}`, with a `,` separator in the middle for a K-V (JSON) object, written as `{el1:el2,"string":el3,el4:"string"}`. It satisfies the following rules:

- The JSON expression is evaluated multiple times, and the sub-expressions are evaluated separately, where the KEY is converted to a string type after the evaluation.
- Whether it is KEY or VALUE, if `'` it is enclosed by or `"`, it will not be calculated, and it will be directly calculated as a string.

For example, if I = 0, then the expression `{i++:i++,"str":i++,i++:"str2"}` evaluates to `{"0":1,"str":2,"3":"str2"}` and I = 4 in the context after execution.

### Subexpression

Starts with `(` and `)` represents another level or subexpression string. Its main purpose is to divide levels and avoid ambiguity. It satisfies the following rules:

- Operators, delimiters, etc., in the subexpression string are not mixed with the outer evaluation.
- The sub-expression only represents that it is an indivisible whole, and does not affect other logics including but not limited to the calculation order, calculation method, etc.

For example, the expression `a=(true;false)?123456:33;a+300` evaluates to 333, and a = 33 in the context after execution

### Placeholders and Splices

In an expression, if you need to use placeholders or splices, you can use placeholders to `${}` represent the calculated part, placeholders `#{}` to represent the node part, and outside the placeholders, strings. <br>
If there is no placeholder in the expression, it means that the whole expression part is a calculation part and there is no splicing. <br>
For example, suppose the context is `{"a":3, "b":1, "a-b":"abc"}`, then:

- The expression `xx${a}xx` execution result is xx3 XX.
- The expression `${a}` execution result is 3.
- The expression `a-b` execution result is 2.
- The execution result of the expression `${a-b}` is 2.
- The execution result of the expression `#{a-b}` is ABC

## Operator

* The operator resolution order is delimiter, assignment, ternary expression, binary computation, unary computation *

### Separator

In an expression, you can allow the split symbol `;` to make the expression evaluate more than once. <br>
It is written as `el1;el2;el3;` or `el1;el2;el3` equal. <br>
The above writing will execute `el1`, `el2`, `el3` in order, and then return the `el3` execution result of. <br>
For example, the execution result of the expression `a=1;a++;a` is 2

### Assign a value

In an expression, an assignable node is allowed to be assigned with an equal sign `=` and return this result. An assignment essentially modifies the value at the specified position and returns. It is written as `nodeName=elString`, and the following issues should be noted:

- If an expression cannot express its position, it cannot be assigned.
- When using assignment, both sides of = are evaluated separately as subexpressions. First, the value to the right of the equal sign is evaluated, and then the position of the node to the left of the equal sign is calculated and the result is returned after assignment.

For example: <br>

- After the expression `a=1` is executed, the node a is set to 1 in the context, and the execution result of the expression is also 1.
- Assuming that I = 0, then the expression `arr[i++]=i` will assign 0 to `arr[0]` the value of and return 0, and at this time I = 1 in the context, the JSON representation of the context is `{"i":1, "arr":[0]}`
- The expression `map.a=map.b=map.c=1;map` is executed as `{"a":1,"b":1,"c":1}`

### Assignable Node Description

In the context, the root node (this), the result of method execution, constants (such as numbers and strings), or various keywords cannot be assigned, and the following writing methods will throw an exception:

-  `this=xxxx`
- `[1，0，2]=[1，2，3]`
-  `a.b.c.toString()=xxx`
- `1=2`
-  `"string"=safasgag`
-  `true=false`

The correct assignable nodes include context nodes or arrays, POJO objects with set methods, and so on. The correct assignment is generally written as follows:

-  `a=1`
-  `a="strXXX"`
-  `i=0;a[i++]=i; a[i++]=i; a[i++]=i`

### Ternary expression

Ternary expression An expression that begins with a condition and contains a `?` sum `:`, written as `el1?el2:el3`, with the following caveats:

- The ternary expression will go through two calculations. First, calculate the el1 substring and convert it to Boolean type for judgment. If it is true, calculate el2 and return the result. Otherwise, calculate el3 and return the result.

For example

- The result of expression `true?123456:33` execution is 123456.
- Expression `false?123456:33` execution result is 33

### Binary computation

Calculations that require two parameters to participate in are binary calculations. Currently, all supported binary operators are shown below. The semantics are the same as those in java syntax.

1. `&&` `||`
2. `==`  `!=` `~=` `>=` `<=` `>` `<`
3. `+` `-`
4. `*` `/` `%`
5. `+=` `-=` `*=` `/=`

Binary calculation has the following calculation rules:

- In the absence of parentheses, the larger the serial number, the higher the priority of calculation, and in the presence of parentheses, the overall calculation in parentheses.
- The `&&` and `||` operators require both arguments to be Boolean or convertible to Boolean.
- The `==` and `!=` operators compare references and then convert to strings or, if numeric, to numeric values.
- The `~=` operator is a regular operator that converts the left and right sides to strings, where the left side is the string to be matched and the right side is a regular expression, and evaluates to false if the string to be matched or the regular expression is null.
- The `+` operator supports string concatenation.
- The `-`, `*`, `/` calculation will convert the two input parameters into numerical values for calculation. If the input value is null, it will be resolved to 0. If it cannot be converted into a numerical value, an exception will be thrown.
- The division `/` calculation sets the precision to 8 digits after the decimal because the division may not be able to divide completely and throw an exception.

For example

- If a = 1, then the `true&&1+a*2<2` result is false.
- Suppose a = 1, then `-a` the result of the calculation is -1 (the result of 0-1).
- The expression `4/2` evaluates to 2. 00000000.
- Expression `4/2 == 2` evaluates to true
- Expression `123456 == "123456"` evaluates to true
- Expression `false&&true == false` evaluates to false
- Expression `"18888888888" ~= "^[0-9]{11}$"` evaluates to true
- Expression `phone=18888888888; phone ~= "^[0-9]{11}$"` evaluates to true

### Unary computation

A calculation that requires one parameter is a unary calculation. Currently, the supported unary calculations are `!`, `++`, `--`. <br>

-  `!` For negation, write `!el`, which attempts to convert the el to a Boolean type and negates it, requiring that the incoming expression be convertible to a Boolean type.
-  `++` It is an autoincrement operation, written as `el++` or `++el`, and its semantics is consistent with Java, but it requires that the incoming expression must be an assignable node.
-  `--` It is a self-subtraction operation, written as `el--` or `--el`, and its semantics is consistent with Java, but it requires that the incoming expression must be an assignable node.

These are the wrong way to write: `100--`, `!"string"`

### Type conversion

In an expression, the type of the object in the context can be converted through a type conversion, written like a cast in Java. <br>
, `(long)` `(boolean)`, `(char)`, `(byte)` `(short)` `(float)`, are currently supported `(int)` by default
, `(double)`, `(String)`, `(class)`, `(BigDecimal)` Custom conversion types can be added through the addBuilder method of Conversion Builder (keywords must be enclosed in parentheses). <br>
Written as `(TYPE)el`, for example:
The result of the expression `str="123456";dec=(BigDecimal)str;dec.getClass().toString()` is `class java.math.BigDecimal` <br>
Note: This conversion is not a Java cast, but a call to the type conversion tool provided by fastjson.

## Test case

For reference, you can view the test classes [ ElTest.java ](../src/test/java/unit/ElTest.java) in a unit test
