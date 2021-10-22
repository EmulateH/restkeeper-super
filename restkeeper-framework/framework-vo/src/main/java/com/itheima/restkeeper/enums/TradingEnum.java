package com.itheima.restkeeper.enums;

import com.itheima.restkeeper.basic.IBasicEnum;

/**
 * @ClassName TradingEnum.java
 * @Description TODO
 */
public enum TradingEnum implements IBasicEnum {

    SUCCEED("200","操作成功"),
    CHECK_TRADING_FAIL("43001","交易单校验失败"),
    TRYLOCK_TRADING_FAIL("43002", "交易单加锁失败"),
    PAYING_TRADING_FAIL("43003", "交易单支付失败"),
    TRADING_STATE_SUCCEED("43004", "交易单已完成"),
    TRADING_STATE_PAYING("43005", "交易单交易中"),
    ENTERPRISEID_EMPT("43006", "企业号为空"),
    FACE_TO_FACE_FAIL("43007", "统一下单失败"),
    QUERY_FACE_TO_FACE_FAIL("43008", "查询统一下单失败"),
    SAVE_OR_UPDATE_FAIL("43009", "交易单保存或修改失败"),
    TRADING_SUCCEED("43010", "SUCCEED")
    ;

    private String code;
    private String msg;

    TradingEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}