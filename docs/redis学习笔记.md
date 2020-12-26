# Redis学习笔记

## 简介

​		redis是远程字典服务「**R**emote **Di**ctionary **S**ervice」的缩写，是互联网技术领域使用最为广泛的存储中间件。常用作缓存，分布式锁等功能。

## 基础数据结构

​		既然redis是字典服务，那么必然需要提供唯一的标识(key)来标记资源。redis中所有的数据结构都是K, V键值的形式，其中K必须是字符串类型。不同数据结构之间的区别在于V结构的不同。

### 1. String(字符串)	<String, String>

​		所谓String类型即redis存储的值V也是String类型。Redis 的字符串是可以修改动态字符串，当字符串长度小于 1M 时，扩容都是加倍现有的空间，如果超过 1M，扩容时一次只会多扩 1M 的空间。字符串最大为 512M。

 #### 常用命令

1. set k v 	// 设置k的值为v

   ~~~shell
   > set name 张三
   OK
   ~~~

2. get k      // 获取k对应的值

   ~~~shell
   > get name
   张三
   ~~~

3. exists k   // 判断k存不存在，存在时返回1，不存在返回0

   ~~~shell
   > exists name
   1
   > exists age
   0
   ~~~

4. del k    // 删除k的值，适用于所有数据结构。k不存在时返回0，删除成功返回1

   ~~~shell
   > del name    
   1
   > get name    // 此时name已被删除，get得到的值为空
   null
   > del age
   0
   ~~~

5.  mset k1 v1 k2 v2 k3 v3 ...  // 批量设置

   ~~~shell
   > mset name 李四 age 20 // 设置name为李四，age为20
   OK
   > get name
   李四
   > get age
   20
   ~~~

6.  mget k1 k2 k3 ...    // 批量获取多个k所对应的值，当其中任意一个k不存在时报错

   ~~~shell
   > mget name age
   李四
   20
   > mget name age height
   Cannot read property '0' of null
   ~~~

7. expire k time   // 设置k的存活时间为time(time以秒为单位)

   ttl k           // 获取k的存活时间，-2：以失效即get的时候返回空，-1: 永不失效，正数为key的剩余存活时间

   ~~~shell
   > expire name 10
   1
   > ttl name
   8
   > ttl name
   4
   > ttl name
   -2
   > get name
   null
   ~~~

8. setex k  time v  // 设置k的值为v的同时设置存在时间为time(time以秒为单位)，相当于set + expire命令

   ~~~shell
   > setex  name 10 李四
   OK
   > ttl name
   7
   > ttl name
   -2
   ~~~

9. setnx k v  // 当k不存在时设置k的值为v

   ~~~shell
   > setnx name 李四
   1
   > setnx name 张三  // name已经存在，所以设置失败
   0
   > get name
   李四
   ~~~

10.  append k v  // 将v追加在原来的v后，如果k不存在则创建k并将值设置v, 返回v的长度

    ~~~shell
    > append name 哈哈哈
    15
    > get name
    李四哈哈哈
    > append  height 20
    2
    > get height
    20
    ~~~

11. incr k   // 将k对应的v加一，v必须是数字。如果k不存在则创建k,并将v设为1。返回加一后的结果

    ~~~shell
    > incr age
    21
    > get age
    21
    > incr dd
    1
    > get dd
    1
    ~~~

12.  incrby k value     //  将k对应的v加上指定的value, 返回相加后的结果。v必须为数字

    ~~~shell
    > incrby age 10
    31
    > get age
    31
    ~~~

13.  decr k  // 自减1 其余同incr
14. decrby k step // 减去指定值, 其余同incrby

### 2. hash(哈希)	<String, hashMap<String, String>>

​		hash型数据结构的v很像java中的hashMap类型，但是这个v的键值只能是String类型。

#### 常用命令

1. hset k field1 value1 field2 value2 ...     // 设置k的值为 field1 field2为子属性的键，返回值为子属性键个数。

   ~~~shell
   > hset people name 张三 age 18 height 180cm
   3
   ~~~

2. hmset k field1 value1 field2 value2 ...  // 同上

   ~~~shell
   > hmset people name 李四 age 20
   OK
   ~~~

3. hgetall k   // 获取k对应的所有子属性

   ~~~shell
   > hgetall people
   name
   李四
   age
   20
   height
   180cm
   ~~~

3. hvars k        // k对应的所有value

   ~~~shell
   > hvals people
   李四
   20
   180cm
   ~~~

   

4. hmget k field   // 获取k中指定field的值

   ~~~shell
   > hmget people name
   张三
   ~~~

5. hdel k field  // 删除k中指定field的值

   ~~~shell
   > hdel people height
   1
   > hgetall people
   name
   李四
   age
   20
   ~~~

6. hexists  k field   //  判断k中指定field存不存在，1: 存在，0: 不存在

   ~~~shell
   > hexists people height
   0
   > hexists people name
   1
   ~~~

7. hlen k   // 获取k中field的个数

   ~~~shell
   > hlen people
   2
   ~~~

8. hincrby k field value  // 将k中指定field对应的值加上value，需要field对应的值为数字，类似incrby

   ~~~shell
   > hincrby people age 10
   30
   ~~~

9.  hsetnx k field value    // //如果k中不存该field，就创建该field并将值设为value

   ~~~shell
   > hsetnx people height 200cm
   1
   > hsetnx people name 哈哈
   0
   > hgetall people
   name
   李四
   age
   20
   height
   200cm
   ~~~

### 3. list(列表)	<String, **queue**<**String**>>

​		list型数据结构的v很像java中的**queue**类型，可以中左边/右边进行push/pop操作，但是这个队列中的元素只能是String。

#### 常用命令

1. lpush k v1 v2 v3 ...   // 创建键为k的列表,并将v1 v2 v3等值从左边push(放入)其中

   ~~~shell
   > lpush list 1 小鸟 鲜花
   3
   ~~~

2.  lrange k start end   // 返回列表中指定范围内的元素，0: 第一位， -1：最后一位，-2：倒数第二位

   ~~~shell
   > lrange list 0 -1
   鲜花
   小鸟
   1
   ~~~

3.  rpush k v1 v2 v3 ...   // 创建键为k的列表,并将v1 v2 v3等值从右边push(放入)其中

   ~~~shell
   > rpush rlist 1 3 4 5
   4
   > lrange rlist 0 -1
   1
   3
   4
   5
   ~~~

4. ipop k   // 从左边弹出一个元素

   ~~~shell
   > lpop llist
   鲜花
   > lrange llist 0 -1
   鲜花
   小鸟
   > lpop rlist
   1
   ~~~

5. rpop k   // 从右边弹出一个元素

   ```shell
   > rpop llist
   1
   > lrange llist 0 -1
   小鸟
   ```

6. llen k   // 获取指定k的list列表的长度

   ~~~shell
   > llen llist
   1
   ~~~

7. lindex k index    // 获取指定下标的元素, 下标从0开始

   ~~~shell
   > lindex rlist 0
   3
   > lindex rlist 1
   4
   ~~~

   

8. ltrim k startIndex endIndex   // 保留指定区间的元素，将不再此区间的都去掉

   ~~~shell
   > lrange rlist 0 -1
   3
   4
   5
   > ltrim rlist 1 1
   OK
   > lrange rlist 0 -1
   4
   ~~~

9. linsert k before/after v vlaue   // 在某个元素之前或之后插入一个元素

   ~~~shell
   > linsert llist before 鲜花 好看   // 在鲜花元素之前插入好看
   6
   > lrange llist 0 -1
   2
   好看
   鲜花
   小鸟
   1
   ~~~

10. lrem k count value    // 从左边或右边移除指定个数匹配的value元素

    - count > 0: 从右往左移除count个值为 value 的元素。
    - count < 0: 从左往右移除count个值为 value 的元素。
    - count = 0: 移除所有值为 value 的元素。

    ~~~shell
    > lrange llist 0 -1
    2
    好看
    鲜花
    小鸟
    1
    小鸟
    > lrem llist -1 小鸟   // 从右边移除一个小鸟
    1
    > lrange llist 0 -1
    2
    好看
    鲜花
    小鸟
    1
    > lrem llist 1 小鸟  // 从左边移除一个小鸟
    1
    > lrange llist 0 -1
    2
    好看
    鲜花
    1
    ~~~

11.  lpushx k v   // 如果k存在时从左边添加一个元素v, k不存在时不做任何操作

    ~~~shell
    > lrange llist 0 -1
    大锤
    2
    好看
    鲜花
    1
    > lpushx newList 大锤
    0
    > lrange newList 0 -1
    ~~~

12.  rpushx k v   // 如果k存在时从右边添加一个元素v, k不存在时不做任何操作

13. rpoplpush sourecList TargetList   // 从sourecList的右边弹出一个元素push到TargetList的左边（原子性）

    ~~~shell
    > rpoplpush llist rlist
    1
    > lpushx newList 大锤
    0
    > lrange llist 0 -1
    大锤
    2
    好看
    鲜花
    > lrange rlist 0 -1
    1
    4
    ~~~

### 4. set <String, set<**String**>>

​		list型数据结构的v很像java中的**set**类型,里面的元素唯一，但是这个队列中的元素只能是String。

#### 常用命令

1. sadd k v1 v2 v3 ...    // 创建一个键为set1的set,并将v1 v2 v3等元素放入其中

   ~~~shell
   > sadd set1 1 2 3 4 1 1   // 该命令会将重复的元素1去掉
   4
   ~~~

2. smembers k    // 获取set中的所有元素

   ~~~shell
   > smembers set1
   1
   2
   3
   4
   ~~~

3. scard k    // 获取set的长度

   ~~~shell
   > scard set1
   4
   ~~~

4. srem k v   // 移除v元素

   ~~~shell
   > srem set1 2
   1
   > smembers set1
   1
   3
   4
   ~~~

5. spop k count   // 从k中随机移除count个元素, 返回被移除的元素

   ~~~shell
   > sadd set3 1 2 3 4 5 6 7
   7
   > spop set1 3
   1
   4
   > spop set3 2
   1
   2
   > spop set3 2
   3
   7
   > smembers set3
   4
   5
   6
   ~~~

6. sismember k v   // 测试k中是否存在v元素, 1: 存在，0: 不存在

   ~~~shell
   > sismember set3 6
   1
   > sismember set3 a
   0
   
   ~~~

7. srandmember k   // 从set中随机获取一个元素

   ~~~shell
   > srandmember set1
   4
   > srandmember set1
   1
   ~~~

8. smove k1 k2 v    // 将k1元素中的v移动到k2中

   ~~~shell
   > smove set1 set2 3
   1
   > smembers set1
   1
   4
   > smembers set2
   3
   ~~~

9. sdiff k k1 k2 k3 ...    // 返回k与k1 k2 k3等set的差集

   ~~~shell
   > sadd k a b c d
   4
   > sadd k1 a b
   2
   > sadd k2 a c
   2
   > sdiff k k1 k2
   d
   ~~~

8. sdiffstore target k k1 k2 ...    // 将k1和k2等set的差集放入target 中（如果target存在，则会被覆盖）

   ~~~shell
   > sdiffstore k4 k k1 k2
   1
   > smembers k4
   d
   ~~~

9. sinter k1 k2 ...   // 返回k1和k2等set的交集 

   ~~~shell
   > sinter k1 k2
   a
   ~~~

9. sinterstore target  k1 k2  // 将k1 k2的交集放入target 中（如果target存在，则会被覆盖）

   ~~~shell
   > sinterstore k5 k1 k2
   1
   > smembers k5
   a
   ~~~

10. sunion k1 k2   // 返回k1和k2的并集 

    ~~~shell
    > sunion k1 k2
    a
    b
    c
    ~~~

11. sunionstore target   k1 k2 ...  // 将k1和k2等set的并集放入target 中（如果target存在，则会被覆盖）

    ~~~shell
    > sunionstore k6 k1 k2
    3
    > smembers k6
    a
    b
    c
    ~~~

###  5. zset 

​		**zset**相当于一个带权重的**set**

#### 常用命令

1. zadd k score1 v1 score2 v2 ...   // 创建键为k的zset,并设置每个元素的权重

   ~~~shell
   > zadd 小明 80 语文 90 数学 50 英语
   3
   ~~~

2. zrange k startIndex endIndex withscores  // 返回指定区间内的元素和对应的权重，withscores可不写

   ~~~shell
   > zrange 小明 1 2 withscores
   语文
   80
   数学
   90
   > zrange 小明 1 2
   语文
   数学
   ~~~


3. zrevrange k startIndex endIndex withscores   // 根据权重由高到低输出指定区间的元素

   ~~~shell
   > zrevrange 小明 0  -1 withscores
   数学
   90
   语文
   80
   英语
   50
   ~~~

4.  zrem k v1 v2 ...    // 移除指定zset中的指定元素

   ~~~shell
   > zrem 小明 语文 数学
   2
   > zrange 小明 0 1 withscores
   英语
   50
   ~~~

5. zcount k minScore MaxScore    // 获取指定score区间内的总数量

   ~~~shell
   > zadd 小明 99 语文 70 物理 80 数学 50 英语
   0
   > zcount 小明 60 90
   2
   ~~~

6. zcard k    // 获取zset中元素的个数

   ~~~shell
   > zcard 小明
   4
   ~~~

7. zscore k v   // 获取指定元素的权重

   ~~~shell
   > zscore 小明 语文
   99
   ~~~


8. zrank k v   // 获取v的索引

   ~~~shell
   > zrank 小明 数学
   2
   ~~~

9. zpopmax k count    // 弹出count个最高分的元素

   ~~~shell
   > zpopmax 小明 1
   语文
   99
   ~~~

10. zpopmin k count    // 弹出count个最高分的元素

## 事务

​		redis中的事务不同于关系型数据库。redis中的事务更像一种通过队列保证多条命令串行化执行的手段。在redis中没有回滚的概念，一条命令的报错并不会影响其他命令的执行。并且redis没有行锁的，也即是说其他线程是可以对事务中正在修改的数据进行变更。

1.  命令出错不会回滚
2.  命令出错，执行不会停止。即接下来的命令依然会执行
3.  discord和exec命令会取消watch监听的key

相关命令：

- [MULTI](http://www.redis.com.cn/commands/multi)                     // 开启事务，使用此命令后，之后输入的命令都会进入一个队列，命令会检查语法但不执行
- [DISCARD](http://www.redis.com.cn/commands/discard)            // 取消事务，使用此命令后，会取消之前输入的命令
- [EXEC](http://www.redis.com.cn/commands/exec)                  // 提交事务，使用此命令后，会执行之前输入的命令
- [WATCH](http://www.redis.com.cn/commands/watch)           // 监视一或多个key,如果在事务执行之前，被监视的key有改动，则事务被打断 （ 类似乐观锁 ）
- [UNWATCH](http://www.redis.com.cn/commands/unwatch)  //  取消watch对所有key的监视（ `EXEC` 或`DISCARD`，也会取消watch对所有key的监视 

## spring data redis

### 关键依赖

~~~xml
 <dependency>
   		<groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
        <version>2.3.3.RELEASE</version>
</dependency>
<dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
        <version>5.3.3.RELEASE</version>
</dependency>
~~~

###  yml配置

~~~yml
spring:
  redis:
    host: 127.0.0.1   
    port: 6379
    password: password
  cache:   # 配置缓存管理
    type: redis
    redis:
      time-to-live: 200000
      use-key-prefix: true
      key-prefix: spring_cache_
~~~

### 配置类

~~~java
@Configuration
@EnableCaching  // 启用注解驱动的缓存管理功能，即@Cacheable、@CachePut、@CacheEvict
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.create(connectionFactory);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {

        // 设置序列化方式
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.setDefaultSerializer(RedisSerializer.json());

        return redisTemplate;
    }
}

~~~

### 使用

注入RedisTemplate或StringRedisTemplate，调用其中的方法即可

- opsForValue                          // Redis String 类型相关操作
- opsForHash                          // Redis Hash类型相关操作
- opsForList                            // Redis List类型相关操作
- opsForZSet                          // Redis Zset类型相关操作
- opsForSet                            // Redis Set类型相关操作
- opsForGeo                         // Redis Geo类型相关操作
- opsForHyperLogLog     // Redis HyperLogLog类型相关操作
- opsForStream                 // Redis Stream相关操作

示例:

~~~java
stringRedisTemplate.opsForValue().set("name", "小明");

redisTemplate.opsForHash().put("books", "java", "16");
redisTemplate.opsForHash().put("books", "python", "16");

redisTemplate.opsForZSet().add("水果", "苹果", 4);
redisTemplate.opsForZSet().add("水果", "香蕉", 3);

~~~





