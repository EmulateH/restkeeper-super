package com.itheima.restkeeper.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.restkeeper.basic.BasicPojo;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.mapper.TableAreaMapper;
import com.itheima.restkeeper.pojo.TableArea;
import com.itheima.restkeeper.req.TableAreaVo;
import com.itheima.restkeeper.service.ITableAreaService;
import com.itheima.restkeeper.utils.BeanConv;
import com.itheima.restkeeper.utils.EmptyUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Description：桌台区域 服务实现类
 */
@Service
public class TableAreaServiceImpl extends ServiceImpl<TableAreaMapper, TableArea> implements ITableAreaService {

    @Override
    public Page<TableArea> findTableAreaVoPage(TableAreaVo tableAreaVo, int pageNum, int pageSize) {
        //构建分页对象
        //按区域名称查询
        //按是否有效查询
        //按sortNo升序排列
        //返回结果
        return null;
    }

    @Override
    public TableArea createTableArea(TableAreaVo tableAreaVo) {
        //转换TableAreaVo为TableArea
        //执行保存
        return null;
    }

    @Override
    public Boolean updateTableArea(TableAreaVo tableAreaVo) {
        //转换TableAreaVo为TableArea
        //按ID执行修改
        return null;
    }

    @Override
    public Boolean deleteTableArea(String[] checkedIds) {
        //构建选中的List<Sring>集合
        //批量删除
        return null;
    }

    @Override
    public List<TableArea> findTableAreaVoList() {
        //构建查询条件
        //执行list查询
        return null;
    }
}
