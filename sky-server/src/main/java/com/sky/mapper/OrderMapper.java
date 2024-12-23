package com.sky.mapper;

import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper {

    /*
    插入订单数据,需要返回主键值
     */
    void insert(Orders orders);

    /*

     */
}
