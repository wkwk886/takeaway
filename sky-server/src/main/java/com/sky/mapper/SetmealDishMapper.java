package com.sky.mapper;
//表示套餐与菜品的对应关系

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /*
    根据菜品id查套餐id,动态SQL
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /*
    批量插入菜品-套餐对应数据
     */
    void insertBatch(List<SetmealDish> setmealDishes);
}
