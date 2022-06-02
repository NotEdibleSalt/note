

`WebFlux`是一个基于`Reactor`的`异步、非阻塞`的web框架。`WebFlux`可以运行在`Netty`, `Undertow`和`Servlet 3.1以上`的容器中

> `WebFlux`并不能使接口的响应时间缩短，它仅仅能够提升吞吐量和伸缩性

`WebFlux`提供了两种使用方式：注解式（[Annotated Controllers](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-controller)）和 函数式（[Functional Endpoints](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-fn)）

- 注解式：和`SpringMvc`的注解一致，使用`RestController`、`GetMapping`、`PostMapping`等注解
- 函数式：基于Lambda表达式，使用Function描述请求端点

本文演示基于注解的使用方式

## 示例

1. 新建数据表

   ~~~sql
   CREATE TABLE `user` (
     `id` int(64) NOT NULL AUTO_INCREMENT,
     `name` varchar(255) DEFAULT NULL,
     `age` int(4) DEFAULT NULL,
     `sex` varchar(2) DEFAULT NULL,
     PRIMARY KEY (`id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
   ~~~

2. 引入依赖

   ```xml
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-webflux</artifactId>
           </dependency>
           <dependency>
               <groupId>dev.miku</groupId>
               <artifactId>r2dbc-mysql</artifactId>
               <version>0.8.2.RELEASE</version>
           </dependency>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-data-r2dbc</artifactId>
           </dependency>
   ```

3. 编写配置文件

   ```yaml
   server:
     port: 8099
   spring:
     data:
       r2dbc:
         repositories:
           enabled: true
   
     r2dbc:
       url: r2dbc:mysql://**********:****/user?useUnicode=true&characterEncoding=UTF-8&useSSL=false
       username: ****
       password: ****
       pool:
         enabled: true
         max-size: 10
         initial-size: 10
         validation-query: select 1
   ```

5. 创建实体类

   ```java
   @Data
   public class User {
   
       private Integer id;
   
       private String name;
   
       private Integer age;
   
       private String sex;
   }
   ```

7. 创建Repository

   ```java
   public interface UserRepository extends R2dbcRepository<User, Integer> {
   }
   ```

6. 创建Controller

   ```java
    @RestController
    public class UserController {
    
        @Resource
        private UserRepository userRepository;
    
        @PostMapping("user")
        public Mono<Integer> createUser(@RequestBody User user){
    
            Mono<User> userMono = userRepository.save(user);
            return userMono.map(User::getId);
        }
    
        @GetMapping("user/{id}")
        public Mono<User> getUserById(@PathVariable("id") Integer id){
    
            Mono<User> userMono = userRepository.findById(id);
            return userMono;
        }
    
        @PutMapping("user")
        public Mono<String> updateUser(@RequestBody User user){
    
            userRepository.save(user);
            return Mono.just("修改成功");
        }
    
        @DeleteMapping("user/{id}")
        public Mono<String> delUser(@PathVariable("id") Integer id){
    
            userRepository.deleteById(id);
            return Mono.just("删除成功");
        }
    
    }
   ```
