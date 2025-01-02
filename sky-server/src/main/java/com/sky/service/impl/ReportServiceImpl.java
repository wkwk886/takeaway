package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.io.File.separator;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WorkspaceService workspaceService;

    /*
    指定时间区间内的营业额统计
     */
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        //list集合存放begin-end每一天的日期
        List<LocalDate> dateList = new ArrayList<>();

        dateList.add(begin);

        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        List<Double> turnoverList = new ArrayList<>();//放每天营业额
        for (LocalDate date : dateList) {
            //查询每一天date对应的营业额，营业额是状态为 已完成 的订单金额合计
            //select sum(amount) from orders where order-time>? and<? and status=5
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);//当天开始时间
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);//当天结束时间

            Map map=new HashMap();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null? 0.0:turnover;
            turnoverList.add(turnover);


        }
        //拼成string
        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /*
    用户数据统计
     */
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        //begin-end的每天的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        //每一天的用户数量，总数/新增
        List<Integer> newUserList = new ArrayList<>();//每天新增
        List<Integer> totalUserList = new ArrayList<>();//每天总数

        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);

            Map map=new HashMap();
            map.put("end",endTime);
            Integer total_user=userMapper.countByMap(map);//总
            map.put("begin",beginTime);
            Integer newUser=userMapper.countByMap(map);//新增
            //放一条数据进集合
            totalUserList.add(total_user);
            newUserList.add(newUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .build();
    }

    /*
    订单统计
     */
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        List<Integer> orderCountList = new ArrayList<>();//每天
        List<Integer> validOrderCountList = new ArrayList<>();//有效
        //查询每天的订单数量，有效/总数
        for (LocalDate date : dateList) {
            LocalDateTime beginTime=LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(date, LocalTime.MAX);
            //查一天的订单总数
            Integer orderCount=getOrderCount(beginTime,endTime,null);
            //查一天的有效订单数
            Integer validOrderCount=getOrderCount(beginTime,endTime,Orders.COMPLETED);

            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCount);
        }
        //时间区间内的订单总数
        Integer totalOrderCount=orderCountList.stream().reduce(Integer::sum).get();
        //时间区间内的有效订单数量
        Integer totalValidOrderCount=validOrderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate=0.0;
        if (totalOrderCount!=0){
            orderCompletionRate=totalValidOrderCount.doubleValue()/totalOrderCount;
        }

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }


    /*
    根据条件统计订单数量
     */
    private Integer getOrderCount(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);
        return orderMapper.countByMap(map);
    }

    /*
    销量排名统计
     */
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> salesTop10=orderMapper.getSalesTop10(beginTime,endTime);
        List<String> names = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String nameList=StringUtils.join(names,",");
        List<Integer> numbers = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String numberList=StringUtils.join(numbers,",");

        return SalesTop10ReportVO.builder()
                .nameList(nameList)
                .numberList(numberList)
                .build();
    }

    /*
    报表导出
     */
    public void exportBusinessData(HttpServletResponse response) {
        //查数据库获取营业数据--最近30天
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        LocalDateTime begin = LocalDateTime.of(dateBegin, LocalTime.MIN);
        LocalDateTime end = LocalDateTime.of(dateEnd, LocalTime.MAX);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(begin, end);//查询概览数据

        //查询数据通过POI写入excel
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);//基于模板文件创建一个新的excel文件
            XSSFSheet sheet = excel.getSheet("Sheet1");
            //填充数据-概览数据
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"至"+dateEnd);//时间
            XSSFRow row4 = sheet.getRow(3);
            row4.getCell(2).setCellValue(businessDataVO.getTurnover());//营业额
            row4.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());//订单完成率
            row4.getCell(6).setCellValue(businessDataVO.getNewUsers());
            XSSFRow row5 = sheet.getRow(4);
            row5.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row5.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            //填充数据-明细数据
            for(int i=0;i<30;i++){
                LocalDate date=dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));
                XSSFRow row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessDataVO.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessDataVO.getUnitPrice());
                row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            }


            //通过输出流，excel下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);
            //关闭资源
            out.close();
            excel.close();

        }catch (IOException e){
            e.printStackTrace();
        }



    }
}
