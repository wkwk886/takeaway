package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
菜品管理
 */
@RestController
@Api(tags="菜品相关接口")
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    /*
    新增菜品
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {//@RequestBody 把json形式的dishDTO转成query
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);//传入数据
        return Result.success();
    }

    /*
    菜品分页查询
     */
    @GetMapping("/page")
    @ApiOperation("菜品分页查询")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult=dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    删除菜品
     */
    @DeleteMapping
    @ApiOperation("批量删除菜品")
    public Result delete(@RequestParam List<Long> ids){//@RequestParam：从 HTTP 请求中提取查询参数、表单参数或 URL 参数，并将其绑定到控制器方法的参数中
        log.info("批量删除菜品：{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /*
    修改菜品：根据id查询菜品和对应口味数据
     */
    @GetMapping("/{id}")
    @ApiOperation("根据菜品id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品：{}",id);
        DishVO dishVO=dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /*
    修改菜品：修改菜品和对应口味数据
     */
    @PutMapping
    @ApiOperation("修改菜品")
    public Result upadte(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /*
    起售停售
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售")
    public Result<String> startOrStop(@PathVariable Integer status,Long id){
        log.info("起售停售菜品：{}",id);
        dishService.startOrStop(status,id);
        return Result.success();

    }

    /*
    根据分类id查询菜品，用于新增套餐
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查找菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("查找菜品：{}",categoryId);
        List<Dish> list=dishService.list(categoryId);
        return Result.success(list);
    }


}
