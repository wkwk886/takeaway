package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    /*
    根据状态，下单时间查询超时未付款订单
     */
    @Select("select * from orders where status=#{status} and order_time<#{orderTime}")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime orderTime);

    /*
    根据订单号，userid查询订单
     */
    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    /*
    根据map的筛选条件返回营业额
     */
    Double sumByMap(Map map);

    /*
    根据map动态条件统计订单数量
     */
    Integer countByMap(Map map);

    /*
    销量排名统计
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime begin,LocalDateTime end);
}
