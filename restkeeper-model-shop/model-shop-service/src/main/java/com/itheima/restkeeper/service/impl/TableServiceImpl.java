package com.itheima.restkeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.restkeeper.basic.BasicPojo;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.pojo.Table;
import com.itheima.restkeeper.mapper.TableMapper;
import com.itheima.restkeeper.req.TableVo;
import com.itheima.restkeeper.service.ITableService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.restkeeper.utils.BeanConv;
import com.itheima.restkeeper.utils.EmptyUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description：桌台 服务实现类
 */
@Service
public class TableServiceImpl extends ServiceImpl<TableMapper, Table> implements ITableService {

    @Override
    public Page<Table> findTableVoPage(TableVo tableVo,int pageNum,int pageSize) {
        //构建分页对象
        //按桌台名称查询
        //按就餐人数查询
        //按桌台使用状态查询
        //按桌台有效性查询
        //按sortNo升序排列
        //返回分页结果
        return null;
    }

    @Override
    public Table createTable(TableVo tableVo) {
        //转换TableVo为Table
        //执行保存
        return null;
    }

    @Override
    public Boolean updateTable(TableVo tableVo) {
        //转换TableVo为Table
        //执行按ID进行修改
        return null;
    }

    @Override
    public Boolean deleteTable(String[] checkedIds) {
        //构建选择ids的List<String>集合
        //执行批量移除
        return null;
    }

    @Override
    public List<Table> findTableVoList() {
        //构建查询条件：有效状态
        //执行list查询
        return null;
    }

    @Override
    public Boolean openTable(TableVo tableVo) {
        //构建条件：SuperConstant.FREE
        //执行update条件查询
        return null;
    }
}
