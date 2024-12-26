package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /*
    用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /*
    历史订单查询
     */
    PageResult pageQuery4User(int page, int pageSize, Integer status);

    /*
    查询订单详情
     */
    OrderVO details(Long id);

    /*
    用户取消订单
     */
    void userCancelById(Long id);

    /*
    再来一单
     */
    void repetition(Long id);

    /*
    订单搜索
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /*
    各个状态的订单数量统计
     */
    OrderStatisticsVO statistics();

    /*
    接单
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);

    /*
    拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /*
    取消订单
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /*
    派送订单
     */
    void delivery(Long id);

    /*
    完成订单
     */
    void complete(Long id);
}
