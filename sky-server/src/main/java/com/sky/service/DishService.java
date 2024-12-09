package com.sky.service;

import com.sky.dto.DishDTO;
import org.springframework.context.annotation.Bean;

public interface DishService {

    /*
    新增菜品和对应的口味数据
     */
    public void saveWithFlavor(DishDTO dishDTO);
}
