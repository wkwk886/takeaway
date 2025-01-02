package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.DishVO;
import com.sky.vo.SetmealVO;
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
    @Autowired
    private SetmealService setmealService;


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
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        //填充setmealdish表剩余部分
        setmealDishMapper.insertBatch(setmealDishes);
    }

    /*
    套餐分页查询
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page=setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    /*
    批量删除
     */
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id ->{
            Setmeal setmeal=setmealMapper.getById(id);
            if(setmeal.getStatus()==StatusConstant.ENABLE){
                //起售中的物品不能删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        //这一批次都可以删除，则：
        ids.forEach(setmealId->{
            //删除套餐
            setmealMapper.deleteById(setmealId);
            //删除套餐-菜品关系
            setmealDishMapper.deleteBysetmealId(setmealId);
        });

    }

    /*
    根据id查询套餐+菜品
     */
    public SetmealVO getByIdWithDIsh(Long id) {
        //套餐
        Setmeal setmeal=setmealMapper.getById(id);
        //菜品
        List<SetmealDish> setmealDishes=setmealDishMapper.getBySetmealId(id);//根据套餐id获取菜品list
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /*
    修改套餐+菜品
     */
    public void update(SetmealDTO setmealDTO) {
        //修改套餐本身
        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        //获取套餐id
        Long setmealId=setmealDTO.getId();
        //根据id查看套餐-菜品对应表，删除关联关系
        setmealDishMapper.deleteBysetmealId(setmealId);
        //建立新的套餐-菜品表
        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();
        //设置setmealDishes对应的套餐
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        //插入新的套餐-菜品关系
        setmealDishMapper.insertBatch(setmealDishes);

    }

    /*
    起售停售
     */
    public void startOrStop(Integer status, Long id) {
        //根据id构造整个实体，来修改实体的status数据
        //起售套餐时，判断套餐内是否有停售菜品，有停售菜品提示"套餐内包含未启售菜品，无法启售"
        if(status==StatusConstant.ENABLE){
            //获取菜品
            List<Dish> dishList=dishMapper.getBySetmealId(id);
            if(dishList!=null && dishList.size()>0){
                dishList.forEach(dish -> {
                    if(dish.getStatus()==StatusConstant.ENABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        //传入status，update
        Setmeal setmeal=Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);

    }


    /**
            * 条件查询
     * @param setmeal
     * @return
             */
    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}


