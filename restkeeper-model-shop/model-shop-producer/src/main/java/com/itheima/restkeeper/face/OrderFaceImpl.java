package com.itheima.restkeeper.face;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.restkeeper.OrderFace;
import com.itheima.restkeeper.TableFace;
import com.itheima.restkeeper.TradingFace;
import com.itheima.restkeeper.constant.AppletCacheConstant;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.enums.OpenTableEnum;
import com.itheima.restkeeper.enums.OrderEnum;
import com.itheima.restkeeper.enums.OrderItemEnum;
import com.itheima.restkeeper.enums.ShoppingCartEnum;
import com.itheima.restkeeper.exception.ProjectException;
import com.itheima.restkeeper.pojo.Dish;
import com.itheima.restkeeper.pojo.Order;
import com.itheima.restkeeper.pojo.OrderItem;
import com.itheima.restkeeper.req.OrderItemVo;
import com.itheima.restkeeper.req.OrderVo;
import com.itheima.restkeeper.req.TableVo;
import com.itheima.restkeeper.req.TradingVo;
import com.itheima.restkeeper.service.IDishService;
import com.itheima.restkeeper.service.IOrderItemService;
import com.itheima.restkeeper.service.IOrderService;
import com.itheima.restkeeper.utils.BeanConv;
import com.itheima.restkeeper.utils.EmptyUtil;
import com.itheima.restkeeper.utils.ExceptionsUtil;
import com.itheima.restkeeper.utils.ResponseWrapBuild;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName OrderFaceImpl.java
 * @Description 订单服务实现
 */
@Slf4j
@DubboService(version = "${dubbo.application.version}",timeout = 5000,
    methods ={
        @Method(name = "findOrderVoPage",retries = 2),
        @Method(name = "opertionToOrderItem",retries = 0),
        @Method(name = "handleTrading",retries = 0),
        @Method(name = "findOrderVoPaid",retries = 2),
        @Method(name = "findOrderVoPaying",retries = 2),
        @Method(name = "synchTradingState",retries = 2)

    })
public class OrderFaceImpl implements OrderFace {

    @DubboReference(version = "${dubbo.application.version}", check = false)
    TradingFace tradingFace;

    @DubboReference(version = "${dubbo.application.version}", check = false)
    TableFace tableFace;

    @Autowired
    IOrderService orderService;

    @Autowired
    IOrderItemService orderItemService;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    IDishService dishService;

    @Override
    public Page<OrderVo> findOrderVoPage(OrderVo orderVo,
                                         int pageNum,
                                         int pageSize) throws ProjectException{
        try {
            Page<Order> page = orderService.findOrderVoPage(orderVo, pageNum, pageSize);
            Page<OrderVo> pageVo = new Page<>();
            BeanConv.toBean(page,pageVo);
            //结果集转换
            List<Order> orderList = page.getRecords();
            List<OrderVo> orderVoList = BeanConv.toBeanList(orderList,OrderVo.class);
            //处理订单项
            if (!EmptyUtil.isNullOrEmpty(orderVoList)){
                orderVoList.forEach(n->{
                    List<OrderItem> orderItems = orderItemService.findOrderItemByOrderNo(n.getOrderNo());
                    List<OrderItemVo> orderItemVoList = BeanConv.toBeanList(orderItems, OrderItemVo.class);
                    n.setOrderItemVoStatisticsList(orderItemVoList);
                });
            }
            pageVo.setRecords(orderVoList);
            return pageVo;
        } catch (Exception e) {
            log.error("查询订单列表异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new ProjectException(OrderEnum.PAGE_FAIL);
        }
    }

    @Override
    @GlobalTransactional
    public OrderVo opertionToOrderItem(Long dishId,
                                       Long orderNo,
                                       String opertionType)throws ProjectException {
        //1、判定订单待支付状态才可操作
        OrderVo orderVoResult = orderService.findOrderByOrderNo(orderNo);
        if (!SuperConstant.DFK.equals(orderVoResult.getOrderState())){
            throw new ProjectException(OrderEnum.STATUS_FAIL);
        }
        //2、判定菜品处于起售状态才可操作
        Dish dish = dishService.getById(dishId);
        if (!SuperConstant.YES.equals(dish.getEnableFlag())||
                !SuperConstant.YES.equals(dish.getDishStatus())){
            throw new ProjectException(OrderEnum.DISH_STATUS_FAIL);
        }
        //3、锁定订单
        String keyOrderItem = AppletCacheConstant.ADD_TO_ORDERITEM_LOCK+orderNo;
        RLock lockOrderItem = redissonClient.getLock(keyOrderItem);
        try {
            if (lockOrderItem.tryLock(
                AppletCacheConstant.REDIS_WAIT_TIME,
                AppletCacheConstant.REDIS_LEASETIME,
                TimeUnit.SECONDS)){
                String key = AppletCacheConstant.REPERTORY_DISH+dishId;
                RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
                //4.1添加可核算订单项
                if (opertionType.equals(SuperConstant.OPERTION_TYPE_ADD)){
                    this.addToOrderItem(dishId,orderNo,atomicLong);
                }
                //4.2、移除可核算订单项
                if (opertionType.equals(SuperConstant.OPERTION_TYPE_REMOVE)){
                    this.removeToOrderItem(dishId,orderNo,atomicLong);
                }
                //5、计算订单总金额
                List<OrderItem> orderItemListResult = orderItemService.findOrderItemByOrderNo(orderNo);
                BigDecimal sumPrice = orderItemListResult.stream()
                    .map(n->{
                        BigDecimal price = n.getPrice();
                        BigDecimal reducePrice = n.getReducePrice();
                        Long dishNum = n.getDishNum();
                        //如果有优惠价格以优惠价格计算
                        if (EmptyUtil.isNullOrEmpty(reducePrice)){
                            return price.multiply(new BigDecimal(dishNum));
                        }else {
                            return reducePrice.multiply(new BigDecimal(dishNum));
                        }
                    }
                ).reduce(BigDecimal.ZERO, BigDecimal::add);
                orderVoResult.setPayableAmountSum(sumPrice);
                //6、修改订单总金额
                OrderVo orderVoHandler = OrderVo.builder()
                        .id(orderVoResult.getId())
                        .payableAmountSum(sumPrice).build();
                boolean flag = orderService.updateById(BeanConv.toBean(orderVoHandler, Order.class));
                if (!flag){
                    throw new ProjectException(OrderItemEnum.SAVE_ORDER_FAIL);
                }
                //7、返回新订单信息
                if (!EmptyUtil.isNullOrEmpty(orderVoResult)){
                    List<OrderItemVo> orderItemVoStatisticsList = BeanConv
                            .toBeanList(orderItemListResult, OrderItemVo.class);
                    orderVoResult.setOrderItemVoStatisticsList(orderItemVoStatisticsList);
                    return orderVoResult;
                }
            }
            return orderVoResult;
        } catch (InterruptedException e) {
            log.error("操作订单信息异常：{}", ExceptionsUtil.getStackTraceAsString(e));
            throw new ProjectException(OrderEnum.OPERTION_SHOPPING_CART_FAIL);
        }finally {
            lockOrderItem.unlock();
        }
    }

    /**
     * @description 移除订单项
     * @param dishId 菜品ID
     * @param orderNo 订单编号
     * @param atomicLong 原子计数器
     * @return
     */
    private void removeToOrderItem(Long dishId, Long orderNo, RAtomicLong atomicLong)  {
        //1、修改订单项
        Boolean flagOrderItem  = orderItemService.updateDishNum(-1L, dishId,orderNo);
        //2、菜品库存
        Boolean flagDish = dishService.updateDishNumber(1L,dishId);
        if (!flagOrderItem||!flagDish){
            throw new ProjectException(ShoppingCartEnum.UPDATE_DISHNUMBER_FAIL);
        }
        //3、添加缓存中的库存数量
        atomicLong.incrementAndGet();
    }

    /**
     * @description 添加可核算订单项
     * @param dishId 菜品ID
     * @param orderNo 订单编号
     * @param atomicLong 原子计数器
     * @return
     */
    private void addToOrderItem(Long dishId, Long orderNo, RAtomicLong atomicLong)  {
        //1、如果库存够，redis减库存
        if (atomicLong.decrementAndGet()>=0){
            //2、修改可核算订单项
            Boolean flagOrderItem  = orderItemService.updateDishNum(1L, dishId,orderNo);
            //3、减菜品库存
            Boolean flagDish = dishService.updateDishNumber(-1L,dishId);
            if (!flagOrderItem||!flagDish){
                //4、减菜品库存失败，归还redis菜品库存
                atomicLong.incrementAndGet();
                throw new ProjectException(ShoppingCartEnum.UPDATE_DISHNUMBER_FAIL);
            }
        }else {
            //5、redis库存不足，虽然可以不处理，但建议还是做归还库存
            atomicLong.incrementAndGet();
            throw new ProjectException(ShoppingCartEnum.UNDERSTOCK);
        }
    }

    /***
     * @description 免单渠道，交易单生成
     * @param orderVo
     * @return TradingVo 交易单
     */
    private TradingVo freeChargeTradingVo(OrderVo orderVo){
        //结算保存订单信息
        Order order = Order.builder().id(orderVo.getId())
            .refund(new BigDecimal(0))
            .discount(new BigDecimal(10))
            .reduce(new BigDecimal(0))
            .orderState(SuperConstant.MD)
            .isRefund(SuperConstant.NO)
            .tradingChannel(orderVo.getTradingChannel())
            .build();
        boolean flag = orderService.updateById(order);
        TradingVo tradingVo = TradingVo.builder()
            .tradingAmount(orderVo.getPayableAmountSum())
            .tradingChannel(orderVo.getTradingChannel())
            .enterpriseId(orderVo.getEnterpriseId())
            .storeId(orderVo.getTableId())
            .payeeId(orderVo.getCashierId())
            .payeeName(orderVo.getCashierName())
            .productOrderNo(orderVo.getOrderNo())
            .tradingType(SuperConstant.TRADING_TYPE_MD)
            .memo(orderVo.getTableName()+":"+orderVo.getOrderNo())
            .build();
        return tradingVo;
    }

    /***
     * @description 退款渠道，交易单生成
     * @param orderVo
     * @return TradingVo 交易单
     */
    private TradingVo refundTradingVo(OrderVo orderVo){
        Order order = Order.builder().id(orderVo.getId())
            .refund(orderVo.getRefunded().add(orderVo.getOperTionRefund()))
            .isRefund(SuperConstant.YES).build();
        boolean flag = orderService.updateById(order);
        TradingVo tradingVo = TradingVo.builder()
            .tradingAmount(orderVo.getRealAmountSum())
            .tradingChannel(orderVo.getTradingChannel())
            .enterpriseId(orderVo.getEnterpriseId())
            .storeId(orderVo.getTableId())
            .payeeId(orderVo.getCashierId())
            .payeeName(orderVo.getCashierName())
            .productOrderNo(orderVo.getOrderNo())
            .tradingType(SuperConstant.TRADING_TYPE_TK)
            .memo(orderVo.getTableName()+":"+orderVo.getOrderNo())
            .build();
        tradingVo.setProductOrderNo(orderVo.getOrderNo());
        tradingVo.setRefund(orderVo.getRefund());
        tradingVo.setIsRefund(SuperConstant.YES);
        return tradingVo;
    }

    /***
     * @description 支付渠道，交易单生成
     * @param orderVo
     * @return TradingVo 交易单
     */
    private TradingVo payTradingVo(OrderVo orderVo){
        //应付总金额
        BigDecimal payableAmountSum = orderVo.getPayableAmountSum();
        //打折
        BigDecimal discount = orderVo.getDiscount();
        //优惠
        BigDecimal reduce = orderVo.getReduce();
        //计算实付总金额
        BigDecimal realAmountSum = payableAmountSum.multiply(discount.divide(new BigDecimal(10))).subtract(reduce);
        //更新订单状态
        Order order = Order.builder().id(orderVo.getId())
            .realAmountSum(realAmountSum)
            .cashierId(orderVo.getCashierId())
            .cashierName(orderVo.getCashierName())
            .tradingChannel(orderVo.getTradingChannel())
            .orderState(SuperConstant.FKZ)
            .isRefund(SuperConstant.NO).build();
        boolean flag = orderService.updateById(order);
        TradingVo tradingVo = null;
        //构建交易单
        if (flag){
            tradingVo = TradingVo.builder()
                .tradingAmount(realAmountSum)
                .tradingChannel(orderVo.getTradingChannel())
                .enterpriseId(orderVo.getEnterpriseId())
                .storeId(orderVo.getTableId())
                .payeeId(orderVo.getCashierId())
                .payeeName(orderVo.getCashierName())
                .productOrderNo(orderVo.getOrderNo())
                .tradingType(SuperConstant.TRADING_TYPE_FK)
                .memo(orderVo.getTableName()+":"+orderVo.getOrderNo())
                .build();
        }
        return tradingVo;
    }

    @Override
    @GlobalTransactional
    public TradingVo handleTrading(OrderVo orderVo) throws ProjectException{
        //2、根据订单生成交易单
        TradingVo tradingVo = tradingConvertor(orderVo);
        if (EmptyUtil.isNullOrEmpty(tradingVo)){
            throw new ProjectException(OrderEnum.FAIL);
        }
        //3、调用支付RPC接口，进行支付
        TradingVo tradingVoResult = tradingFace.doPay(tradingVo);
        //4、结算后桌台状态修改：开桌-->空闲
        Boolean flag = true;
        if (EmptyUtil.isNullOrEmpty(tradingVoResult)){
            throw new ProjectException(OrderEnum.FAIL);
        }else {
            TableVo tableVo = TableVo.builder()
                .id(orderVo.getTableId())
                .tableStatus(SuperConstant.FREE).build();
            flag = tableFace.updateTable(tableVo);
            if (!flag){
                throw new ProjectException(OrderEnum.FAIL);
            }
        }
        return tradingVoResult;
    }

    @Override
    @GlobalTransactional
    public Boolean handleTradingRefund(OrderVo orderVo)throws ProjectException {
        //2、获取当前交易单信息
        OrderVo orderVoBefore = findOrderVoPaid(orderVo.getOrderNo());
        //3、退款收款人不为同一人，退款操作拒绝
        if (orderVo.getTradingChannel().equals(SuperConstant.TRADING_CHANNEL_REFUND)
            &&orderVo.getCashierId().longValue()!=orderVoBefore.getCashierId().longValue()){
            throw new ProjectException(OrderEnum.REFUND_FAIL);
        }
        //4、当前交易单信息，退款操作拒绝
        if (!SuperConstant.YJS.equals(orderVoBefore.getOrderState())){
            throw new ProjectException(OrderEnum.REFUND_FAIL);
        }
        //5、根据订单生成交易单
        TradingVo tradingVo = tradingConvertor(orderVo);
        if (EmptyUtil.isNullOrEmpty(tradingVo)){
            throw new ProjectException(OrderEnum.FAIL);
        }
        //6、执行退款交易
        TradingVo tradingVoResult = tradingFace.doPay(tradingVo);
        boolean flag = true;
        if (EmptyUtil.isNullOrEmpty(tradingVoResult)){
            throw new ProjectException(OrderEnum.FAIL);
        }
        return flag;
    }

    /***
     * @description 转换订单为交易单
     * @param orderVo 订单信息
     * @return: com.itheima.restkeeper.req.TradingVo
     */
    private TradingVo tradingConvertor(OrderVo orderVo)throws ProjectException {
        //免单渠道，交易单生成
        if (orderVo.getTradingChannel().equals(SuperConstant.TRADING_CHANNEL_FREE_CHARGE)){
            return freeChargeTradingVo(orderVo);
        //退款渠道，交易单生成
        }else if (orderVo.getTradingChannel().equals(SuperConstant.TRADING_CHANNEL_REFUND)){
            return refundTradingVo(orderVo);
        //支付渠道，交易单生成
        }else {
            return payTradingVo(orderVo);
        }
    }

    @Override
    public OrderVo findOrderVoPaid(Long orderNo)throws ProjectException {
        return orderService.findOrderVoPaid(orderNo);
    }

    @Override
    public List<OrderVo> findOrderVoPaying() throws ProjectException{
        return orderService.findOrderVoPaying();
    }

    @Override
    @GlobalTransactional
    public Boolean synchTradingState(Long orderNo, String tradingState)throws ProjectException {
        OrderVo orderVo = orderService.findOrderByOrderNo(orderNo);
        orderVo.setOrderState(tradingState);
        return orderService.saveOrUpdate(BeanConv.toBean(orderVo,Order.class));
    }

}
