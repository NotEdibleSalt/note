# Rust学习笔记

## 变量与可变性



## 数据类型

### 标量类型

**标量**（*scalar*）类型代表一个单独的值。Rust 有四种基本的标量类型：整型、浮点型、布尔类型和字符类型

#### 整型

| 长度    | 有符号  | 无符号  |
| ------- | ------- | ------- |
| 8-bit   | `i8`    | `u8`    |
| 16-bit  | `i16`   | `u16`   |
| 32-bit  | `i32`   | `u32`   |
| 64-bit  | `i64`   | `u64`   |
| 128-bit | `i128`  | `u128`  |
| arch    | `isize` | `usize` |

#### 浮点数

Rust 的浮点数类型是 `f32` 和 `f64`，分别占 32 位和 64 位。默认类型是 `f64`

#### 布尔

Rust 中的布尔类型有两个可能的值：`true` 和 `false`

> Rust 并不会尝试自动地将非布尔值转换为布尔值

#### 字符

Rust 的 `char` 类型的大小为四个字节(four bytes)，并代表了一个 Unicode 标量值。在 Rust 中，拼音字母、中文、日文、韩文等字符，emoj以及零长度的空白字符都是有效的`char` 值

```rust
fn main() {
    let c = 'z';
    let z = '掘';
    let heart_eyed_cat = '😻';
    println!("{}, {}, {}", c, z, heart_eyed_cat);   
}
```

### 复合类型

Rust 有两个原生的复合类型：`元组（tuple）`和`数组（array）`

#### 元组（tuple）

元组是一个将`多个其他类型`的值组合进`一个复合类型`的主要方式，元组长度固定：一旦声明其长度不会增大或缩小

使用包含在`圆括号中的逗号分隔的值列表`来创建一个元组。元组中的每一个位置可以使用不同的类型标识

可以食用模式匹配解构和点号（`.`）访问元组中的元素

```rust
fn main() {

    // 显示声明一个元组
    let tup: (i32, f64, u8) = (500, 6.4, 1);
    println!("The value of y is: {}, {}, {}", tup.0, tup.1, tup.2);

    // 隐式声明一个元组
    let tup = ('a', 6.4, true);
    let (x, y, z) = tup;
    println!("The value of y is: {}, {}, {}", x, y, z); 
}
```

#### 数组（array）

与其他静态语言中的数组相同，Rust中的数组也有类型相同、长度固定的特点

```rust
fn main() {

    let a = [1, 2, 3, 4, 5];
    // 显示指定数组的类型和长度
    let a: [i32; 5] = [1, 2, 3, 4, 5];
    // 指定填充值和长度，下面的写法与 let a = [3, 3, 3, 3, 3]; 效果相同 
    let a = [3; 5];
}
```

## 集合

Rust常见的集合有以下三种：

- **vector** 允许我们一个挨着一个地储存一系列数量可变的值
- **字符串**（*string*）是字符的集合
- **哈希 map**（*hash map*）允许我们将值与一个特定的键（key）相关联

### vector

`vector` 类似于`ArrayList`可以存储多个值，在内存中彼此相邻地排列所有的值。`vector `只能储存相同类型的值

#### 创建

```rust
// 创建一个存储i32类型的空 vector
let v: Vec<i32> = Vec::new();

// 创建一个包含初始值的vector
let v = vec![1, 2, 3];
```

#### 追加元素

```rust
// 新建一个可变的vector
let mut v = Vec::new();
// 向vector追加元素
v.push(5);
```

#### 弹出最后一个元素

```rust
v.pop();
```

#### 获取元素

Rust提供两种方式访问`vector`中的元素，`下标`和`get`方法

```rust
let v = vec![1, 2, 3, 4, 5];

 // get方法会返回一个Option, 可以有效避免空指针
 let does_not_exist = v.get(100);
println!("does_not_exist: {}", does_not_exist.is_none());
    
// 通过下标访问时，如果下标超出vector的长度，会出现异常引发程序崩溃
let does_not_exist = &v[100];
println!("does_not_exist: {}", does_not_exist);
```

#### 遍历

可以使用`for in`的方式遍历`vector`中的元素

```rust
let v = vec![100, 32, 57];
for i in &v {
    println!("{}", i);
}
```

#### 结合枚举存储不同类型的值

利用枚举的所有成员都是统一类型的特点，可以实现在`vector`中存储不同类型的值

```rust
enum SpreadsheetCell {
    Int(i32),
    Float(f64),
    Text(String),
}

let row = vec![
    SpreadsheetCell::Int(3),
    SpreadsheetCell::Text(String::from("blue")),
    SpreadsheetCell::Float(10.12),
];
```

>vector离开作用域被清除时，其存储的元素也会被清除

### 字符串

，，，

### 哈希 map

#### 创建

```rust
use std::collections::HashMap;

fn main() {
    // 方式1：使用new关键字创建HashMap
    let mut map = HashMap::new();
    map.insert(1, 1);
    let a = map.get(&1);
    println!("a: {}", a.expect("a"));
    
    // 方式2：使用一个元组的 vector 的 collect 方法
    let teams  = vec![String::from("Blue"), String::from("Yellow")];
    let initial_scores = vec![10, 50];
    let tup = teams.iter().zip(initial_scores.iter());
    let map:HashMap<_, _> = tup.collect();
    println!("map: {:?}", map);

}
```

#### 所有权

对于 `i32` 这样的实现了 `Copy` trait 的类型，其值可以拷贝进哈希 map

像 `String` 这样拥有所有权的值，其值将被移动，而哈希 map 会成为这些值的所有者

#### 获取元素

```rust
use std::collections::HashMap;

fn main() {

    let mut scores = HashMap::new();
    scores.insert("Blue", 10);
    scores.insert("Yellow", 50);
    // 使用get方法获取元素
    let score = scores.get(&"Blue");
    println!("score: {}", score.expect("null"));

    // 遍历HashMap
    for (key, value) in &scores {
        println!("{}: {}", key, value);
    }
}
```

#### 只在键没有对应值时插入

```rust
use std::collections::HashMap;

fn main() {

    let mut scores = HashMap::new();
    scores.insert("Blue", 10);

    // 检查k "Blue" 是否存在，不存在就插入
    scores.entry("Blue").or_insert(30);
    scores.entry("Yellow").or_insert(50);

    println!("{:?}", scores);
}
```

#### 根据旧值更新一个值

~~~rust
use std::collections::HashMap;

fn main() {

    let text = "hello world wonderful world";

    let mut map = HashMap::new();
    
    for word in text.split_whitespace() {
        // or_insert方法会返回这个键的值的一个可变引用（&mut V）
        let count = map.entry(word).or_insert(0);
        // 用*号解引用进行赋值
        *count += 1;
    }
    
    println!("{:?}", map);
}
~~~



## 函数

Rust使用关键字`fn` 关键字声明新函数。

Rust 代码中的函数和变量名使用 *snake case* 规范风格（全小写字母加下划线）

### 参数

在函数签名中，**必须** 声明每个参数的类型

### 返回值

Rust在箭头（`->`）后声明它的类型

在 Rust 中，函数的返回值等同于函数体最后一个表达式的值

使用 `return` 关键字和指定值，可从函数中提前返回；但大部分函数隐式的返回最后的表达式

```rust
fn main() {

    let (a, b) = f();
    println!("a: {}, b: {}", a, b);
}

// 函数f, 返回一个包含u32和String类型的元组
fn f() -> (u32, String){

    const MAX_POINTS: u32 = 100_000;
    return (MAX_POINTS, String::from("asd"));
}
```

### 包含语句和表达式的函数体

函数体由一系列的语句和一个可选的结尾表达式构成

1. **语句**（*Statements*）是执行一些操作但不返回值的指令

2. **表达式**（*Expressions*）计算并产生一个值

~~~rust
fn main() {
    // 语句 
    let x = 5;
    println!("The value of y is: {}", x);

    let y = {
        let x = 3;
        // 表达式，默认将最后一句返回
        x + 1
    };
    println!("The value of y is: {}", y);
}
~~~

## 控制流

### if

```rust
fn main() {
   
    let number = 6;

    if number % 4 == 0 {
        println!("number is divisible by 4");
    } else if number % 3 == 0 {
        println!("number is divisible by 3");
    } else if number % 2 == 0 {
        println!("number is divisible by 2");
    } else {
        println!("number is not divisible by 4, 3, or 2");
    }
    
    
}
```

> `if` 是一个表达式，可以使用`let`接受它的返回值
>
> ```rust
> fn main() {
>     
>     let number = if true {
>         5
>     } else {
>         6
>     };
>     println!("The value of number is: {}", number);
> }
> ```

### loop循环

`loop` 无限循环，直到使用`break`关键字明确停止

```rust
fn main() {

    let mut a: u16 = 1;
    loop {
    
        a = a + 1;
        println!("a: {}", a);
        if a == 5 {
            break;
        }
    }
}
```

#### 返回值

```rust
fn main() {

    let mut a: u16 = 1;
    let result = loop {
    
        a = a + 1;
        println!("a: {}", a);
        if a == 5 {
            // 返回循环体的值，可以使用let接收
            break a * 2;
        }
    };
    println!("result: {}", result);
}
```

### while条件循环

```rust
fn main() {
    
    let mut number = 3;
    while number != 0 {
        
        println!("{}!", number);
        number = number - 1;
    }
    println!("number: {}", number);
}
```

### for循环遍历

```rust
fn main() {
    for number in (1..4).rev() {
        println!("{}!", number);
    }
    println!("LIFTOFF!!!");
}
```

## 所有权

所有权（系统）是 Rust 最为与众不同的特性，它让 Rust 无需垃圾回收（garbage collector）即可保障内存安全

### 所有权规则

> 1. Rust 中的每一个值都有一个被称为其 **所有者**（*owner*）的变量
> 2. 值在任一时刻有且只有一个所有者
> 3. 当所有者（变量）离开作用域，这个值将被丢弃



## 结构体

`struct`或者 `structure`，通过包装标量类型和复合类型自定义一个新的数据类型

### 定义

结构体使用关键字`stuct`定义

```rust
// 定义一个名为User的结构体
struct User {
    username: String,
    email: String,
    sign_in_count: u64,
    active: bool,
}
```

### 实例化

```rust
// 实例化一个Users实例
let mut user1 = User {
    email: String::from("someone@example.com"),
    username: String::from("someusername123"),
    active: true,
    sign_in_count: 1,
};

// 改变user1中email字段的值
user1.email = String::from("anotheremail@example.com");
```

> 整个实例的所有字段要么都可变，要么都不可变；Rust 并不允许只将某个字段标记为可变

#### 变量与字段同名时的字段初始化简写语法

```rust
// 因为函数的email和username参数与User结构体的这两个属性名相同，实例化的时候可以简写
fn build_user(email: String, username: String) -> User {
    User {
        email,
        username,
        active: true,
        sign_in_count: 1,
    }
}
```

#### 使用结构体更新语法从其他实例创建实例

可以通过获取其他实例字段值的方式创建新的实例

```rust
let user2 = User {
    email: String::from("another@example.com"),
    username: String::from("anotherusername567"),
    ..user1  // 获取实例user1的active和sign_in_count字段值
};
```

#### 元组结构体

元组结构体有着结构体名称提供的含义，但没有具体的字段名，只有字段的类型

```rust
// 定义两个元组结构体
struct Color(i32, i32, i32);
struct Point(i32, i32, i32);

// 实例化这两个元组结构体
let black = Color(0, 0, 0);
let origin = Point(0, 0, 0);
```

#### 类单元结构体

一个没有任何字段的结构体

### 方法

`方法`与函数类似：使用 `fn` 关键字声明，拥有参数和返回值

方法与函数的不同之处在于，方法在`结构体`、`枚举`或 `trait 对象`的上下文中被定义，并且它的第一个参数总是 `self`，代表调用该方法的结构体实例

#### 定义定义

```rust
struct Rectangle {
    width: u32,
    height: u32,
}

impl Rectangle {
    fn area(&self) -> u32 {
        self.width * self.height
    }
}

fn main() {
    let rect1 = Rectangle { width: 30, height: 50 };

    println!(
        "The area of the rectangle is {} square pixels.",
        rect1.area()
    );
}
```

方式使用实例`.`的方式调用

#### 关联函数

定义在impl块中的函数(参数不以`self`开头)是`关联函数`

`关联函数`使用`::`调用，如String::from

> 结构体可以有多个 `impl` 块，

## 枚举与模式匹配

枚举允许你通过列举可能的 **成员**（*variants*） 来定义一个类型

### 定义枚举

枚举使用关键字`enum`定义

```rust
enum color {
    red,
    blur,
}
```

使用`::`可以获取枚举值

~~~rust
let red = color::red;
~~~

> 枚举也可以使用 `impl` 定义方法，使用枚举值`.`调用

### match 运算符

Rust 有一个叫做 `match` 的极为强大的控制流运算符，它允许我们将一个值与一系列的模式相比较，并根据相匹配的模式执行相应代码

模式可由字面值、变量、通配符和许多其他内容构成

```rust
use std::option::Option;

fn main() {

    fn plus_one(x: Option<i32>) -> Option<i32> {
        match x {
            None => None,
            Some(i) => Some(i + 1),
        }
    }
    
    let five = Some(5);
    let six = plus_one(five);
    let none = plus_one(None);
    println!("five: {}", five.expect("空值"));
    println!("six: {}", six.expect("空值"));
    println!("none: {}", none.is_none());
}
```

#### 通配符

`match`语句可以使用通配符`__`匹配所有的可能值

```rust
let some_u8_value = 0u8;
match some_u8_value {
    1 => println!("one"),
    3 => println!("three"),
    5 => println!("five"),
    7 => println!("seven"),
    _ => (),
}
```

### `if let` 简单控制流

`if let` 获取通过等号分隔的一个模式和一个表达式，来达到match的效果

```rust
let some_u8_value = Some(0u8);
match some_u8_value {
    Some(3) => println!("three"),
    _ => (),
}

// 等同于上面的match
if let Some(3) = some_u8_value {
    println!("three");
}
```

