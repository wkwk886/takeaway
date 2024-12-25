package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    /*
    用户下单
     */
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        //处理业务异常：地址簿为空
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //处理业务异常：购物车数据为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);//建立当前用户的购物车,只需要id数据
        List<ShoppingCart> shoppingCartList=shoppingCartMapper.list(shoppingCart);//list方法根据id返回整个购物车
        if(shoppingCartList==null || shoppingCartList.size()==0){
            //抛出异常
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入1条数据
        Orders orders = new Orders();
        //DTO是前端传过来的，orders是后端操作用的（orders最全,VO是返回给前端的
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        //补充别的属性
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);
        orders.setAddress(addressBook.getDetail());

        orderMapper.insert(orders);

        List<OrderDetail> orderDetailList=new ArrayList<>();
        //向订单明细表插入n条数据
        for (ShoppingCart cart : shoppingCartList) {
            //每一次遍历，插入一个订单明细数据
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());//id是当前订单明细观点的orders的id,这个id是SQL自己生成，直接插入数据库中的
            orderDetailList.add(orderDetail);
        }
        //批量插入数据库
        orderDetailMapper.insertBatch(orderDetailList);

        //清空用户购物车
        shoppingCartMapper.deleteByUserId(userId);

        //封装VO并返回
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );

        //生成空JSON，跳过微信支付流程
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
/*      // 模拟支付成功，更新数据库订单状态
        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(ordersPaymentDTO.getOrderNumber(), userId);
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);*/

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
    历史订单查询
     */
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        List<OrderVO> list=new ArrayList();//存放返回结果列表
        //设置分页
        PageHelper.startPage(pageNum, pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        //得到当前用户的id
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        //分页条件查询
        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);//返回结果封装在page

        if(page!=null && page.getTotal()>0){
            for (Orders orders : page) {
                Long orderId = orders.getId();//订单id
                //查询订单明细
                List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(orderId);
                //构造返回数据
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), list);
    }

    /*
    查询订单详情
     */
    public OrderVO details(Long id) {
        Orders orders=orderMapper.getById(id);
        //获得details
        List<OrderDetail> orderDetailList=orderDetailMapper.getByOrderId(orders.getId());
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetailList);
        return orderVO;
    }

    /*
    用户取消订单
     */
    public void userCancelById(Long id) {
        //根据id查询当前订单，获取数据库中的原始对象
        Orders ordersDB = orderMapper.getById(id);
        //校验订单是否存在，核对订单状态
        if(ordersDB==null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus()>2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //建立一个新orders，用于赋值给数据库修改status等信息，这样可以避免直接修改db
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());

        //修改数据库
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //调用微信退款接口
            //修改status
            orders.setPayStatus(Orders.REFUND);
        }
        //更新数据库
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orders.setCancelReason("用户取消");
        orderMapper.update(orders);
    }

    /*
    再来一单
     */
    public void repetition(Long id) {
        //获取用户id
        Long userId=BaseContext.getCurrentId();
        //根据订单id获取订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        //将订单详情转为购物车对象,并添加到购物车中
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            //订单详情复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            //设置购物车数据
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());
        //将购物车数据添加到数据库
        shoppingCartMapper.insertBatch(shoppingCartList);
    }
}
