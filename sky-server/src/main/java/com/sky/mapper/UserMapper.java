package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /*
    根据openid查数据
     */
    @Select("select * from user where openid=#{openid}")
    User getByOpenid(String openid);

    /*
    插入user数据,需要返回主键值
     */
    void insert(User user);

    /*
    根据id获得用户
     */
    @Select("select * from user where id=#{userId}")
    User getById(Long userId);
}

