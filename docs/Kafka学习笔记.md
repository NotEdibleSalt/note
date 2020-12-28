# Kafka学习笔记

## 官方简介

### 定义

Kafka是 一个**分布式流处理平台**，具有以下三种特性:

1. 可以让你发布和订阅流式的记录。这一方面与消息队列或者企业消息系统类似。        
2. 可以储存流式的记录，并且有较好的容错性。        
3. 可以在流式记录产生时就进行处理。    

### 应用场景

1. 构造**实时流数据管道**，它可以在系统或应用之间可靠地获取数据。 (相当于消息队列)        
2. 构建**实时流式应用程序**，对这些流数据进行转换或者影响。(就是流处理，通过kafka stream topic和topic之间内部进行转换)

## 相关概念

- `Broker`
   部署了Kafka实例的服务器节点,，多个Broker组成Kafka集群。
- `Topic`
  Topic 就是数据主题，Kafka 通过 *topic* 对存储的流数据进行分类，相同类型的数据存储在同一个Topic中。一个topic可以拥有一个或者多个消费者来订阅它的数据。
- `Partition`
  每个topic可以有一个或多个partition（分区）。分区是在物理层面上的，不同的分区对应着不同的数据文件。Kafka使用分区支持物理上的并发写入和读取，从而大大提高了吞吐量。
- `Record`
   写入Kafka中并可以被读取的消息记录。每个record包含了key、value和timestamp。
- `Producer`
   生产者，用来向Kafka中发送数据（record）。
- `Consumer`
   消费者，用来读取Kafka中的数据（record）。
- `Consumer Group`
  一个消费者组可以包含一个或多个消费者。消费者组中的消费者是竞争关系，即同一个Topic中的一条数据（record）只能被消费者组中的一个消费者消费。

## Spring Boot整合Kafka

1. 引入依赖

   ~~~xml
   <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-starter-stream-kafka</artifactId>
               <version>3.1.0</version>
   </dependency>
   ~~~

2. 配置yml

   ~~~yaml
   spring:
     cloud:
       stream:
         kafka:
           binder:
             auto-create-topics: true   # 自动创建topics
             brokers: ***.****.***.***:9092
         bindings:
           logP-out-0:    # 对用在ProducersConfig中的生产函数logP
             destination: log  # logP将数据发送的topic
             contentType: application/json
           logC-in-0:    # 对用在ConsumersConfig中的生产函数logC
             destination: log
             group: log_group
           addAgeC-in-0:
             destination: addAge
             group: addAge_group
         function:
           definition: logP;logC;addAgeC  # 指定对应的函数为Spring Cloud Stream中的生产消费通道
   ~~~

3. 编写生产者

   方式1

   ~~~java
   @Configuration
   public class ProducersConfig {
   
       private BlockingQueue<Person> unbounded = new LinkedBlockingQueue<>();
       
       /**
        * 对应yml中配置的logP-out-0通道，即topic log
        * @return java.util.function.Supplier<com.example.kafka.entity.Person>
        * @Date 2020-12-27
        **/
       @Bean
       public Supplier<Person> logP(){
           return () -> unbounded.poll();
       }
   
       /**
        * 调用本方法向log topic发送消息 
        * 
        * @param person: 
        * @return void
        * @Date 2020-12-27
        **/
       public void log(Person person){
           unbounded.offer(person);
       }
       
   }
   ~~~

   方式2

   ~~~java
   @RestController
   public class UserController {
   
       @Autowired
       private StreamBridge streamBridge;
   
       @PostMapping("/addAge")
       public boolean addAge(@RequestBody Person person){
   
           person.setAge(RandomUtil.randomInt(10, 90));
           person.setSuccess(RandomUtil.randomBoolean());
           person.setBirthday(new Date());
   
           // 通过streamBridge直接对应的topic发送消息
           return streamBridge.send("addAge", person);
       }
       
   }
   ~~~

4. 编写消费者

   ~~~java
   @Configuration
   public class ConsumersConfig {
   
       /**
        * 对应yml中配置的logC-in-0通道，即topic log。
        * 消费topic log中的消息
        *
        * @return java.util.function.Consumer<com.example.kafka.entity.Person>
        * @Date 2020-12-27
        **/
       @Bean
       public Consumer<Person> logC() {
   
           return person -> {
               System.out.println("Received: " + person);
           };
       }
   
       /**
        * 对应yml中配置的addAgeC-in-0通道，即topic addAge。
        * 消费topic addAge中的消息
        *
        * @return java.util.function.Consumer<com.example.kafka.entity.Person>
        * @Date 2020-12-27
        **/
       @Bean
       public Consumer<Person> addAgeC(){
   
           return person -> {
   
               person.setAge(person.getAge() + 10);
               System.out.println("Consumer addAge: " + person.toString());
           };
       }
   }
   ~~~

5. 发送消息

   ~~~java
   @RestController
   public class UserController {
   
       @Autowired
       private StreamBridge streamBridge;
   
       @Autowired
       private ProducersConfig producersConfig;
   
       @PostMapping("/log")
       public void log(@RequestBody Person person){
   
           producersConfig.log(person);
       }
   
       @PostMapping("/addAge")
       public boolean addAge(@RequestBody Person person){
   
           person.setAge(RandomUtil.randomInt(10, 90));
           person.setSuccess(RandomUtil.randomBoolean());
           person.setBirthday(new Date());
   
           System.out.println("Producer addAge: " + person.toString());
           return streamBridge.send("addAge", person);
       }
   
   }
   ~~~

   



