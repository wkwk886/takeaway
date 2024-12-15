package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;


    /*
    新增套餐
     */
    @Transactional
    public  void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        //向套餐表插入数据
        setmealMapper.insert(setmeal);

        //获取套餐自己生成的id
        Long setmealId = setmeal.getId();

        //创造套餐、菜品关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //根据当前的setmeal-id套餐id,获得套餐里的每一个菜品，插入setmeal-dish这个对应表
        setmealDishes.forEach(setmealDish -> {setmealDish.setSetmealId(setmealId);});

        //填充setmealdish表剩余部分
        setmealDishMapper.insertBatch(setmealDishes);





    }
}
