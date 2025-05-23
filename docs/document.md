# Simply Expression Language

***

## 介绍

简易表达式，旨在解决判断、计算、取值、结构转换这些逻辑简单但编码繁复的工作<br>
主要应用场景：动态规则的计算场景、类型适配（报文转换）

## 定义说明

- 上下文：当前时态下的运行环境变量集合，可通过执行表达式对上下文节点进行取值、赋值、计算、删除等操作。
- 上下文节点：指代上下文中存放值的“路径”，若将上下文当做一种映射，则上下文节点映射它对应的上下文节点值。

## 主要API

这里介绍类`ELExecutor`中的API

``` java

    /**
     * 生成表达式对象
     *
     * @param el 表达式字符串
     * @return 可执行的表达式对象
     */
    public static EL compile(final String el);

    /**
     * 生成节点对象
     *
     * @param nodeName 节点名
     * @return 可执行的节点对象
     */
    public static Node compileNode(String nodeName);

    /**
     * 计算表达式，返回结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @return 计算结果
     */
    public static Object eval(String el, Object vars);

    /**
     * 计算表达式，返回指定类型结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @param type 预期结果的类型
     * @return 计算结果
     */
    public static <T> T eval(String el, Object vars, Class<T> type);

    /**
     * 计算表达式，返回指定类型结果
     *
     * @param el   表达式
     * @param vars 上下文
     * @param type 预期结果的类型
     * @return 计算结果
     */
    public static Object eval(String el, Object vars, Type type);

    /**
     * 从对象中获取某个属性或节点
     *
     * @param ctx      上下文/POJO
     * @param nodeName 属性名或节点名
     * @return 对应属性或节点的值
     */
    public static Object getNode(Object ctx, String nodeName);

    /**
     * 将某个值放入对象指定属性或节点
     *
     * @param ctx      上下文/POJO
     * @param nodeName 属性名或节点名
     * @param value    要放入的值
     */
    public static void putNode(Object ctx, String nodeName, Object value);

    /**
     * 将某个值或节点移除或置空
     *
     * @param ctx      上下文/POJO
     * @param nodeName 要移除或置空的节点名
     */
    public static void removeNode(Object ctx, String nodeName);

```

## 语法介绍

### 节点

节点表示在上下文中的地址或位置，并满足以下规则

- 它以字母开头，以符号【.】作为层级划分。
- 若它的层级以`[index]`结尾，则代表这个层级指代数组或者List中指定第`index`位的值，index会被解析为整型。
- 若它的层级以`[index]`结尾时，index为0也可以指代非数组类对象本身。
- 除了特殊节点（例如根节点）、常量、关键字，一般都是可赋值节点。

例如： 假设上下文内容用JSON表示为`{"a":{"b":{"arr":[null,"100"],"x":{"d":"testStr3"}}}}`则

- 节点`a`取值为`{"b":{"arr":[null,"100"],"x":{"d":"testStr3"}}}`。
- 节点`a.b.arr`取值为`[null,"100"]`。
- 节点`a.b.arr[1]`取值为`100`。
- 节点`a.b.x.d`取值为`testStr3`。
- 节点`a.b.x.d[0]`取值为`testStr3`。
- 节点`a.b.x.d[1]`取值为`null`。

### 关键字

- `this`表示表达式操作的当前上下文。
- `null`表示java语义中的`null`，一般用于判断相等或不等
- `empty`表示java语义中的`null`或者空字符串，一般用于判断相等或不等。
- `blank`表示java语义中的`null`或空白字符串（空格），一般用于判断相等或不等。
- `true`表示java语义中的`true`。
- `false`表示java语义中的`false`。

另这些表达式`null==empty`、`null==blank`、`empty==blank`执行结果均为true

### 字符串

以`"`开头且以`"`结尾或者以`'`开头且以`'`结尾的内容表示一个字符串。字符串满足以下规则：

- 字符串是常量，它可以赋值给节点但不能被赋值。
- 若在双引号括起来的字段中需要表示双引号则需要加上`\`作为转义符。
- 同理在单引号括起来的字段中需要使用单引号也需要加上`\`作为转义符。
- 双引号和单引号扩住的字符串是同样的字符串，除了token不同并无其他区别。

例如:

- 表达式`' '==" "`执行执行结果为true
- 表达式`''==blank`执行结果为true
- 表达式`'123456'=="123456"`执行结果为true
- 表达式`'"123"'`执行结果为`"123"`
- 表达式`"\"123\""`执行结果为`"123"`
- 表达式`"'123'"`执行结果为`'123'`
- 表达式`'\'123\''`执行结果为`'123'`

### 数字、数值

以数字开头的内容表示一个数字或数值，数字是常量，它可以赋值给节点但不能被赋值。<br>
例如正确的写法为`1000`或`88.888`。<br>
若一个表达式为`123xxx`或`123.1.1.1`，它会被识别为数字然后解析失败。<br>

### 数组、集合

以`[`开头且以`]`结尾中间用`,`分隔表示一个数组，写法为`[el1,el2,el3]`。它满足如下规则：

- 数组表达式会经过多次计算，分别计算数组内的子表达式，然后按顺序组装成数组。
- 数组支持使用size()方法取长度。

例如:

- 假设i=0,那么`[i++,i++,i++]`的计算结果为`[0,1,2]`，且执行完毕之后上下文中i=3。
- 假设执行了表达式`arr=['x',null,'y']`，那么再执行表达式`arr.size()`计算结果为3，`arr[0]`计算结果为`x`，`arr[2]`计算结果为`y`;

### K-V对象(JSON对象)

以`{`开头且以`}`结尾，中间用`,`分隔表示一个K-V（JSON）对象，写法为`{el1:el2,"string":el3,el4:"string"
}`。它满足如下规则：

- JSON表达式会经过多次计算，分别计算其中的子表达式，其中KEY在计算完毕之后会转换为字符串类型。
- 无论是KEY还是VALUE，若被`'`或`"`括起来则不进行计算，直接算为字符串。

例如:
假设i=0，那么表达式`{i++:i++,"str":i++,i++:"str2"}`计算结果为`{"0":1,"str":2,"3":"str2"}`，且执行完毕之后上下文中i=4。

### 子表达式

以`(`开头且以`)`代表另一个层级或子表达式串，主要用途为划分层级和避免歧义，它满足如下规则：

- 子表达式串中的运算符或分割符等均不会与外部混合计算。
- 子表达式仅代表它为一个不可分割整体，不影响其它包括但不限于计算顺序、计算方式等逻辑。

例如:表达式`a=(true;false)?123456:33;a+300`计算结果为333，且执行完毕后上下文中a=33

### 占位符和拼接

在表达式中，若需要使用占位符或拼接，可以使用占位符`${}`代表计算部分，使用占位符`#{}`代表取节点部分，在占位符之外则代表字符串。<br>
若表达式中不存在占位符，则表示整个表达式部分为计算部分，不存在拼接。<br>
例如: 假设上下文内容为`{"a":3, "b":1, "a-b":"abc"}`，那么:

- 表达式`xx${a}xx`执行结果为xx3xx。
- 表达式`${a}`执行结果为3。
- 表达式`a-b`执行结果为2。
- 表达式`${a-b}`的执行结果为2。
- 表达式`#{a-b}`的执行结果为abc

## 运算符

*运算符解析顺序为分隔符、赋值、三元表达式、二元计算、一元计算*

### 分隔符

在表达式中，可以允许使用分割符号`;`使表达式进行多次计算。<br>
写法为`el1;el2;el3;`或者`el1;el2;el3`等写法。<br>
以上写法会按顺序执行`el1`、`el2`、`el3`，然后将`el3`的执行结果返回。<br>
例如：表达式`a=1;a++;a`的执行结果为2

### 赋值

在表达式中，允许使用等于号`=`对可赋值节点进行赋值，并返回这个结果，赋值本质上是修改指定位置的值并返回。写法为`nodeName=elString`，需要注意以下问题：

- 若一个表达式无法表达它的位置，则它不可以被赋值。
- 在使用赋值时会把=两边作为子表达式分别计算，先计算等号右边的值，然后计算等号左边的节点位置并进行赋值后将结果返回。

例如：<br>

- 表达式`a=1`的执行后在上下文中将节点a设置为1，且这个表达式执行结果也是1
- 假设i=0，那么表达式`arr[i++]=i`，会将0赋值给`arr[0]`的值并且返回0，且此时上下文中i=1，用JSON表示上下文为`{"i":1, "arr":[0]}`
- 表达式`map.a=map.b=map.c=1;map`执行结果为`{"a":1,"b":1,"c":1}`

### 可赋值节点说明

在上下文中，根节点(this)、方法执行结果、常量(例如数字和字符串或上下文本身不可写)或各种关键字不可被赋值，如下几种写法执行会抛出异常：

- `this=xxxx`
- `[1，0，2]=[1，2，3]`
- `a.b.c.toString()=xxx`
- `1=2`
- `"string"=safasgag`
- `true=false`

正确的可赋值节点包括，上下文节点或数组，有set方法的pojo对象等等，正确的赋值一般为下面这些写法：

- `a=1`
- `a="字符串"`
- `i=0;a[i++]=i; a[i++]=i; a[i++]=i`

### 三元表达式

三元表达式以条件开头且包含`?`和`:`的表达式，写法为`el1?el2:el3`，需要注意以下问题：

- 三元表达式会经过两次计算，先计算el1子串并转换为布尔型进行判断，如果为true则计算el2且返回结果否则计算el3且返回结果。

例如：

- 表达式`true?123456:33`执行结果为123456
- 表达式`false?123456:33`执行结果为33

### 二元计算

需要两个参数参与的计算为二元计算，当前所有支持的二元计算符如下所示，语义同java语法中语义

1. `&&`、`||`
2. `==`、`!=`、`~=`、`>=`、`<=`、`>`、`<`
3. `+`、`-`
4. `*`、`/`、`%`
5. `+=`、`-=`、`*=`、`/=`

二元计算有如下计算规则：

- 在没有括号参与的情况下，序号越大越优先计算，存在括号则括号内整体计算。
- `&&`和`||`运算符要求两个参数均为布尔或者可以转换为布尔型。
- `==`和`!=`运算符会先比较引用然后转换为字符串进行比较，若为数值则按数值进行比较。
- `~=`运算符为正则运算符，会将左右两边转换为字符串，其中左边为待匹配字符串，右边为正则表达式，待匹配字符串或正则表达式为null时计算结果为false。
- `+`运算符支持字符串拼接。
- `-`、`*`、`/`计算均会将两个入参转换为数值进行计算，若传入为空值将被解析成0，若无法转换为数值则抛出异常。
- 由于除法可能遇到除不尽且抛出异常的情况，除法`/`计算均会将小数后精度设置为8位。

例如:

- 假设a=1，那么`true&&1+a*2<2`计算结果为false。
- 假设a=1，那么`-a`的计算结果为-1（是0-1的结果）
- 表达式`4/2`计算结果为2.00000000
- 表达式`4/2==2`计算结果为true
- 表达式`123456=="123456"`计算结果为true
- 表达式`false&&true==false`计算结果为false
- 表达式`'18888888888' ~= '^[0-9]{11}$'`计算结果为true

### 一元计算

需要一个参数参与的计算为一元计算，目前支持的一元计算为`!`、`++`、`--`。<br>

- `!`为取反操作，写法为`!el`，它会把el尝试转换为布尔型且取反，要求传入表达可以转换为布尔类型。
- `++`为自增操作，写法为`el++`或`++el`，语义与java一致，但要求传入表达式必须为可赋值节点。
- `--`为自减操作，写法为`el--`或`--el`，语义与java一致，但要求传入表达式必须为可赋值节点。

这些是错误的写法：`100--`、`!"string"`

### 类型转换

在表达式中，可以通过类型转换（写法类似于java中强制转换）将上下文中对象类型进行转换。<br>
当前默认支持`(int)`、`(long)`、`(boolean)`、`(char)`、`(byte)`、`(short)`、`(float)`
、`(double)`、`(String)`、`(class)`、`(BigDecimal)`可通过ConversionBuilder的addBuilder方法添加自定义转换类型（关键字必须用括号包括）。<br>
写法为 `(类型)el`，例如:
表达式`str="123456";dec=(BigDecimal)str;dec.getClass().toString()`的执行结果为`class java.math.BigDecimal`<br>
注意：该转换不是java的强制转换，底层是调用fastjson提供的类型转换工具。

## 测试用例

作为使用参考，可以查看单元测试中的测试类[ElTest.java](../src/test/java/unit/ElTest.java)
