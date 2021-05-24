# Solidity学习笔记

Solidity是为了编写智能合约而创建的编程语言，Solidity运行在以太坊虚拟机中（EVM）。

## 数据类型

### 值类型

值类型被用作函数参数或者用在赋值语句中时，总会进行值拷贝

#### `bool` 

取值为字面常数值 `true` 和 `false`，默认`false`

#### `int` / `uint` 

有符号和无符号的不同位数的整型变量。支持（int8` 到 `int256 / uint8` 到 `uint256）

#### `address`

一个 20 字节的值（以太坊地址的大小），可以转换成uint160类型。

##### 成员变量

- `balance`：该地址的余额
- `transfer`：向一个地址发送 以太币Ether （以 wei 为单位）。执行失败时以太币Ether 交易会被打回，终止执行并抛出异常
- `send`：`transfer` 的低级版本。如果执行失败，当前执行的合约不会被终止，但 `send` 会返回 `false`
- `call`：调用外部合约，返回值为true或者false。只有当能够找到此方法并执行成功后，会返回true，而如果不能够找到此函数或执行失则会返回false。如果外部合约有回调函数，在找不到调用的函数时，会执行回调函数。会修改外部合约的状态，不会修改本地合约的状态
- `delegatecall`：调用外部合约，返回值为true或者false。只有当能够找到此方法并执行成功后，会返回true，而如果不能够找到此函数或执行失则会返回false。如果外部合约有回调函数，在找不到调用的函数时，会执行回调函数。不会修改外部合约中的状态变量，会修改本地合约状态变量的值

>从 0.5.0 版本开始，合约不会从地址类型派生，但仍然可以显式地转换成地址类型

#### 定长字节数组

定长字节数组有` bytes1`， `bytes2`， `bytes3`， ...， `bytes32`。`byte` 是 `bytes1` 的别名。

##### 成员变量

- `.length` 表示这个字节数组的长度（只读）

#### 枚举类型

使用关键字`enum`可以创建一个枚举类型

```java
enum ActionChoices { GoLeft, GoRight, GoStraight, SitStill }
```

### 引用类型

相比值类型引用类型都有一个额外属性`数据位置`，用来说明数据是保存在内存`memory `中还是存储`storage `中。这是因为在拷贝这些类型变量的时候Gas开销相当大，为了尽量减少减少开销执行`数据位置`是非常必要的。

根据上下文不同，大多数时候数据有默认的位置，但也可以通过在类型名后增加关键字 `storage` 或 `memory` 进行修改

默认数据位置：

- 外部函数的参数（不包括返回参数）： `calldata`
- 状态变量： `storage`

- 函数参数（包括返回参数）： `memory`
- 所有其它局部变量： `storage`

>在存储`storage`和内存`memory `之间两两赋值，或者存储`storage`向`状态变量`或（`状态变量`向`状态变量`）赋值都会创建一份独立的拷贝
>
>状态变量向局部变量赋值时仅仅传递一个引用，而且这个引用总是指向状态变量，因此后者改变的同时前者也会发生改变
>
>从一个内存memory存储的引用类型向另一个 内存memory存储的引用类型赋值并不会创建拷贝

#### string

字符串字面常数是指由双引号或单引号引起来的字符串（`"foo"` 或者 `'bar'`）。可以隐式地转换成 `bytes1`，……，`bytes32`

#### 数组

solidity中的数组可以在声明时指定长度，也可以动态调整大小。一个元素类型为 `T`，固定长度为 `k` 的数组可以声明为 `T[k]`，而动态数组声明为 `T[]`

对于存储`storage`的数组来说，元素类型可以是任意的（即元素也可以是数组类型，映射类型或者结构体）。对于 内存`memory`的数组来说，元素类型不能是映射类型(`mapping`)，如果作为 public 函数的参数，它只能是 ABI 类型。

##### 创建内存数组

可以使用`new`关键字在内存`memory`中创建变长数组。与存储`storage`数组相反的是，你不能通过修改成员变量 `.length`改变内存`memory`数组的大小

```java
// 创建一个长度为7，uint256类型的数组
uint256[] memory a = new uint256[](7);

// 这行会报错，因为x是变长的内存数组， [1, 3, 4]是长度为3的定长数组
uint[] x = [1, 3, 4];
```

>定长的内存`memory`数组并不能赋值给变长的内存`memory`数组

##### 成员变量

- `length`: 当前数组的长度。动态数组可以在存储`storag`中通过改变成员变量`.length` 改变数组大小
- `push`: 变长的存储`storage`数组以及`bytes`类型都有`push`函数，用来在数组末尾追加一个新的元素并返回新数组的长度

>如果使用`.length` 使数组的减少数组的长度，超出的元素会被清除
>
>可以使用`delete`关键字删除数组
>
>```java
>// 删除数组中下标2中的元素
>delete arr[2];
>
>// 删除数组中的所有元素
>delete arr
>```

#### 结构体

结构体是由一批数据组合而成的一种新的数据类型。Solidity中使用关键字`struct`定义结构体

```java
contract UserManage {
   
    // 定义一个名为User的结构体，包含两个属性id和name
    struct User {
        uint8 id;
        string name;
    }

    mapping (uint8 => User) Users;

    function add() public {
        
        // 实例化一个user，存储位置在内存memory
        User memory user1 = User(1, "Alice");
        User memory user2 = User({id: 2, name: "Bobo"});
        
        // 将实例化的user放在mapping中
        Users[1] = user1;
        Users[2] = user2;
    }
    
   function getNameById(uint8 id) public view returns(string name){
        
        User memory user = Users[id];
        // 可以通过实例.属性的方式访问结构体的属性
        return user.name;
    }
}
```

>结构体不能包含自己,但是可以使用mapping类型引用自己

#### mapping(映射)

Solidity中的`mapprng`类似于`map`类型，存储K-V结构的数据。但是在`mapprng`中并不存储 key，而是存储key的 `keccak256` 哈希值

##### 声明方式

 ```java
 mapping(_KeyType => _ValueType)
 ```

`_KeyType` 可以是除了映射、变长数组、合约、枚举以及结构体以外的几乎所有类型

 `_ValueType` 可以是包括映射类型在内的任何类型

>不支持迭代，只能根据确定的key获取value

## 控制、循环

`Solidity`中有`if`，`else`，`while`，`do`，`for`，`break`，`continue`，`return`，`? :`控制循环语句

>`Solidity`中非布尔类型数值不能转换为布尔类型,因此 `if (1) { ... }` 的写法在`Solidity`中无效

## 可见性

`external`

本合约中不能使用，只能在外部合约中使用

`public`

本合约和外部合约都可以调用

`internal`

在本合约或者子合约中使用

`private`

只能在本合约中使用

## 函数

### 声明方式

```java
// 函数标识  函数名         参数     可见性  状态声明   返回值（可以有多个返回值）
function getNameById(uint8 id) public view returns(string name){        
        return "aaa";
 }
```

### 函数修饰器

函数修饰器有点像Vue中的插槽，将函数放在修饰器的指定位置执行。但函数执行之前或之后，会执行函数修饰器中的语句，`修饰器可以被继承，并且可以被派生合约覆盖`

```java
// 声明语句  修饰器名称
modifier checkSender {
        require(msg.sender == owner);
        _;   // 被修饰函数将在这里执行
}

// 使用修饰器checkSender修饰close函数，close函数被调用后会先执行 require(msg.sender == owner)语句
function close() public checkSender {
   
}
```

### 函数状态声明

#### View

将函数声明为`view`时，可以读取合约状态，不能修改合约状态

下面的语句被认为是修改状态：

1. 修改状态变量
2. 产生事件（`event`）
3. 创建其它合约
4. 使 `selfdestruct`销毁合约
5. 通过调用发送以太币
6. 调用任何没有标记为`view`或者 `pure`的函数
7. 使用低级调用语句（call、）
8. 使用包含特定操作码的内联汇编

#### Pure

不能读取或修改合约状态

除上面的修改状态语句之外，以下语句被认为是从读取状态：

1. 读取状态变量
2. 访问 `this.balance` 或者 `<address>.balance`
3. 访问 `block`，`tx`， `msg` 中任意成员 （除 `msg.sig` 和 `msg.data` 之外）
4. 调用任何未标记为 `pure` 的函数。
5. 使用包含某些操作码的内联汇编

### Fallback（回调函数）

一个即没有名字也不能有参数和返回值的函数

在一个合约的调用中，没有其他函数与给定的函数名匹配，那么就会执行回调函数

除此之外，回调函数还可以用来接受以太币，接收以太币时，回调函数必须标记为 `payable`

```java
// 不能接收以太币，因为 fallback 函数没有payable修饰符
 function() public {  }

// 可以接收以太币
function() public payable { }
```

### 函数重载

合约中可以具有多个不同参数的同名函数

## event（事件）

事件允许我们方便地使用 EVM 的日志基础设施

当事件被调用时，会将其参数存储到交易的日志中，这些日志与合约相关联被并存入区块链中

日志和事件在合约内不可直接被访问（甚至是创建日志的合约也不能访问）

事件在合约中可被继承

```java
// 定义一个事件
event log(
        string id,
        string name,
);

function test(string  id, string name) public  {
        // 调用事件
        log(id, name);
  }
```

## 合约

`Solidity`合约类似于面向对象语言中的类，合约中包含常量、变量、函数。

调用另一个合约实例的函数时，会执行一个 EVM 函数调用，这个操作会切换执行时的上下文，这样前一个合约的状态变量就不能访问了

### 声明

`Solidity`使用关键字`contract`声明合约

```java
// 声明合约的版本
pragma solidity ^0.4.24;

// 声明一个名为A的合约
contract A {
    // 定义一个uint类型的私有变量data
    uint private data;

    // 定义一个私有函数f
    function f(uint a) private returns(uint b) { return a + 1; }
}
```

### 构造函数

合约的构造函数有两种方式声明，

1. 使用和合约名相同的函数声明

2. 使用关键字`constructor`声明

   ```java
   contract A {
   
       bytes32 name;
   
       // 方式1：使用和合约名相同的函数声明构造函数
       // 声明合约A的构造函数
       function A(bytes32 _name) public {
           name = _name;
       }
   
       // 方式2：使用关键字constructor声明构造函数
       constructor(){
           
       }
   }
   ```

### 继承

`Solidity`使用关键字`is`继承或实现其他合约，`Solidity`支持多继承

```java
pragma solidity ^0.4.24;

contract A {
    function owned() public { owner = msg.sender; }
    address owner;
}

// 使用 is 继承合约A。B合约可以访问A合约所有非私有成员，包括内部函数和状态变量，
// 但无法通过 this 来外部访问。
contract B is A {
    
    function kill() public {
        if (msg.sender == owner) {
            selfdestruct(owner);
        }      
    }
}
```

#### 多重继承

```java
// 以下代码编译出错，代码编译出错的原因是 `C` 要求 `X` 重写 `A` （因为定义的顺序是 `A, X` ）， 但是 `A` 本身要求重写 `X`，无法解决这种冲突

pragma solidity ^0.4.24;

contract X {}
contract A is X {}
contract C is A, X {}
```

>可以通过一个简单的规则来记忆： 以从“最接近的基类”（most base-like）到“最远的继承”（most derived）的顺序来指定所有的基类

### 抽象合约

合约函数可以缺少实现，包含未实现函数的合约时抽象合约

抽象合约无法编译

如果合约继承自抽象合约，并且没有通过重写来实现所有未实现的函数，那么它本身也是抽象的

```java
pragma solidity ^0.4.24;

contract A {
    function test() public returns (bytes32);
}

// 继承A合约
contract B is A {
    // 实现A合约中的test方法
    function test() public returns (bytes32) { 
        return "hello";
    }
}
```

### 接口

接口不能有实现任何函数。除此之外接口还有以下限制：

1. 无法继承其他合约或接口
2. 无法定义构造函数
3. 无法定义变量
4. 无法定义结构体
5. 无法定义枚举

合约可以使用`is`关键字继承接口

