package com.sky.controller.admin;

import com.sky.constant.StatusConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")//手动指定bean名词
@RequestMapping("/admin/shop")
@Api(tags="店铺相关接口")
@Slf4j
public class ShopController {

    @Autowired
    private RedisTemplate redisTemplate;
    public static final String KEY="SHOP_STATUS";
    /*
    设置营业状态
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺营业状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("设置店铺营业状态为:{}",status==1?"营业中":"打烊中");
        //mapper层与mysql链接，操作mysql;若操作redis,直接在controller完成
        redisTemplate.opsForValue().set("KEY",status);//字符串类型操作
        return Result.success();
    }

    /*
    查询营业状态
     */
    @GetMapping("/status")
    @ApiOperation("查询店铺状态")
    //不需要传入参数
    public Result<Integer> getStatus(){
        Integer status=(Integer) redisTemplate.opsForValue().get("KEY");
        log.info("获取店铺营业状态为:{}",status==1?"营业中":"打烊中");
        return Result.success(status);
    }


}
