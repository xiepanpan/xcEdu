项目：

| xc-ui-pc-sysmanage | vue 项目系统管理中心 |
| ------------------ | -------------------- |
| xcEduService       | 后台服务             |



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





项目命令：

