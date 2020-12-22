# Elasticsearch学习笔记

## 简介

Elasticsearch 是一个基于[Lucene](https://baike.baidu.com/item/Lucene/6753302)的分布式、高扩展、高实时的搜索与数据分析引擎，常被用作全文检索、结构化搜索、分析以及这三个功能的组合。它可以被下面这样准确的形容：

- 一个分布式的实时文档存储，*每个字段* 可以被索引与搜索
- 一个分布式实时分析搜索引擎
- 能胜任上百个服务节点的扩展，并支持 PB 级别的结构化或者非结构化数据

## 名词/概念

### 名词

- **cluster**：集群，es集群中有一个为主节点，这个主节点通过选举产生的。主从节点是对于集群内部来说的。es的一个概念就是去中心化，对于集群外部来说与任何一个节点的通信都是是等价。
- **shards**：索引分片，es把一个完整的索引分成多个分片分布到不同的节点上,构成分布式搜索。分片的数量只能在索引创建前指定，索引创建后不能更改。
- **replicas**：索引副本，es可以设置多个索引的副本，副本的作用一是提高系统的[容错性](https://baike.baidu.com/item/容错性)，当某个节点某个分片损坏或丢失时可以从副本中恢复。二是提高es的查询效率。
- **recovery**：数据恢复/数据重新分布，es在有节点加入或退出时会根据机器的负载对索引分片进行重新分配，挂掉的节点重新启动时也会进行数据恢复。
- **river**：代表es的一个数据源，也是其它存储方式（如：数据库）同步数据到es的一个方法。它是以插件方式存在的一个es服务，通过读取river中的数据并把它索引到es中，官方的river有couchDB的，RabbitMQ的，Twitter的，Wikipedia等。
- **gateway**：代表es索引快照的存储方式，es默认是先把索引存放到内存中，当内存满了时再持久化到本地硬盘。gateway对索引快照进行存储，当这个es集群关闭再重新启动时就会从gateway中读取索引备份数据。
- **discovery.zen**：es的自动发现节点机制，es是一个基于p2p的系统，它先通过广播寻找存在的节点，再通过[多播](https://baike.baidu.com/item/多播)协议来进行节点之间的通信，同时也支持[点对点](https://baike.baidu.com/item/点对点)的交互。
- **Transport**：代表es内部节点或集群与客户端的交互方式，默认内部是使用tcp协议进行交互，同时它支持http协议（json格式）、thrift、servlet、memcached、zeroMQ等的[传输协议](https://baike.baidu.com/item/传输协议)（通过插件方式集成）。
- **Index**：索引，索引包含的是一大推相似结构的文档数据，例如我们的商品索引，订单索引等，类比于我们的数据库。
- **Type**：类型，每一个索引里面可以有一个或者多个`type`，`type`是`index`中的一个逻辑数据分类，比如我的博客系统，一个索引，可以定义用户数据`type`，可以定义文章数据`type`，也可以定义评论数据`type`，类比数据库的表。
- **Document**：文档，文档是`ElasticSearch`中最小的数据单元，一条`Document`可以是一条文章数据，一条用户数据，一条评论数据，通常使用`JSON`数据结构来表示，每个`index`下的`type`中，存储多个`document`，类别数据库中的行。
- **Field**：字段，一个document里面存在多个Field字段，每个Field就是一个数据字段，类比数据库中的列。
- **mapping**：映射，数据如何存储在索引上，需要一个约束配置，例如数据类型，是否存储，查询的时候是否分词等等，类比数据库汇总的约束。

### Elasticsearch和数据库对别

| 关系型数据库   | ElasticSearch   |
| -------------- | --------------- |
| 数据库Database | Index (索引)    |
| 表Table        | Type (类型)     |
| 数据行Row      | Document (文档) |
| 数据列Column   | Field (字段)    |
| 约束Schema     | Mapping (映射)  |

### Elasticsearch数据类型

Elasticsearch 支持如下简单类型：

- 字符串: 

  >
  >- text（会进行分析【分词，建立倒排索引】）
  >-  keyword【不会分析，只有完全匹配才能搜索到】

- 数字 : `byte`, `short`, `integer`, `long`,`float`, `double`

- 布尔型: `boolean`

  > True/False/Yes/No等都能解析成boolean类型

- 日期: `date`

- 二进制: `binary` (无法被检索)

- 数组：[]

- 对象：`object`，单独的JSON对象

- 嵌套数据类型：`nested`，关于JSON对象的数组

- geo: (地理数据类型)

  >- 地理点数据类型：`geo_point`(经纬点)
  >- 地理形状数据类型：`geo_shape`

- 特定类型

  >- IP 类型: ip 用于描述 IPv4 和 IPv6 地址
  >- 补全类型(Completion): completion 提供自动完成提示
  >- 令牌计数类型(Token count): token_count 用来统计字符串中词条的数量
  >- 附件类型(attachment)：将附件如Office文件，open document格式，ePub，HTML等索引为 attachment 类型
  >- 抽取类型(Percolator)：接受来自领域特定语言（query-dsl）的查询



当你索引一个包含新域的文档—之前未曾出现-- Elasticsearch 会使用 [*动态映射*](https://www.elastic.co/guide/cn/elasticsearch/guide/current/dynamic-mapping.html) ，通过JSON中基本数据类型，尝试猜测域类型，使用如下规则：

| **JSON type**                  | **域 type** |
| ------------------------------ | ----------- |
| 布尔型: `true` 或者 `false`    | `boolean`   |
| 整数: `123`                    | `long`      |
| 浮点数: `123.45`               | `double`    |
| 字符串，有效日期: `2014-09-15` | `date`      |
| 字符串: `foo bar`              | `string`    |

> 这意味着如果你通过引号( `"123"` )索引一个数字，它会被映射为 `string` 类型，而不是 `long` 。但是，如果这个域已经映射为 `long` ，那么 Elasticsearch 会尝试将这个字符串转化为 long ，如果无法转化，则抛出一个异常

## 使用

Elasticsearch 提供Restful 风格的HTTP请求和Elasticsearch Clients的方式进行数据的操作。

- Restful API with JSON over HTTP

  >| method | desc                             |
  >| ------ | -------------------------------- |
  >| put    | 修改Document (文档)              |
  >| post   | 创建Index (索引)/Document (文档) |
  >| get    | 查询Index (索引)/Document (文档) |
  >| delete | 删除Index (索引)/Document (文档) |

- Elasticsearch Clients

  >- [Java REST Client [7.10\]](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/index.html)
  >- [JavaScript API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/javascript-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/javascript-api/index.html)
  >- [Ruby API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/ruby-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/ruby-api/index.html)
  >- [Go API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/go-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/go-api/index.html)
  >- [.NET API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/net-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/net-api/index.html)
  >- [PHP API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/php-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/php-api/index.html)
  >- [Perl API](https://www.elastic.co/guide/en/elasticsearch/client/perl-api/current/index.html)
  >- [Python API [7.x\]](https://www.elastic.co/guide/en/elasticsearch/client/python-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/python-api/index.html)
  >- [eland](https://www.elastic.co/guide/en/elasticsearch/client/eland/current/index.html)
  >- [Rust API](https://www.elastic.co/guide/en/elasticsearch/client/rust-api/current/index.html)
  >- [Java API (deprecated) [7.10\]](https://www.elastic.co/guide/en/elasticsearch/client/java-api/current/index.html) — [other versions](https://www.elastic.co/guide/en/elasticsearch/client/java-api/index.html)
  >- [Community Contributed Clients](https://www.elastic.co/guide/en/elasticsearch/client/community/current/index.html)

## 示例

### 索引

1. 创建索引

   >创建名为persion的索引
   >
   >~~~sh
   >PUT /persion
   >{
   >  "settings": {
   >    
   >  },
   >  "mappings": {
   >    "properties": {
   >      "id": {
   >        "type": "keyword",
   >        "index": true
   >      },
   >      "name": {
   >        "type": "text",
   >        "index": true
   >      },
   >      "sex": {
   >        "type": "keyword"
   >      },
   >      "age": {
   >        "type": "integer"
   >      },
   >      "birthday": {
   >        "type": "date",
   >        "format": ["yyyy-mm-dd"]
   >      },
   >      "isSeccess": {
   >        "type": "boolean"
   >      }
   >    }
   >  }
   >}
   >~~~

2. 查看索引

   >查看persion索引
   >
   >~~~ sh
   >GET /persion
   >~~~

3. 更新索引

   >
   >
   >

4. 删除索引

   >删除persion索引
   >
   >~~~sh
   >DELETE /persion
   >~~~

### 数据

1. 新增数据

   >POST /<index>/_doc/<id>    # id可以不写，不写时会自动创建
   >
   >~~~sh
   >POST /persion/_doc/1
   >{
   >  "id": "1",
   >  "name": "大锤子",
   >  "sex": "男",
   >  "age": 12,
   >  "birthday": "2020-09-23",
   >  "isSeccess": true
   >}
   >~~~

2. 查看数据

   >GET /<index>/_doc/<id>
   >
   >~~~sh
   >GET /persion/_doc/1
   >~~~

3. 修改数据

   >PUT  /<index>/_doc/<id> 
   >
   >~~~sh
   >PUT /persion/_doc/1
   >{
   >  "id": "1",
   >  "name": "小锤子",
   >  "sex": "男",
   >  "age": 20,
   >  "birthday": "2020-09-23",
   >  "isSeccess": false
   >}
   >~~~

4. 删除数据

   >DELETE /<index>/_doc/<id>
   >
   >~~~sh
   >DELETE /persion/_doc/1
   >~~~

### 复杂查询

1. match: 模糊匹配，把查询条件进行分词，然后进行查询,多个词条之间是or的关系

   >~~~sh
   >GET /persion/_search
   >{
   >"query": {
   >   "match": {
   >   "name": "小锤"
   >  }
   >}
   >}
   >~~~

2. term: 精准匹配，查询输入内容是否存在该字段的分词结果中（term查询不要查询text类型）

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "term": {
   >      "sex": {
   >        "value": "男"
   >      }
   >    }
   >  }
   >}
   >~~~

3. multi_match: 多字段匹配查询,会在多个字段的分词结果中查询输入内容

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "multi_match": {
   >      "query": "男",
   >      "fields": ["name", "sex"]
   >    }
   >  }
   >}
   >~~~

4. terms: 多值精准匹配，类型sql中的`in`

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "terms": {
   >      "sex": [
   >        "男",
   >        "女"
   >      ]
   >    }
   >  }
   >}
   >~~~

5. bool: `bool`把各种其它查询通过`must`（与）、`must_not`（非）、`should`（或）的方式进行组合

   >
   >
   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "bool": {
   >      "must": [
   >        {
   >          "match": {
   >            "name": "小"
   >          }
   >        }
   >      ],
   >      "must_not": [
   >        {
   >          "term": {
   >            "sex": {
   >              "value": "女"
   >            }
   >          }
   >        }
   >      ]
   >    }
   >  }
   >}
   >~~~

6. range: 范围查询

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "range": {
   >      "age": {
   >        "gte": 10,
   >        "lte": 30
   >      }
   >    }
   >  }  
   >}
   >~~~

7. fuzzy：允许存在偏差的`term`查询，`fuzziness`用来指定偏差范围

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "fuzzy": {
   >      "name": {
   >        "value": "小子",
   >        "fuzziness": 1
   >      }
   >    }
   >  }
   >}
   >~~~

8. filter: 在查询

   >~~~sh
   >GET /persion/_search
   >{
   >  "query": {
   >    "bool": {
   >      "must": [
   >        {
   >          "match": {
   >            "name": "小"
   >          }
   >        }
   >      ],
   >      "filter": [
   >        {
   >          "range": {
   >            "age": {
   >              "gte": 10,
   >              "lte": 25
   >            }
   >          }
   >        }
   >      ]
   >    }
   >  }
   >}
   >~~~

9. 排序

   >~~~sh
   >GET /persion/_search
   >{
   >  "sort": [
   >    {
   >      "age": {
   >        "order": "desc"
   >      }
   >    }
   >  ]
   >}
   >~~~

10. aggs: 聚合。对指定字段执行指定的聚合操作（最大max、最小min、求和sum等）

    >~~~sh
    >GET /persion/_search
    >{
    >  "aggs": {
    >    "NAME": {
    >      "sum": {
    >        "field": "age"
    >      }
    >    }
    >  }
    >}
    >~~~

## 整合spring-data-elasticsearch

1. 引入依赖

   ~~~xml
   <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
       	 <version>4.1.2</version>
   </dependency>
   ~~~

2. 编写配置文件

   ```yml
   spring:
     data:
       elasticsearch:
         repositories:
           enabled: true    # 开启Repository模式
     elasticsearch:
       rest:
         uris: 1**.***.***.***:9200
         username: username
         password: password
   ```

3. 创建实体类

   ~~~java
   @Data
   @Document(indexName = "userentity")   
   public class UserEntity {
   
       @Id
       @Field(type = FieldType.Keyword)
       private String id;
   
       @Field(type = FieldType.Text)
       private String name;
   
       @Field(type = FieldType.Keyword)
       private String sex;
   
       @Field(type = FieldType.Integer)
       private Integer age;
   
       @Field(type = FieldType.Boolean)
       private boolean isSuccess;
   
       @Field(type = FieldType.Date ,format = DateFormat.date)
       private Date birthday;
   }
   ~~~

4. 创建UserEntity的UserRepository接口，该接口需要继承ElasticsearchRepository接口

   ~~~java
   public interface UserRepository extends ElasticsearchRepository<UserEntity, String> {
   }
   ~~~

5. 注入UserRepository或者ElasticsearchRestTemplate

   ~~~java
   @RestController
   public class UserController {
   
       @Autowired
       private ElasticsearchRestTemplate elasticsearchRestTemplate;
   
       @Resource
       private UserRepository userRepository;
   
       /**
        * 通过UserRepository#findById方法查询数据
        *
        * @param id:
        * @return UserEntity
        * @Date 2020-12-21
        **/
       @GetMapping("/{id}")
       public UserEntity getUser(@PathVariable("id") String id){
   
           Optional<UserEntity> userEntityOptional = userRepository.findById(id);
           return userEntityOptional.orElseGet(null);
       }
   
       /**
        * 通过UserRepository#save方法保存数据
        *
        * @param userEntity:
        * @return UserEntity
        * @Date 2020-12-21
        **/
       @PostMapping("/save")
       public UserEntity saveUser(@RequestBody UserEntity userEntity){
   
           userEntity.setAge(RandomUtil.randomInt(10, 90));
           userEntity.setSuccess(RandomUtil.randomBoolean());
           userEntity.setBirthday(new Date());
           return userRepository.save(userEntity);
       }
   
       /**
        *  通过UserRepository#deleteById方法删除数据
        *
        * @param id:
        * @return boolean
        * @Date 2020-12-21
        **/
       @DeleteMapping("/{id}")
       public boolean delUser(@PathVariable("id") String id){
   
           userRepository.deleteById(id);
           Optional<UserEntity> userEntityOptional = userRepository.findById(id);
           return userEntityOptional.isPresent();
       }
   
       /**
        * 通过ElasticsearchRestTemplate#get方法查询数据
        *
        * @param id:
        * @return UserEntity
        * @Date 2020-12-21
        **/
       @GetMapping("template/{id}")
       public UserEntity get(@PathVariable("id") String id){
   
           return elasticsearchRestTemplate.get(id, UserEntity.class);
       }
   
   
       /**
        * 通过ElasticsearchRestTemplate#search方法查询数据
        * 查询参数为string类型
        *
        * @param param:
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @GetMapping("template/stringQuery")
       public SearchHits<UserEntity> stringQuery(@RequestParam("param") String param){
   
           String queryCondition = "{\n" +
                   "    \"match\": {\n" +
                   "      \"name\": {\n" +
                   "        \"query\": \"" + param + "\"\n" +
                   "      }\n" +
                   "    }\n" +
                   "  }";
           Query query = new StringQuery(queryCondition);
   
           SearchHits<UserEntity> searchHits = elasticsearchRestTemplate.search(query, UserEntity.class);
           return searchHits;
       }
   
       /**
        * 通过ElasticsearchRestTemplate#search方法查询数据
        * 查询参数为Native类型
        *
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @GetMapping("template/nativeSearch")
       public SearchHits<UserEntity> nativeSearch(){
   
   
           QueryBuilder queryBuilder = new MatchQueryBuilder("name", "小");
           Query query = new NativeSearchQuery(queryBuilder);
   
           SearchHits<UserEntity> searchHits = elasticsearchRestTemplate.search(query, UserEntity.class);
           return searchHits;
       }
   
       /**
        * 通过ElasticsearchRestTemplate#search方法查询数据
        * 查询参数为Criteria类型
        *
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @GetMapping("template/criteriaQuery")
       public SearchHits<UserEntity> criteriaQuery(){
   
           Criteria criteria = new Criteria("sex").is("女");
           Query query = new CriteriaQuery(criteria);
   
           SearchHits<UserEntity> searchHits = elasticsearchRestTemplate.search(query, UserEntity.class);
           return searchHits;
       }
   
       /**
        * 通过ElasticsearchRestTemplate#save方法保存数据
        * index(索引) 为实体类上注解标识的索引
        *
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @PostMapping("template/save")
       public UserEntity saveUserEntity(@RequestParam("name") String name){
   
           UserEntity userEntity = new UserEntity();
           userEntity.setId(IdUtil.simpleUUID());
           userEntity.setName(name);
           userEntity.setAge(RandomUtil.randomInt(10, 90));
           userEntity.setSuccess(RandomUtil.randomBoolean());
           userEntity.setBirthday(new Date());
   
           return elasticsearchRestTemplate.save(userEntity);
       }
   
       /**
        * 通过ElasticsearchRestTemplate#delete方法删除数据
        *
        *
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @DeleteMapping("template/{id}")
       public String delUserEntity(@PathVariable("id") String id){
   
           String delete = elasticsearchRestTemplate.delete(id, UserEntity.class);
           return delete;
       }
   
       /**
        * 通过ElasticsearchRestTemplate#exists方法查询数据是否存在
        *
        *
        * @return SearchHits<UserEntity>
        * @Date 2020-12-21
        **/
       @GetMapping("template/exists/{id}")
       public boolean exists(@PathVariable("id") String id){
   
           return elasticsearchRestTemplate.exists(id, UserEntity.class);
       }
       
   }
   ~~~

   











