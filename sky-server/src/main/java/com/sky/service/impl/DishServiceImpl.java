package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /*
    新增菜品和对应的口味数据
     */
    @Transactional
    //保证方法原子性
    public void saveWithFlavor(DishDTO dishDTO){
        //向菜品表插入一条数据
        Dish dish=new Dish();//用dish即可，不需要用dishdto
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);
        //todo 获取insert语句生成的主键值，很乱
        Long dishId=dish.getId();

        //像口味表插入n条数据
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                //flavor中包括了dishId
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavors);
        }


    }
}
