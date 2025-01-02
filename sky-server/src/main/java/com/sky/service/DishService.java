package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;
import org.springframework.context.annotation.Bean;

import java.util.List;

public interface DishService {



    /*
     新增菜品和对应的口味数据
      */
    public void saveWithFlavor(DishDTO dishDTO);

    /*
    菜品分页查询接口
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
    批量删除菜品
     */
    void deleteBatch(List<Long> ids);

    /*
    修改菜品：根据id查询菜品和对应口味数据
     */
    DishVO getByIdWithFlavor(Long id);

    /*
   修改菜品：修改菜品和对应口味数据
    */
    void updateWithFlavor(DishDTO dishDTO);

    /*
    起售停售
     */
    void startOrStop(Integer status, Long id);

     /*
     根据分类id查找菜品
      */
     List<Dish> list(Long categoryId);


    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);
}
