package com.itheima.restkeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.pojo.Dish;
import com.itheima.restkeeper.mapper.DishMapper;
import com.itheima.restkeeper.pojo.DishFlavor;
import com.itheima.restkeeper.req.DishVo;
import com.itheima.restkeeper.service.IDishFlavorService;
import com.itheima.restkeeper.service.IDishService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.restkeeper.utils.BeanConv;
import com.itheima.restkeeper.utils.EmptyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description：菜品管理 服务实现类
 */
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements IDishService {

    @Autowired
    IDishFlavorService dishFlavorService;

    @Autowired
    DishMapper dishMapper;

    @Override
    public Page<Dish> findDishVoPage(DishVo dishVo, int pageNum, int pageSize) {
        //构建Page<Dish>分页对象
        //按分类查询
        //按菜品名称查询
        //按简码查询
        //按有效性查询
        //按sortNo升序
        //执行page返回结果
        return null;
    }

    @Override
    @Transactional
    public Dish createDish(DishVo dishVo) {
        //转换DishVo为Dish
        //执行保存
        //保存菜品和口味中间表信息
        return null;
    }

    /**
     * @Description 菜品拥有的口味
     */
    private List<String> dishHasDishFlavor(Long dishId){
        return null;
    }

    @Override
    public Boolean updateDish(DishVo dishVo) {
            //删除以往有的口味
        //添加新口味
        return null;
    }

    @Override
    public Boolean deleteDish(String[] checkedIds) {
        //构建选择ids的List<String>
        //批量移除菜品
        //批量移除菜品口味
        return null;
    }

    @Override
    public List<Dish> findDishVoByCategoryId(Long categoryId) {
        //构建查询条件：菜品起售，菜品有效
        //执行list查询
        return null;
    }

    @Override
    public List<Dish> findDishVoByStoreId(Long storeId) {
        //构建查询条件：店铺ID，菜品起售，菜品有效
        //执行list查询
        return null;
    }

    @Override
    public Boolean updateDishNumber(Long step,Long dishId) {
        //修改菜品数量
        return null;
    }

    @Override
    public List<Dish> findDishVos() {
        return null;
    }

}
