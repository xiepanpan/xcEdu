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