package com.itheima.restkeeper.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.restkeeper.TableFace;
import com.itheima.restkeeper.basic.ResponseWrap;
import com.itheima.restkeeper.enums.TableEnum;
import com.itheima.restkeeper.exception.ProjectException;
import com.itheima.restkeeper.req.TableVo;
import com.itheima.restkeeper.utils.EmptyUtil;
import com.itheima.restkeeper.utils.ExceptionsUtil;
import com.itheima.restkeeper.utils.ResponseWrapBuild;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @ClassName TableController.java
 * @Description 桌台Controller
 */
@RestController
@RequestMapping("table")
@Slf4j
@Api(tags = "桌台controller")
public class TableController {

    @DubboReference(version = "${dubbo.application.version}",check = false)
    TableFace tableFace;

    /**
     * @Description 桌台列表
     * @param tableVo 查询条件
     * @return
     */
    @PostMapping("page/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询桌台list",notes = "查询桌台list")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "tableVo",value = "桌台查询对象",dataType = "TableVo"),
        @ApiImplicitParam(paramType = "path",name = "pageNum",value = "页码",dataType = "Integer"),
        @ApiImplicitParam(paramType = "path",name = "pageSize",value = "每页条数",dataType = "Integer")
    })
    public ResponseWrap<Page<TableVo>> findTableVoPage(
        @RequestBody TableVo tableVo,
        @PathVariable("pageNum") int pageNum,
        @PathVariable("pageSize") int pageSize) {
        return null;
    }

    /**
     * @Description 添加桌台
     * @param tableVo 对象信息
     * @return
     */
    @PostMapping
    @ApiOperation(value = "添加桌台",notes = "添加桌台")
    @ApiImplicitParam(name = "tableVo",value = "桌台对象",required = true,dataType = "TableVo")
    ResponseWrap<TableVo> createTable(@RequestBody TableVo tableVo) {
        return null;
    }

    /**
     * @Description 修改桌台
     * @param tableVo 对象信息
     * @return
     */
    @PatchMapping
    @ApiOperation(value = "修改桌台",notes = "修改桌台")
    @ApiImplicitParam(name = "tableVo",value = "桌台对象",required = true,dataType = "TableVo")
    ResponseWrap<Boolean> updateTable(@RequestBody TableVo tableVo) {
        return null;
    }

    /**
     * @Description 查找桌台
     * @param tableId 桌台id
     * @return
     */
    @GetMapping("{tableId}")
    @ApiOperation(value = "查找桌台",notes = "查找桌台")
    @ApiImplicitParam(paramType = "path",name = "tableId",value = "桌台Id",dataType = "Long")
    ResponseWrap<TableVo> findTableByTableId(@PathVariable("tableId") Long tableId) {
        return null;
    }

    /**
     * @Description 删除桌台
     * @param tableVo 查询对象
     * @return
     */
    @DeleteMapping
    @ApiOperation(value = "删除桌台",notes = "删除桌台")
    @ApiImplicitParam(name = "tableVo",value = "桌台查询对象",required = true,dataType = "TableVo")
    ResponseWrap<Boolean> deleteTable(@RequestBody TableVo tableVo ) {
        //拿到选中桌台的IDS
        return null;
    }

    /**
     * @Description 查找桌台
     * @return
     */
    @GetMapping("list")
    @ApiOperation(value = "查找桌台列表",notes = "查找桌台列表")
    ResponseWrap<List<TableVo>> findTableVoList() {
        return null;
    }

    @PostMapping("update-table-enableFlag")
    @ApiOperation(value = "修改桌台有效状态",notes = "修改桌台有效状态")
    ResponseWrap<Boolean> updateTableEnableFlag(@RequestBody TableVo tableVo) {
        return null;
    }

    @PostMapping("update-table-tableStatus")
    @ApiOperation(value = "修改桌台状态",notes = "修改桌台状态")
    ResponseWrap<Boolean> updateTableStatus(@RequestBody TableVo tableVo) {
        return null;
    }
}
