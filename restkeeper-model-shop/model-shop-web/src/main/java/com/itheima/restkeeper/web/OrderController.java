package com.itheima.restkeeper.web;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.restkeeper.OrderFace;
import com.itheima.restkeeper.TableFace;
import com.itheima.restkeeper.TradingFace;
import com.itheima.restkeeper.basic.ResponseWrap;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.enums.BrandEnum;
import com.itheima.restkeeper.enums.OrderEnum;
import com.itheima.restkeeper.exception.ProjectException;
import com.itheima.restkeeper.req.*;
import com.itheima.restkeeper.utils.EmptyUtil;
import com.itheima.restkeeper.utils.ExceptionsUtil;
import com.itheima.restkeeper.utils.ResponseWrapBuild;
import com.itheima.restkeeper.utils.UserVoContext;
import io.seata.spring.annotation.GlobalTransactional;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName OrderController.java
 * @Description 订单处理controller
 */
@RestController
@RequestMapping("order")
@Slf4j
@Api(tags = "订单信息controller")
public class OrderController {

    @DubboReference(version = "${dubbo.application.version}", check = false)
    OrderFace orderFace;

    /**
     * @param orderVo 查询条件
     * @return
     * @Description 订单列表
     */
    @PostMapping("page/{pageNum}/{pageSize}")
    @ApiOperation(value = "查询订单list", notes = "查询订单list")
    @ApiImplicitParams({
        @ApiImplicitParam(name = "orderVo", value = "订单查询对象", required = false, dataType = "OrderVo"),
        @ApiImplicitParam(paramType = "path", name = "pageNum", value = "页码", example = "1", dataType = "Integer"),
        @ApiImplicitParam(paramType = "path", name = "pageSize", value = "每页条数", example = "10", dataType = "Integer")
    })
    public ResponseWrap<Page<OrderVo>> findOrderVoPage(
        @RequestBody OrderVo orderVo,
        @PathVariable("pageNum") int pageNum,
        @PathVariable("pageSize") int pageSize) throws ProjectException {
        Page<OrderVo> orderVoList = orderFace.findOrderVoPage(orderVo, pageNum, pageSize);
        return ResponseWrapBuild.build(OrderEnum.SUCCEED, orderVoList);
    }

    @PostMapping("opertion-to-orderItem/{dishId}/{orderNo}/{opertionType}")
    @ApiOperation(value = "操作订单菜品数量",notes = "操作订单菜品数量")
    @ApiImplicitParams({
        @ApiImplicitParam(paramType = "path",name = "dishId",value = "菜品ID",dataType = "Long"),
        @ApiImplicitParam(paramType = "path",name = "orderNo",value = "订单编号",dataType = "Long"),
        @ApiImplicitParam(paramType = "path",name = "opertionType",value = "操作动作",example = "ADD",dataType = "String")
    })
    public ResponseWrap<OrderVo> opertionToOrderItem(
        @PathVariable("dishId") Long dishId,
        @PathVariable("orderNo") Long orderNo,
        @PathVariable("opertionType") String opertionType) throws ProjectException {
        OrderVo orderVo = orderFace.opertionToOrderItem(dishId,orderNo,opertionType);
        return ResponseWrapBuild.build(BrandEnum.SUCCEED,orderVo);
    }

    @PostMapping("handleTrading")
    @ApiOperation(value = "订单结算",notes = "订单结算")
    @ApiImplicitParam(name = "orderVo",value = "订单信息",dataType = "OrderVo")
    public ResponseWrap<TradingVo> handleTrading(@RequestBody OrderVo orderVo){
        //1、获得结算人信息
        String userVoString = UserVoContext.getUserVoString();
        UserVo userVo = JSONObject.parseObject(userVoString, UserVo.class);
        orderVo.setCashierId(userVo.getId());
        orderVo.setCashierName(userVo.getUsername());
        return ResponseWrapBuild.build(BrandEnum.SUCCEED,orderFace.handleTrading(orderVo));
    }

    @PostMapping("handle-trading-refund")
    @ApiOperation(value = "订单退款",notes = "订单退款")
    @ApiImplicitParam(name = "orderVo",value = "订单信息",dataType = "OrderVo")
    public ResponseWrap<Boolean> handleTradingRefund(@RequestBody OrderVo orderVo){
        //1、获得当前订单结算人信息
        String userVoString = UserVoContext.getUserVoString();
        UserVo userVo = JSONObject.parseObject(userVoString, UserVo.class);
        orderVo.setCashierId(userVo.getId());
        orderVo.setCashierName(userVo.getUsername());
        Boolean flag = orderFace.handleTradingRefund(orderVo);
        return ResponseWrapBuild.build(BrandEnum.SUCCEED,flag);
    }

}