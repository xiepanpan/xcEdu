项目：

| xc-ui-pc-sysmanage | vue 项目系统管理中心 |
| ------------------ | -------------------- |
| xcEduService       | 后台服务             |
| xc-ui-pc-teach     | 教学后台配置         |



### 第一天 cms搭建

CMS （Content Management System）即内容管理系统 

使用MongoDB数据库 使用swagger生成文档

为什么要使用MongoDB：

1、Mongodb是非关系型数据库，存储Json格式数据 ,数据格式灵活。
2、相比课程管理等核心数据CMS数据不重要，且没有事务管理要求。



分页接口：

http://localhost:31001/cms/page/list/1/2



这里Dao接口继承了MongoRepository，在MongoRepository中定义了很多现成的方法，如save、delete等，通
过下边的代码来测试这里父类方法。



### 第二天 cms前端

vue

element vue组件

proxyTable解决前端向后端发请求跨域问题

### 第三天 cms基于vue增删改查 统一异常拦截器



### 第四天 使用freemarker页面静态化存储到GridFS中

主要实现的功能：实现轮播图的可配置 

实现模板+图片路径 = 生成静态页面



SpringMVC提供 RestTemplate请求http接口，RestTemplate的底层可以使用第三方的http客户端工具实现http 的
请求，常用的http客户端工具有Apache HttpClient、OkHttpClient等，本项目使用OkHttpClient完成http请求，
原因也是因为它的性能比较出众。



GridFS是MongoDB提供的用于持久化存储文件的模块，CMS使用MongoDB存储数据，使用GridFS可以快速集成
开发。
它的工作原理是：
在GridFS存储文件是将文件分块存储，文件会按照256KB的大小分割成多个块进行存储，GridFS使用两个集合
（collection）存储文件，一个集合是chunks, 用于存储文件的二进制数据；一个集合是files，用于存储文件的元数
据信息（文件名称、块大小、上传时间等信息）。
从GridFS中读取文件要对文件的各各块进行组装、合并。

**业务逻辑：根据模板管理的流程，最终将模板信息存储到MongoDB的cms_template中，将模板文件存储到GridFS中。**

cms_config 存储各种轮播图的配置

cms_page  dataurl属性存储远程调用cms——config的路径 存储模板id

cms_template 存储模板信息 fileid存储对应的模板文件

步骤：

1. 存储模板文件到GridFs中 生成objectId（测试类中实现这个功能）
2. 在cms_template  插入一条数据 fileId为上传模板文件对应objectId 这样template和文件系统对应起来
3. 修改cms_page  填写对应的数据dataurl 和对应templatId
4. 然后页面预览就是模板+数据的整合了

dataurl：http://localhost:31001/cms/config/getmodel/5a791725dd573c3574ee333f

### 

### 环境配置

MongoDB数据库 studio3T 客户端

nginx搭建本地的静态文件映射

### 第五天 rabbitmq基础

### 第六天 页面发布

技术点： rabbitmq



页面发布 替换静态资源 实现前台页面的替换

课程查询和添加 三级树状结构

element-ui 的tree组件

### 第七天 pageHelper 分页

### 第八天 fastdfs 图片上传

课程图片的保存 查询 和删除  

vue图片上传组件

删除： promise异步调用

通过查询deleteCoursePic方法的底层代码，deleteCoursePic最终返回一个promise对象。

Promise是ES6提供的用于异步处理的对象，因为axios提交是异步提交，这里使用promise作为返回值。

Promise的使用方法如下：

Promise对象在处理过程中有三种状态：

pending：进行中

resolved：操作成功

rejected: 操作失败



### 第九天 课程预览

#### eureka注册中心

为什么 要用注册中心？
1、微服务数量众多，要进行远程调用就需要知道服务端的ip地址和端口，注册中心帮助我们管理这些服务的ip和
端口。
2、微服务会实时上报自己的状态，注册中心统一管理这些微服务的状态，将存在问题的服务踢出服务列表，客户
端获取到可用的服务进行调用。



高可用：

1、在实际使用时Eureka Server至少部署两台服务器，实现高可用。
2、两台Eureka Server互相注册。
3、微服务需要连接两台Eureka Server注册，当其中一台Eureka死掉也不会影响服务的注册与发现。
4、微服务会定时向Eureka server发送心跳，报告自己的状态。
5、微服务从注册中心获取服务地址以RESTful方式发起远程调用。

在IDEA中制作启动脚本:

-DPORT=50101 -DEUREKA_DOMAIN=eureka01 -DEUREKA_SERVER=http://eureka02:50102/eureka/

-DPORT=50102 -DEUREKA_DOMAIN=eureka02 -DEUREKA_SERVER=http://eureka01:50101/eureka/



准备工作：

模板编写并测试通过后要在数据库保存：
1、模板信息保存在xc_cms数据库(mongodb)的cms_template表
2、模板文件保存在mongodb的GridFS中。

流程图：

![](https://xiepanpan123.oss-cn-beijing.aliyuncs.com/%E5%AD%A6%E6%88%90%E5%9C%A8%E7%BA%BF/%E8%AF%BE%E7%A8%8B%E9%A2%84%E8%A7%88%E5%8A%9F%E8%83%BD%E6%B5%81%E7%A8%8B.png)

**ssi** 服务端嵌入（server side include）,是一种基于服务器端的网页制作技术，可以将多个子页面合并渲染输出，大多数基于unix平台的web服务器均支持ssi指令，如nginx、apache

https://www.cnblogs.com/dehigher/p/10127380.html

项目命令：



### 第十天 课程发布



课程发布流程图

![](https://xiepanpan123.oss-cn-beijing.aliyuncs.com/%E5%AD%A6%E6%88%90%E5%9C%A8%E7%BA%BF/%E8%AF%BE%E7%A8%8B%E5%8F%91%E5%B8%83%E5%8A%9F%E8%83%BD%E6%B5%81%E7%A8%8B.png)





http://www.xuecheng.com/cms/preview/5e7d94f92ed406810c600bdb

### 第十一天 es搜索

如何维护课程索引信息？

1、当课程向MySQL添加后同时将课程信息添加到索引库。

采用Logstach实现，Logstach会从MySQL中将数据采集到ES索引库。

2、当课程在MySQL更新信息后同时更新该课程在索引库的信息。

采用Logstach实现。

3、当课程在MySQL删除后同时将该课程从索引库删除。

手工写程序实现，在删除课程后将索引库中该课程信息删除。



es搜索



### 第十二天 前端搜索 

Nuxt.js

搜索分页  高亮

### 第十三天 视频上传

WebUploader 断点续传

![GjVWz8.png](https://s1.ax1x.com/2020/04/13/GjVWz8.png)

![GjZTXD.png](https://s1.ax1x.com/2020/04/13/GjZTXD.png)

本项目使用如下钩子方法：

1）before-send-file

在开始对文件分块儿之前调用，可以做一些上传文件前的准备工作，比如检查文件目录是否创建完成等。

2）before-send

在上传文件分块之前调用此方法，可以请求服务端检查分块是否存在，如果已存在则此分块儿不再上传。

3）after-send-file

在所有分块上传完成后触发，可以请求服务端合并分块文件。



服务端需要实现如下功能：

1、上传前检查上传环境

检查文件是否上传，已上传则直接返回。

检查文件上传路径是否存在，不存在则创建。

2、分块检查

检查分块文件是否上传，已上传则返回true。

未上传则检查上传路径是否存在，不存在则创建。

3、分块上传

将分块文件上传到指定的路径。

4、合并分块

将所有分块文件合并为一个文件。

在数据库记录文件信息。

### 第十四天 视频处理  媒资管理

 **原始视频通常需要经过编码处理，生成m3u8和ts文件方可基于HLS协议播放视频。通常用户上传原始视频，系统自动处理成标准格式，系统对用户上传的视频自动编码、转换，最终生成m3u8文件和ts文件**，处理流程如下：

1、用户上传视频成功

2、系统对上传成功的视频自动开始编码处理

3、用户查看视频处理结果，没有处理成功的视频用户可在管理界面再次触发处理

4、视频处理完成将视频地址及处理结果保存到数据库



**代码中使用@RabbitListener注解指定消费方法，默认情况是单线程监听队列，可以观察当队列有多个任务时消费端每次只消费一个消息，单线程处理消息容易引起消息处理缓慢，消息堆积，不能最大利用硬件资源。**

**可以配置mq的容器工厂参数，增加并发处理数量即可实现多线程处理监听队列，实现多线程处理消息**。

```java
//消费者并发数量
    public static final int DEFAULT_CONCURRENT = 10;

    @Bean("customContainerFactory")
    public SimpleRabbitListenerContainerFactory containerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConcurrentConsumers(DEFAULT_CONCURRENT);
        factory.setMaxConcurrentConsumers(DEFAULT_CONCURRENT);
        configurer.configure(factory, connectionFactory);
        return factory;
    }
```

#### vue 父子通信

课程计划与媒体资源关联

创建映射：

Post http://localhost:9200/xc_course/doc/_mapping

```json
{
"properties" : {
            
            "description" : {
                "analyzer" : "ik_max_word",
           "search_analyzer": "ik_smart",
               "type" : "text"
            },
            "grade" : {
               "type" : "keyword"
            },
            "id" : {
               "type" : "keyword"
            },
            "mt" : {
               "type" : "keyword"
            },
            "name" : {
                "analyzer" : "ik_max_word",
           "search_analyzer": "ik_smart",
               "type" : "text"
            },
         "users" : {
               "index" : false,
               "type" : "text"
            },
            "charge" : {
               "type" : "keyword"
            },
            "valid" : {
               "type" : "keyword"
            },
            "pic" : {
               "index" : false,
               "type" : "keyword"
            },
         "qq" : {
               "index" : false,
               "type" : "keyword"
            },
            "price" : {
               "type" : "float"
            },
         "price_old" : {
               "type" : "float"
            },
            "st" : {
               "type" : "keyword"
            },
            "status" : {
               "type" : "keyword"
            },
            "studymodel" : {
               "type" : "keyword"
            },
        "teachmode" : {
               "type" : "keyword"
            },
            "teachplan" : {
                "analyzer" : "ik_max_word",
           "search_analyzer": "ik_smart",
               "type" : "text"
            },
        "expires" : {
               "type" : "date",
        "format": "yyyy-MM-dd HH:mm:ss"
            },
        "pub_time" : {
               "type" : "date",
         "format": "yyyy-MM-dd HH:mm:ss"
            },
        "start_time" : {
               "type" : "date",
        "format": "yyyy-MM-dd HH:mm:ss"
            },
       "end_time" : {
               "type" : "date",
        "format": "yyyy-MM-dd HH:mm:ss"
            }
         }
 }
```



### hosts文件：

```
#xuecheng
127.0.0.1 www.xuecheng.com
127.0.0.1 eureka01
127.0.0.1 eureka02
192.168.217.130 img.xuecheng.com
```

端口分配：

| 系统               | 端口         |
| ------------------ | ------------ |
| cms                | 31001        |
| course             | 31200        |
| eureka             | 50102  50101 |
| filesystem文件系统 | 22100        |







