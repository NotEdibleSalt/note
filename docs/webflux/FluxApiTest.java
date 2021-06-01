package com.ss.secuity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @Author: xush
 * @Date: 2021-5-28
 * @Version: v1.0
 */
@Slf4j
public class FluxApiTest {

    @Test
    public void just() throws InterruptedException {

        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> stringFlux1 = Flux.just("1", "2", "3");

        Flux<Integer> integerFlux = Flux.just(1, 2);

        List<String> stringList = List.of("a", "b", "c");
        // 从迭代对象创建
        Flux<String> fromIterable = Flux.fromIterable(stringList);
        // 从Stream创建
        Flux.fromStream(stringList.stream());

        // 从其他Flux创建
        Flux<String> from = Flux.from(stringFlux);
        // 从数组创建
        Flux<String> fromArray = Flux.fromArray(new String[]{"1", "2", "3"});

        Flux<Object> create = Flux.create((sink) -> {
            sink.next("1");
            // 这里发出了complete信号，所有2不会发给订阅者
            sink.complete();
            sink.next("2");
        });

        Flux.generate(synchronousSink -> {

            synchronousSink.next("a");
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

        Flux<Integer> range = Flux.range(2, 10);

        // 延迟30秒之后，每隔1秒产生从0开始递增加1的数据流
        Flux.interval(Duration.ofSeconds(3), Duration.ofSeconds(1));
        //防止程序过早退出，放一个CountDownLatch拦住
        CountDownLatch latch = new CountDownLatch(1);
        latch.await();


        int i = 1;
        Flux<Integer> just = Flux.just(i);
        System.out.println("*****************");
        just.subscribe(System.out::println);

        // defer产生的Flux在每次订阅消费的时候都会重新去请求发布者最新的数据
        Flux<Integer> defer = Flux.defer(() -> just);
        i = i + 10;
        defer.subscribe(System.out::println);
        i = i + 10;
        defer.subscribe(System.out::println);

        System.out.println("*****************");
        just.subscribe(System.out::println);

        Flux.firstWithValue(stringFlux).subscribe(System.out::println);
        Flux<Object> empty = Flux.empty();

        Flux<String> defer1 = Flux.defer(() -> Flux.fromIterable(list()));
        System.out.println("defer创建Flux");
        defer1.subscribe();

        System.out.println("****************");
        Flux<String> iterable = Flux.fromIterable(list());
        System.out.println("fromIterable创建Flux");
        iterable.subscribe();
    }

    public List<String> list() {
        System.out.println("创建list");
        return List.of("1", "2");
    }

    @Test
    public void push() {

        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> stringFlux1 = Flux.just("1", "2", "3");
        Flux<String> concat = Flux.concat(stringFlux, stringFlux1);
        concat.subscribe(System.out::println);

        System.out.println("********************");

        Flux<String> merge = Flux.merge(stringFlux, stringFlux1);
        merge.subscribe(System.out::println);
    }

    @Test
    public void merge() throws InterruptedException {

        Flux<String> stringFlux = Flux.just("a", "b");
        Flux<String> stringFlux1 = Flux.just("1", "2", "3");

        Flux<String> concat = Flux.concat(stringFlux, stringFlux1);

        Flux<String> interval = Flux.interval(Duration.ofSeconds(1), Duration.ofSeconds(3))
                .map(l -> l + "a");
        Flux<Long> interval1 = Flux.interval(Duration.ofSeconds(2));

        Flux<Object> merge = Flux.merge(interval, interval1);
        Flux<List<Object>> combineLatest = Flux.combineLatest(str -> List.of(str), interval, interval1);

        Flux.firstWithSignal(interval, interval1);

        CountDownLatch latch = new CountDownLatch(1);
        latch.await();

        Flux<Tuple2<String, String>> zip = Flux.zip(stringFlux, stringFlux1);

        Flux<String> concatWithValues = stringFlux.concatWithValues("1", "2");
    }

    @Test
    public void conversionOperation() {

        Flux<String> stringFlux1 = Flux.just("a", "b");
        Flux<char[]> map = stringFlux1.map(str -> str.toCharArray());

        Flux<String> stringFlux = Flux.just("hello", "world");
        Flux<String> flatMap = stringFlux.flatMap(str -> Flux.fromArray(str.split("")));
//        flatMap.subscribe(System.out::println);

        Flux<String[]> handle = stringFlux.handle((str, sink) -> sink.next(str.split("")));
    }

    @Test
    public void grouping() {

        Flux<String> stringFlux = Flux.just("a", "b", "c", "d", "e");
        Flux<List<String>> buffer = stringFlux.buffer(2);

        Flux<Flux<String>> window = stringFlux.window(2);
//        window.log().subscribe(System.out::println);

//        window.flatMap(f -> f).subscribe(System.out::println);

        Flux<GroupedFlux<String, Integer>> groupedFluxFlux = Flux.range(1, 10)
                .groupBy(i -> i % 2 == 0 ? "even" : "odd");

        groupedFluxFlux.flatMap(f -> Flux.just(f.key())).subscribe(System.out::println);
//        groupedFluxFlux.filter()

    }

    @Test
    public void toCollection() {

        Flux<String> stringFlux = Flux.just("a", "b", "c", "d", "e", "a", "b");

        Iterable<String> strings = stringFlux.toIterable();
        Stream<String> stringStream = stringFlux.toStream();

        Mono<List<String>> listMono = stringFlux.collectList();
        List<String> list = listMono.block();

        Mono<Map<Integer, Integer>> mapMono = Flux.range(1, 10).collectMap(i -> i % 3);
        Map<Integer, Integer> map = mapMono.block();

        Mono<ArrayList<String>> collect = stringFlux.collect(ArrayList::new, (a, b) -> a.add(b));
    }

    @Test
    public void filter() {

        Flux<Integer> filter = Flux.range(1, 10).filter(i -> i % 3 == 0);

        Flux<Integer> integerFlux = Flux.range(1, 10).filterWhen(i -> Flux.just(i % 2 == 0));

        Flux<String> stringFlux = Flux.just("a", "b", 1, 2).ofType(String.class);

        Flux<String> distinct = Flux.just("a", "b", "a", "c").distinct();

        Flux<String> distinctUntilChanged = Flux.just("a", "b", "a", "c", "c", "1", "a").distinctUntilChanged();

        Flux<String> take = Flux.just("a", "b", "a", "c").take(2);
        Flux<String> takeLast = Flux.just("a", "b", "a", "c").takeLast(3);

        Flux<String> takeUntil = Flux.just("a", "b", "a", "c", "c", "1", "a").takeUntil(str -> str.equals("1"));
        Flux<String> takeWhile = Flux.just("a", "b", "a", "c", "c", "1", "a").takeWhile(str -> !str.equals("1"));

        Flux<String> skip = Flux.just("a", "b", "a", "c", "c", "1", "a").skip(3);

        Flux.just("a", "b", "a", "c", "c", "1", "a");

        skip.subscribe(System.out::println);
    }

    @Test
    public void errorHandle() {

        Flux<String> just = Flux.just("a", "b", "c");
        Flux<String> error = Flux.error(new RuntimeException("哈哈哈哈哈哈"));
//
//        just.concatWith(error).concatWithValues("1", "2")
//                .onErrorReturn("error");
//
//        Flux<String> withValues = just.concatWithValues("1", "2").concatWith(error);
//        withValues.onErrorResume(str -> Flux.just(str.getMessage()))
//                .subscribe(log::info);

//        just.concatWith(error).concatWithValues("1", "2")
//                .onErrorMap(err -> new RuntimeException("出错了"))
//                .doFinally(str -> System.out.println("doFinally"));

//        just.concatWith(error).concatWithValues("1", "2")
//                .onErrorContinue((err, obj) -> log.error("23"))
//                .subscribe(log::info);

        Flux<Integer> map = Flux.range(1, 20)
                .map(i -> {
                    if (i % 3 == 0) {
                        throw new RuntimeException("出错数字: " + i);
                    }
                    return i;
                });
//        map.onErrorContinue((err, obj) -> System.out.println(err.getMessage()));

        map.doOnError(err -> System.out.println("aaaa"))
                .subscribe(System.out::println);
    }

    @Test
    public void respondToNotificationSignals() {

        Flux<String> stringFlux = Flux.just("a", "b", "a", "c");
//        stringFlux.doOnNext(str -> System.out.println("str: " + str))
//                .subscribe(System.out::println);
//
//        stringFlux.doOnComplete(() -> System.out.println("doOnComplete"))
//                .subscribe(System.out::println);

//        stringFlux.doOnCancel(() -> System.out.println("doOnCancel"))
//                .doOnRequest(l -> System.out.println(l))
//                .doOnSubscribe(subscription -> {
//                    System.out.println("doOnSubscribe");
//                    subscription.request(4);
////                    subscription.cancel();
//                }).subscribe(System.out::println);

//        stringFlux.concatWith(Flux.error(new RuntimeException("出错了")))
//                .doOnError(err -> System.out.println(err.getMessage()))
//                .subscribe(System.out::println);


        stringFlux.doOnCancel(() -> System.out.println("doOnCancel"))
                .doOnRequest(l -> System.out.println(l))
                .doOnEach(stringSignal -> {
                    System.out.println("doOnEach");
                    System.out.println(stringSignal.get());
                })
                .doOnSubscribe(subscription -> {
                    System.out.println("doOnSubscribe");
                    subscription.request(4);
                    subscription.cancel();
                }).subscribe(System.out::println);
    }

    @Test
    public void contextTest() {

        String key = "message";
//        Mono<String> r = Mono.just("Hello")
//                .flatMap(s -> Mono.deferContextual(contextView -> Mono.just(contextView))
//                        .map( ctx -> s + " " + ctx.get(key)))
//                .contextWrite(ctx -> ctx.put(key, "Reactor"))
//                .contextWrite(ctx -> ctx.put(key, "World"));
//        r.subscribe(System.out::println);

        Flux<String> stringFlux = Flux.just("a", "b", "c")
                .transformDeferredContextual((flux, ctx) -> {
                    String ctxOrDefault = ctx.getOrDefault("key", "default");
                    return flux.flatMap(str -> Flux.just(str + " " + ctxOrDefault));
                });

        stringFlux.contextWrite(Context.of("key", "World")).subscribe(System.out::println);
        System.out.println("******************************************************");
        stringFlux.contextWrite(Context.of("key", "1234")).contextWrite(Context.of("key", "AAAA"))
                .subscribe(System.out::println);
    }

    @Test
    public void transformTest(){

        Function<Flux<String>, Flux<String>> filterAndMap =
                f -> f.filter(color -> !color.equals("orange")).map(String::toUpperCase);

        Flux.fromIterable(Arrays.asList("blue", "green", "orange", "purple"))
                .doOnNext(System.out::println)
                .transform(filterAndMap)
                .subscribe(d -> System.out.println("Subscriber to Transformed MapAndFilter: "+ d));
    }

    @Test
    public void s() throws InterruptedException {

//        Flux<Integer> integerFlux = Flux.range(1, 10000).publishOn(Schedulers.parallel());

//        integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
//        System.out.println("***********1************");
//        integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
//        System.out.println("***********2************");
//        integerFlux.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
//        CountDownLatch countDownLatch = new CountDownLatch(10);
//        countDownLatch.await();

//        Hooks.onOperatorDebug();
        Flux<String> integerFlux1 = Flux.range(1, 10000)

                .map(i -> i + "a")
                .map(String::toUpperCase)
                .subscribeOn(Schedulers.parallel())
                ;

        integerFlux1.publishOn(Schedulers.single()).subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
//        System.out.println("***********1************");
//        integerFlux1.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
//        System.out.println("***********2************");
//        integerFlux1.subscribe(i -> System.out.println(Thread.currentThread().getName() + " " + i));
        CountDownLatch countDownLatch = new CountDownLatch(10);
        countDownLatch.await();

    }
}
