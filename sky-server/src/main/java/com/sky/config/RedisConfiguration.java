package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        //接受一个RedisConnectionFactory类型的参数，用于创建与Redis数据库建立连接的接口
        log.info("创建redis模板");
        RedisTemplate redisTemplate = new RedisTemplate();
        //设置redis的连接工厂对象,建立与Redis的连接
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //设置redis key的序列化器,在界面里显示时key不是乱码
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        return redisTemplate;
    }


}
