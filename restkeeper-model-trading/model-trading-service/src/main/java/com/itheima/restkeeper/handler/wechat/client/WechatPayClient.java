package com.itheima.restkeeper.handler.wechat.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.itheima.restkeeper.utils.ExceptionsUtil;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @ClassName WechatPayClient.java
 * @Description 功能列表
 */
@Slf4j
public class WechatPayClient {

    //appId
    String appid;

    //商户号
    String mchId;

    //私钥字符串
    String privateKey;

    //商户证书序列号
    String mchSerialNo;

    //V3密钥
    String apiV3Key;

    /***
     * @description 构建客户端
     *
     * @param mchId  商户号
     * @param privateKey 私钥字符串
     * @param mchSerialNo 商户证书序列号
     * @param apiV3Key V3密钥
     * @return
     */
    @Builder
    public WechatPayClient(String appid,
                           String mchId,
                           String privateKey,
                           String mchSerialNo,
                           String apiV3Key) {
        this.appid = appid;
        this.mchId = mchId;
        this.privateKey = privateKey;
        this.mchSerialNo = mchSerialNo;
        this.apiV3Key = apiV3Key;
    }

    /***
     * @description 统一收单线下交易预创建
     * @param outTradeNo 发起支付传递的交易单号
     * @param amount 交易金额
     * @param description 商品描述
     * @return
     */
    public String preCreate(String outTradeNo,String amount,String description){
        //请求地址
        String domain ="/v3/pay/transactions/native";
        WechatPayHttpClient httpClient = WechatPayHttpClient.builder()
                .mchId(mchId)
                .mchSerialNo(mchSerialNo)
                .apiV3Key(apiV3Key)
                .privateKey(privateKey)
                .domain(domain)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode bodyParams = objectMapper.createObjectNode();
        bodyParams.put("mchid",mchId)
                  .put("appid",appid )
                  .put("description", description)
                  .put("notify_url", "https://www.weixin.qq.com/wxpay/pay.php")
                  .put("out_trade_no", outTradeNo);
        bodyParams.putObject("amount")
                  .put("total", Integer.valueOf(amount));
        try {
            return httpClient.doPost(bodyParams);
        } catch (IOException e) {
            log.error("微信支付：preCreate，发生异常：{}", ExceptionsUtil.getStackTraceAsString(e));
        }
        return null;
    }

    /***
     * @description 统一收单线下交易查询
     * @param outTradeNo 发起支付传递的交易单号
     * @return
     */
    public String query(String outTradeNo){
        //请求地址
        String domain ="/v3/pay/transactions";
        WechatPayHttpClient httpClient = WechatPayHttpClient.builder()
                .mchId(mchId)
                .mchSerialNo(mchSerialNo)
                .apiV3Key(apiV3Key)
                .privateKey(privateKey)
                .domain(domain)
                .build();
        try {
            //uri参数对象
            String uriParams ="/"+mchId+"/"+outTradeNo;
            return httpClient.doGet(uriParams);
        } catch (IOException | URISyntaxException e) {
            log.error("微信支付：preCreate，发生异常：{}", ExceptionsUtil.getStackTraceAsString(e));
        }
        return null;
    }

    /***
     * @description 统一收单交易退款接口
     * @param outTradeNo 发起支付传递的交易单号
     * @param amount 退款金额
     * @param outRefundNo 商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔
     * @return
     */
    public String refund(String outTradeNo,String amount,String outRefundNo){
        //请求地址
        String domain ="/v3/refund/domestic/refunds";
        WechatPayHttpClient httpClient = WechatPayHttpClient.builder()
                .mchId(mchId)
                .mchSerialNo(mchSerialNo)
                .apiV3Key(apiV3Key)
                .privateKey(privateKey)
                .domain(domain)
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode bodyParams = objectMapper.createObjectNode();
        bodyParams.put("out_refund_no", outRefundNo)
                  .put("out_trade_no", outTradeNo);
        bodyParams.putObject("amount")
                  .put("refund", Integer.valueOf(amount));
        try {
            return httpClient.doPost(bodyParams);
        } catch (IOException e) {
            log.error("微信支付：refund，发生异常：{}", ExceptionsUtil.getStackTraceAsString(e));
        }
        return null;
    }

    /***
     * @description 统一收单交易退款接口查询
     * @param outRefundNo 商户系统内部的退款单号
     * @return
     */
    public String queryRefund(String outRefundNo){
        //请求地址
        String domain ="/v3/refund/domestic/refunds";
        WechatPayHttpClient httpClient = WechatPayHttpClient.builder()
                .mchId(mchId)
                .mchSerialNo(mchSerialNo)
                .apiV3Key(apiV3Key)
                .privateKey(privateKey)
                .domain(domain)
                .build();
        try {
            String uriParams ="/"+outRefundNo;
            return httpClient.doGet(uriParams);
        } catch (IOException | URISyntaxException e) {
            log.error("微信支付：queryRefund，发生异常：{}", ExceptionsUtil.getStackTraceAsString(e));
        }
        return null;
    }


}
