package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {

    /*
    插入订单数据,需要返回主键值
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /*
    根据动态条件
    查询订单信息
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    根据id查询订单
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);

    /*
    计算各个状态的数量
     */
    @Select("select count(id) from orders where status=#{status} ")
    Integer countStatus(Integer status);
}
