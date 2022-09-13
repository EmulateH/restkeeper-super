package com.itheima.restkeeper.web;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.restkeeper.DishFace;
import com.itheima.restkeeper.basic.ResponseWrap;
import com.itheima.restkeeper.enums.DishEnum;
import com.itheima.restkeeper.req.DishVo;
import com.itheima.restkeeper.utils.ResponseWrapBuild;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName DishController.java
 * @Description 菜品Controller
 */
@RestController
@RequestMapping("dish")
@Slf4j
@Api(tags = "菜品controller")
public class DishController {

    @DubboReference(version = "${dubbo.application.version}", check = false)
    DishFace dishFace;

    /**
     * @param dishVo 查询条件
     * @return
     * @Description 菜品列表
     */
    @PostMapping("page/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询菜品分页", notes = "查询菜品分页")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dishVo", value = "菜品查询对象", dataType = "DishVo"),
            @ApiImplicitParam(paramType = "path", name = "pageNum", value = "页码", dataType = "Integer"),
            @ApiImplicitParam(paramType = "path", name = "pageSize", value = "每页条数", dataType = "Integer")
    })
    public ResponseWrap<Page<DishVo>> findDishVoPage(
            @RequestBody DishVo dishVo,
            @PathVariable("pageNum") int pageNum,
            @PathVariable("pageSize") int pageSize) {
        Page<DishVo> dishVoPage = dishFace.findDishVoPage(dishVo, pageNum, pageSize);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, dishVoPage);
    }

    /**
     * @param dishVo 对象信息
     * @return
     * @Description 添加菜品
     */
    @PostMapping
    @ApiOperation(value = "添加菜品", notes = "添加菜品")
    @ApiImplicitParam(name = "dishVo", value = "菜品对象", required = true, dataType = "DishVo")
    ResponseWrap<DishVo> createDish(@RequestBody DishVo dishVo) {
        DishVo dishVoResult = dishFace.createDish(dishVo);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, dishVoResult);
    }

    /**
     * @param dishVo 对象信息
     * @return
     * @Description 修改菜品
     */
    @PatchMapping
    @ApiOperation(value = "修改菜品", notes = "修改菜品")
    @ApiImplicitParam(name = "dishVo", value = "菜品对象", required = true, dataType = "DishVo")
    ResponseWrap<Boolean> updateDish(@RequestBody DishVo dishVo) {
        Boolean flag = dishFace.updateDish(dishVo);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, flag);
    }

    /**
     * @param dishVo 查询对象
     * @return
     * @Description 删除菜品
     */
    @DeleteMapping
    @ApiOperation(value = "删除菜品", notes = "删除菜品")
    @ApiImplicitParam(name = "dishVo", value = "菜品查询对象", required = true, dataType = "DishVo")
    ResponseWrap<Boolean> deleteDish(@RequestBody DishVo dishVo) {
        //获得所有选中的菜品IDS
        String[] checkedIds = dishVo.getCheckedIds();
        Boolean flag = dishFace.deleteDish(checkedIds);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, flag);
    }

    /**
     * @param dishId 菜品id
     * @return
     * @Description 查找菜品
     */
    @GetMapping("{dishId}")
    @ApiOperation(value = "查找菜品", notes = "查找菜品")
    @ApiImplicitParam(paramType = "path", name = "dishId", value = "菜品Id", dataType = "Long")
    ResponseWrap<DishVo> findDishByDishId(@PathVariable("dishId") Long dishId) {
        DishVo dishVo = dishFace.findDishByDishId(dishId);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, dishVo);
    }

    @PostMapping("update-dish-enableFlag")
    @ApiOperation(value = "修改菜品有效状态", notes = "修改菜品有效状态")
    ResponseWrap<Boolean> updateDishEnableFlag(@RequestBody DishVo dishVo) {
        Boolean flag = dishFace.updateDish(dishVo);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, flag);
    }

    @PostMapping("update-dish-dishStatus")
    @ApiOperation(value = "修改菜品状态", notes = "修改菜品状态")
    ResponseWrap<Boolean> updateDishDishStatus(@RequestBody DishVo dishVo) {
        Boolean flag = dishFace.updateDish(dishVo);
        return ResponseWrapBuild.build(DishEnum.SUCCEED, flag);
    }

}
