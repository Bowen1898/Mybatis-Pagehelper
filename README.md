#mybatis-pagehelper
用过很多分页插件，发现这些插件都存在着一些问题

#### 拦截查询，在查询中添加limit子句来实现分页
 大家都知道，mysql的limit n,m是取n+m行再丢掉n行来实现的，这样导致页数到后面时效率非常低下
，通用的做法是使用延迟关联来实现分页。

由于需要表的信息才能进行延迟关联，所以常见的分页插件基本没有提供生成延迟关联语句的功能

#### 无法对一对多，多对多进行分页
limit只能限制数据库中原始的行，无法限制orm映射之后的对象的个数。

但是延迟关联恰好又能做到这一点，可以通过对延迟关联子查询进行limit，就可以达到目的

#### 所以
基于以上的想法，本项目只对分页后的对象进行简单的包装，并且可以基于延迟关联生成count子句

## 使用
- UserMapper.java
```java
public interface UserMapper{
    /**
    * 
    * @param param 分页查询参数 传递参数PageParam则自动分页，要放在第一位
    * @return 分页对象
    */
    Page<User> listUsers(PageParam param);
}
```
- UserMapper.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.yuyuko.myforum.system.dao.UserMapper">

<select id="listUsers" resultMap="UserDTOResultMap">
    select user.*
    from (select id from user limit #{limitFrom},#{limitSize}) join user using (id) 
</select>

//可以自己写也可以不写
//pagehelper会去查找count+分页语句id首字母大写的语句，如果没找到就生成一个
//默认是按照分页语句中from后面第一个子查询来改写成count查询，因此请将延迟关联查询放在from后面第一个

<select id="countListUsers" resultType="long">
//和listUsers中的子查询一样，只是select的变成了count(0)
    select count(0)
    from user
    limit #{limitFrom},#{limitSize}
</select>

</mapper>

```

- UserService.java
```java
public class UserService{
    @Autowired
    UserMapper userMapper;
    
    public PageInfo<User> listUsers(PageParam pageParam){
        return userMapper.listUser(pageParam).toPageInfo();
    }
}
```

## 说明
本项目改造自 [https://github.com/pagehelper/Mybatis-PageHelper](https://github.com/pagehelper/Mybatis-PageHelper)
目的不是为了使用，而是为了说明在很多场景下基于增加limit子句的分页插件并不灵活。
一般来说简单包装一下分页结果就可以了。有其他需求的自己添加