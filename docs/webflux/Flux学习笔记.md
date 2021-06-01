# Reactor学习笔记

## 简介

### Reactor（反应式/响应式）

`Reactor`是一个用于JVM的完全`非阻塞的响应式`编程框架，具备高效的需求管理（即对`背压`的控制）能力

`Reactor`是`响应式编程范式`的实现，总结起来有如下几点：

1. `响应式编程`是一种关注于`数据流`和`变化传递`的异步编程方式
2. `响应式编程`的实现方式是基于`观察者模式`的扩展

> 背压：由消费者控制生产者的生产速度，以解决生产者生产的速度远大于消费者消费的速度时所造成的消息的积压

### Flux

`Flux` 是一个以`数据流`的形式发出 `0 到 N` 个元素的`发布者(Publisher)`，`Flux`发布的数据`数据流`可以被一个`错误(error)`或`完成(completion)`信号终止，也可以被`取消`信号取消

### Mono

`Mono` 是一个特殊的`Flux`， 它最多发出一个元素，然后终止于一个 `onComplete` 信号或一个 `onError` 信号

## Flux

### 创建

#### `just`

直接使用元素创建`Flux`, `just`直接在创建`Flux`时就拿到数据，之后有谁订阅它，就重新发送数据给订阅者

如果给`just`传入的数据是一个 HTTP 调用的结果，那么在初始化`just`的时候会进行唯一的一次网络调用

```java
        Flux<String> stringFlux = Flux.just("a");
        
        Flux<Integer> integerFlux = Flux.just(1, 2);
```

#### `from`、`fromArray`、`fromIterable`、`fromStream`

从其他数据源创建

```java
        // 从其他Flux创建
        Flux<String> from = Flux.from(stringFlux);
        // 从数组创建
        Flux<String> fromArray = Flux.fromArray(new String[]{"1", "2", "3"});

        List<String> stringList = List.of("a", "b", "c");
        // 从迭代对象创建
        Flux<String> fromIterable = Flux.fromIterable(stringList);
        // 从Stream创建
        Flux.fromStream(stringList.stream());
```

#### `create`

使用`FluxSink `对象创建，支持同步和异步的消息产生，并且可以在一次调用中产生多个元素,可以用于 `push` 或 `pull` 模式

```java
        Flux.create((sink) -> {
            sink.next("1");
            // 这里发出了complete信号，所有2不会发给订阅者
            sink.complete();
            sink.next("2");
        }).subscribe(System.out::println);
```

#### `generate`

以 `同步的`， `逐个地` 产生值的方法

```java
      Flux.generate(synchronousSink -> {
            // 只能调用一次next 
            synchronousSink.next("a");
            synchronousSink.complete();
        });


        // 基于状态值的generate,
        // 返回值作为一个新的状态值用于下一次调用
        // 直到接收到complete信号才会停止generate
        Flux.generate(
                () -> 0,
                (state, synchronousSink) -> {
                    synchronousSink.next(state);
                    if (state == 20) {
                        // 发送complete信息，停止generate
                        synchronousSink.complete();
                    }
                    return state + 1;
                });
```

#### `range`

产生从起始值递增的那个整数

```java
// 第一个参数是 range 的开始，第二个参数是要生成的元素个数
Flux<Integer> range = Flux.range(2, 10);
```

 #### `interval`

周期性生成从0开始的的Long。周期从`delay`之后启动，每隔period`时间`返回一个加1后的Long

~~~java
        // 延迟30秒之后，每隔1秒产生从0开始递增加1的数据流
        Flux.interval(Duration.ofSeconds(30), Duration.ofSeconds(1));
~~~

>`interval方法返回的Flux运行在另外的线程中，main线程需要休眠或者阻塞之后才能看到周期性的输出

#### `defer`

延迟提供发布者，只有在被订阅的时候才去构造`Flux`，每次被订阅的时候都会重新构造`Flux`

```java
   public void test(){
        Flux<String> defer = Flux.defer(() -> Flux.fromIterable(list()));
        System.out.println("defer创建Flux");
        defer.subscribe();

        System.out.println("****************");
        Flux<String> iterable = Flux.fromIterable(list());
        System.out.println("fromIterable创建Flux");
        iterable.subscribe();
    }

    public List<String> list(){
        System.out.println("创建list");
        return List.of("1", "2");
    }

/**
    defer创建Flux
    创建list
     ****************
     创建list
     fromIterable创建Flux
**/
```

#### `push`

类似于`create` 

```java
        Flux<String> push = Flux.push(emitter -> {
            emitter.next("a");
            emitter.next("b");
            emitter.complete();
            emitter.next("c");
        });
```

#### `empty`

创建一个空的Flux

```java
Flux<Object> empty = Flux.empty();
```

### 订阅

`Flux`被订阅后会将数据逐个发给`订阅者`，订阅者通过`subscribe`方法订阅消费`Flux`发来的数据

```java
// 订阅并触发
subscribe(); 

// 对每一个生成的元素进行消费
subscribe(Consumer<? super T> consumer); 

// 对正常元素进行消费，对产生的错误进行响应
subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer); 

// 对正常元素和错误均有响应，还定义了消费完成后的回调
subscribe(Consumer<? super T> consumer, Consumer<? super Throwable> errorConsumer,
          Runnable completeConsumer); 
```

### 合并`Flux`

#### `concat`

按照首位相连的方式合并两个`Flux`，并返回一个新的`Flux`

```java
        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> stringFlux1 = Flux.just("1", "2", "3");
        Flux<String> concat = Flux.concat(stringFlux, stringFlux1);
```

#### `merge`

按照`Flux`中数据出现的顺序合并两个`Flux`，并返回一个新的`FLux`

```java
       Flux<String> interval = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(3))
                .map(l -> l + "a");
       Flux<Long> interval1 = Flux.interval(Duration.ofSeconds(2));

       Flux<Object> merge = Flux.merge(interval, interval1);
```

#### `combineLatest`

把所有源中最新产生的元素合并成一个新元素下发。只要其中任何一个源中产生了新元素，合并操作就会执行一次，然后下发新产生的元素

```java
        Flux<String> interval = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(3))
                .map(l -> l + "a");
        Flux<Long> interval1 = Flux.interval(Duration.ofSeconds(2));

        Flux<List<Object>> combineLatest = Flux.combineLatest(str -> List.of(str), interval, interval1);
```

#### `zip`

按照所有源中元素出现的顺序，将所有源的元素一比一组合成元组

```java
	Flux<Tuple2<String, String>> zip = Flux.zip(stringFlux, stringFlux1);
```

#### `startWith`

在`Flux`的开头添加元素，返回一个新的`Flux`

```java
        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> newStringFlux = stringFlux.startWith("1", "2");
```

#### `concatWithValues`

在`Flux`的末尾添加元素，返回一个新的`Flux`

```java
        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> concatWithValues = stringFlux.concatWithValues("1", "2");
```

### 转换

#### `map`

将`Flux`中的元素按照`map`中定义的规则进行转换

```java
        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<char[]> map = stringFlux.map(str -> str.toCharArray());
```

#### `flatMap`

将`Flux`中的元素按照`flatMap`中定义的规则进行转换, `flatMap`中函数的返回值是一个`Publisher`，`flatMap`会将多个`Publisher`合并展开其中的元素

```java
 	Flux<String> stringFlux = Flux.just("hello", "world");
       Flux<String> flatMap = stringFlux.flatMap(str -> Flux.fromArray(str.split("")));
```

#### `handle`

将`Flux`中的元素按照`handle`中定义的规则进行转换, `handle`的参数是一个`BiConsumer`

```java
	Flux<String[]> handle = stringFlux.handle((str, sink) -> sink.next(str.split("")));
```

#### `toIterable`

将`Flux`转成迭代器

```java
         Flux<String> stringFlux = Flux.just("a", "b", "c", "d", "e");
         Iterable<String> strings = stringFlux.toIterable();
```

#### `toStream`

将`Flux`转成`Stream`

```java
	Stream<String> stringStream = stringFlux.toStream();
```

#### `collectList`

将`Flux`中的元素放入list中，并用`Mono`包裹返回

```java
	Mono<List<String>> listMono = stringFlux.collectList();
	// 获取Mono中的元素
	List<String> block = listMono.block();
```

#### `collectMap`

将`Flux`中的元素放入map中，并用`Mono`包裹返回

```java
        Mono<Map<Integer, Integer>> mapMono = Flux.range(1, 10).collectMap(i -> i % 3);
        Map<Integer, Integer> map = mapMono.block();
```

#### `collect`

将`Flux`中的元素放入`输入的集合类型`(collect中的第一个参数)中，并用`Mono`包裹返回

```java
	Mono<ArrayList<String>> collect = stringFlux.collect(ArrayList::new, (a, b) -> a.add(b));
```

### 分组处理

#### `buffer`

将`Flux`中的元素按个数进行分组放入一个集合中

```java
	Flux<String> stringFlux = Flux.just("a", "b", "c", "d", "e");
        Flux<List<String>> buffer = stringFlux.buffer(2);
```

#### `window`

根据个数、时间等条件，或能够定义边界的发布者， 把`Flux`中的元素分组放入一个`Flux`中

```java
	Flux<Flux<String>> window = stringFlux.window(2);
```

#### `groupBy`

将`Flux`中的元素按自定义的规则进行分组放入一个`GroupedFlux`中

```java
        Flux<GroupedFlux<String, Integer>> groupedFluxFlux = Flux.range(1, 10)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd");
```

### 过滤

#### `filter`

过滤`Flux`中的元素，保留`filter`断言为真的元素

```java
	Flux<Integer> filter = Flux.range(1, 10).filter(i -> i % 3 == 0);
```

#### `filterWhen`

根据自定义判断函数，异步地进行判断, 

```java
	Flux<Integer> integerFlux = Flux.range(1, 10).filterWhen(i -> Flux.just(i % 2 == 0));
```

#### `ofType`

根据元素`类型`过滤

```java
	Flux<String> stringFlux = Flux.just("a", "b", 1, 2).ofType(String.class);
```

#### `distinct`

去除重复元素

```java
	Flux<String> distinct = Flux.just("a", "b", "a", "c").distinct();
```

#### `distinctUntilChanged`

去除连续重复的元素

```java
	Flux<String> distinctUntilChanged = Flux.just("a", "b", "a", "c", "c", "1", "a").distinctUntilChanged();
```

#### `take`

按个数、时间等条件保留前几个元素

```java
	Flux<String> take = Flux.just("a", "b", "a", "c").take(2);
```

#### `takeLast`

保留指定个数的后几个元素

```java
	Flux<String> takeLast = Flux.just("a", "b", "a", "c").takeLast(3);
```

#### `takeUntil`

保留直到断言为真的前几个元素

```java
	Flux<String> takeUntil = Flux.just("a", "b", "a", "c", "c", "1", "a").takeUntil(str -> str.equals("1"));
```

#### `takeWhile`

和`takeUntil`相反

```java
	Flux<String> takeWhile = Flux.just("a", "b", "a", "c", "c", "1", "a").takeWhile(str -> !str.equals("1"));
```

#### `skip`

按个数、时间等条件跳过前几个元素

```java
	Flux<String> skip = Flux.just("a", "b", "a", "c", "c", "1", "a").skip(3);
```

#### `skipLast`

#### `skipUntil`

#### `skipWhile`

### 错误处理

#### `error`

创建一个`error`的`Flux`

```java
	Flux<String> error = Flux.error(new RuntimeException("哈哈哈哈哈哈"));
```

#### `onErrorReturn`

捕获异常然后返回缺省值

```java
	Flux<String> just = Flux.just("a", "b", "c");
	Flux<String> error = Flux.error(new RuntimeException("哈哈哈哈哈哈"));

	just.concatWith(error).onErrorReturn("error");
```

#### `onErrorMap`

捕获异常，处理后重新抛出一个异常

```java
	just.concatWith(error).onErrorMap(err -> new RuntimeException("出错了"))
```

#### `onErrorResume`

捕获异常，将异常转换成正常的元素

```java
	just.concatWith(error).concatWithValues("1", "2").onErrorResume(str -> Flux.just(str.getMessage()));
```

#### `onErrorContinue`

出现错误跳过错误，使用原数据继续执行

```java
	Flux.range(1, 20)
        	.map(i -> {
            	if ( i%3 == 0 ){
                	throw new RuntimeException("出错数字: " + i);
          	  	}
            	return i;
        	})
                 // 捕获异常并打印异常信息，也可以不打印
       		 .onErrorContinue((err, obj) -> System.out.println(err.getMessage()))
        	  .subscribe(System.out::println);
```

### 得到通知（完成、出现错误、取消订阅等）信号后执行一些操作

#### `doOnNext`

获取下一个元素的时候触发，不改变源`Flux`

```java
	Flux<String> stringFlux = Flux.just("a", "b", "a", "c");
	stringFlux.doOnNext(str -> System.out.println("str: " + str))
        	.subscribe(System.out::println);
```

#### `doOnComplete`

订阅者消费完成后触发

```java
	stringFlux.doOnComplete(() -> System.out.println("doOnComplete"))
        	.subscribe(System.out::println);
```

#### `doOnCancel`

取消订阅时触发

```java
stringFlux.doOnCancel(() -> System.out.println("doOnCancel"))
       // 订阅时触发
        .doOnSubscribe(subscription -> {
            System.out.println("doOnSubscribe");
            // 取消订阅
            subscription.cancel();
        }).subscribe(System.out::println);
```

#### `doOnSubscribe`

订阅时触发

#### `doOnError`

出现错误时触发

```java
	stringFlux.concatWith(Flux.error(new RuntimeException("出错了")))
        	.doOnError(err -> System.out.println(err.getMessage()))
       		.subscribe(System.out::println);
```

#### `doOnRequest`

请求数据时触发

```java
        stringFlux.doOnCancel(() -> System.out.println("doOnCancel"))
                // 请求数据时触发，l为请求数
                .doOnRequest(l -> System.out.println(l))
                .doOnSubscribe(subscription -> {
                    System.out.println("doOnSubscribe");
                    // 设置请求数
                    subscription.request(4);
                }).subscribe(System.out::println);
```

#### `doOnEach`

所有类型的信号都会触发

#### `doFinally`

所有结束的情况（完成complete、错误error、取消cancel）都会触发

### Context

`Context` 是一个类似于 `Map`（这种数据结构）的接口：它存储键值（key-value）对，它作用于一个 `Flux` 或一个 `Mono` 上，而不是应用于一个线程（`Thread`）

- key 和 value 都是 `Object` 类型，所以 `Context` 可以包含任意数量的任意对象
- `Context` 是 **不可变的（immutable）**
- `put(Object key, Object value)` 方法来存储一个键值对，返回一个新的 `Context` 对象
- `putAll(Context)`方法将两个 context 合并为一个新的 context
- `hasKey(Object key)` 方法检查一个 key 是否已经存在
- `getOrDefault(Object key, T defaultValue)` 方法取回 key 对应的值（类型转换为 `T`）， 或在找不到这个 key 的情况下返回一个默认值
- `getOrEmpty(Object key)` 来得到一个 `Optional<T>`（context 会尝试将值转换为 `T`）
- `delete(Object key)` 来删除 key 关联的值，并返回一个新的 `Context`

>  **创建一个** `Context` 时，你可以用静态方法 `Context.of` 预先存储最多 5 个键值对。 它接受 2, 4, 6, 8 或 10 个 `Object` 对象，两两一对作为键值对添加到 `Context`。  你也可以用 `Context.empty()` 方法来创建一个空的 `Context`。

```java
Flux<String> stringFlux = Flux.just("a", "b", "c")
        // 获取Context
        .transformDeferredContextual((flux, ctx) -> {
            // 从context中取数据
            String ctxOrDefault = ctx.getOrDefault("key", "default");
            return flux.flatMap(str -> Flux.just(str + " " + ctxOrDefault));
        });

// 向Context存入数据
stringFlux.contextWrite(Context.of("key", "World")).subscribe(System.out::println);
System.out.println("******************************************************");

// 多次绑定相同的key, 离序列近的会覆盖离序列远的，因为context时从下游向上游传递
stringFlux.contextWrite(Context.of("key", "1234")).contextWrite(Context.of("key", "AAAA"))
                .subscribe(System.out::println);
```

> **写入**与**读取** `Context` 的**相对位置**很重要：因为 `Context` 是不可变的，它的内容只能被`上游的操作符`看到
>
> 如果多次对 `Context` 中的同一个 key 赋值的话，读取 `Context` 的操作符只能拿到下游最近的一次写入的值

### 调度器 `Scheduler`

`Reactor`执行模式以及执行过程取决于所使用的`Scheduler`。`Scheduler`是一个拥有广泛实现类的抽象接口`Schedulers`类提供的静态方法用于达成如下的执行环境：

- 当前线程（`Schedulers.immediate()`）
- 可重用的单线程（`Schedulers.single()`）。注意，这个方法对所有调用者都提供同一个线程来使用， 直到该调度器（Scheduler）被废弃。如果你想使用同一个线程，就对每一个调用使用 `Schedulers.newSingle()`。
- 弹性线程池`Schedulers.elastic()`。它根据需要创建一个线程池，重用空闲线程。线程池如果空闲时间过长 （默认为 60s）就会被废弃。`Schedulers.elastic()` 能够方便地给一个阻塞的任务分配它自己的线程，从而不会妨碍其他任务和资源
- 固定大小线程池（`Schedulers.parallel()`）。所创建线程池的大小与 CPU 个数等同。

此外，你还可以使用 `Schedulers.fromExecutorService(ExecutorService)` 基于现有的 `ExecutorService` 创建 `Scheduler`。（虽然不太建议，不过你也可以使用 `Executor` 来创建）。你也可以使用 `newXXX` 方法来创建不同的调度器。比如 `Schedulers.newElastic(yourScheduleName)` 创建一个新的名为 `yourScheduleName` 的弹性调度器。

```java
Flux<Integer> integerFlux = Flux.range(1, 10000).publishOn(Schedulers.parallel());

integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
System.out.println("***********1************");
integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
System.out.println("***********2************");
integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));

// 等待程序结束，以便观测效果
CountDownLatch countDownLatch = new CountDownLatch(10);
countDownLatch.await();
```

#### 调整调度器 `Scheduler`

Reactor 提供`publishOn`和`subscribeOn`方法在响应式链中调整调度器`Scheduler`。它们都接受一个`Scheduler`作为参数，来替换调度器

- `publishOn` 的用法和处于订阅链（subscriber chain）中的其他操作符一样。它将上游信号传给下游，同时执行指定的调度器 `Scheduler` 的某个工作线程上的回调。 它会**改变后续的操作符的执行所在线程**（直到下一个 `publishOn` 出现在这个链上）。
- `subscribeOn` 用于订阅（subscription）过程，作用于那个向上的订阅链（发布者在被订阅时才激活，订阅的传递方向是向上游的）。所以，无论你把 `subscribeOn` 至于操作链的什么位置， **它都会影响到源头的线程执行环境（context）**。 但是，它不会影响到后续的 `publishOn`，后者仍能够切换其后操作符的线程执行环境

```java
        Flux<Integer> integerFlux1 = Flux.range(1, 10000).publishOn(Schedulers.single());

        integerFlux1.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
        System.out.println("***********1************");
        integerFlux1.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
        System.out.println("***********2************");
        integerFlux1.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));

	// 等待程序结束，以便观测效果
        CountDownLatch countDownLatch = new CountDownLatch(10);
        countDownLatch.await();
```

