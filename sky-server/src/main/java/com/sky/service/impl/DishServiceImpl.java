package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

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

    /*
    菜品分页查询实现类
     */
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());//开始分页
        //分页查询
        Page<DishVO> page=dishMapper.pageQuery(dishPageQueryDTO);//Page:通常是一个分页结果的封装类,DishVO:泛型类型
        return new PageResult(page.getPageSize(), page.getResult());//返回一个新的分页结果对象PageResult
    }

    /*
    批量删除菜品
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
         //判断是否能删除-是否为起售？
        for (Long id : ids) {
            Dish dish=dishMapper.getById(id);
            if (dish.getStatus()== StatusConstant.ENABLE){
                //当前菜品起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        //判断是否能删除-在套餐中？
        List<Long> setmealIds=setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds!=null && setmealIds.size()>0){
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //删除菜品表中的菜品
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品对应的口味表中的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }

    }

    /*
    修改菜品：根据id查询菜品和对应口味数据
     */
    public DishVO getByIdWithFlavor(Long id) {
        //查菜品
        Dish dish=dishMapper.getById(id);
        //查口味，可能有多个，是个list集合
        List<DishFlavor> dishFlavors=dishFlavorMapper.getByDishId(id);
        //封装
        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);//对象拷贝
        dishVO.setFlavors(dishFlavors);//单独设置口味数据

        return dishVO;
    }

    /*
   修改菜品：修改菜品和对应口味数据
    */
    public void updateWithFlavor(DishDTO dishDTO) {
        //修改菜品信息
        Dish dish=new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        //删除原有口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());

        //重新插入口味数据
        List<DishFlavor> flavors=dishDTO.getFlavors();
        if(flavors!=null&&flavors.size()>0){
            flavors.forEach(dishFlavor -> {
                //flavor中包括了dishId
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /*
    起售停售
     */
    @Transactional
    public void startOrStop(Integer status, Long id) {
        //构造实体
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        //update
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }


    }

}
