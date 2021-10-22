package com.itheima.restkeeper.adapter.impl;

import com.itheima.restkeeper.adapter.SmsSendAdapter;
import com.itheima.restkeeper.balancer.SendLoadBalancer;
import com.itheima.restkeeper.constant.SuperConstant;
import com.itheima.restkeeper.handler.SmsSendHandler;
import com.itheima.restkeeper.pojo.SmsBlacklist;
import com.itheima.restkeeper.pojo.SmsChannel;
import com.itheima.restkeeper.pojo.SmsSendRecord;
import com.itheima.restkeeper.pojo.SmsTemplate;
import com.itheima.restkeeper.service.ISmsBlacklistService;
import com.itheima.restkeeper.service.ISmsChannelService;
import com.itheima.restkeeper.service.ISmsTemplateService;
import com.itheima.restkeeper.utils.EmptyUtil;
import com.itheima.restkeeper.utils.RegisterBeanHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName SmsSendAdapterImpl.java
 * @Description 短信适配器实现
 */
@Slf4j
@Component
public class SmsSendAdapterImpl implements SmsSendAdapter {

    @Autowired
    ISmsBlacklistService smsBlacklistService;

    @Autowired
    ISmsTemplateService smsTemplateService;

    @Autowired
    ISmsChannelService smsChannelService;

    @Autowired
    RegisterBeanHandler registerBeanHandler;

    private static Map<String,String> sendHandlers =new HashMap<>();

    private static Map<String,String> loadBalancers =new HashMap<>();

    static {
        sendHandlers.put(SuperConstant.ALIYUN_SMS,"aliyunSmsSendHandler");
        sendHandlers.put(SuperConstant.TENCENT_SMS,"tencentSmsSendHandler");
        sendHandlers.put(SuperConstant.BAIDU_SMS,"baiduSmsSendHandler");
        loadBalancers.put(SuperConstant.HASH,"hashSend");
        loadBalancers.put(SuperConstant.RANDOM,"randomSend");
        loadBalancers.put(SuperConstant.ROUND_ROBIN,"roundRobinSend");
        loadBalancers.put(SuperConstant.WEIGHT_RANDOM,"weightRandomSend");
        loadBalancers.put(SuperConstant.WEIGHT_ROUND_ROBIN,"weightRoundRobinSend");
    }

    @Override
    public Boolean SendSms(
                    String templateNo,
                    String loadBalancerType,
                    Set<String> mobiles,
                    LinkedHashMap<String, String> templateParam) throws Exception {
        //过滤黑名单中电话号码
        List<SmsBlacklist> smsBlacklists = smsBlacklistService.list();
        if (!EmptyUtil.isNullOrEmpty(smsBlacklists)){
            Set<String> smsBlackMobiles= smsBlacklists.stream()
                .map(SmsBlacklist::getMobile)
                .collect(Collectors.toSet());
            mobiles.removeAll(smsBlackMobiles);
        }
        Boolean flag = false;
        //获得审核通过模板模板
        List<SmsTemplate> smsTemplates = smsTemplateService.findSmsTemplateByTemplateNo(templateNo);
        if (EmptyUtil.isNullOrEmpty(smsTemplates)){
            log.warn("模板templateNo：{}，未定义",templateNo);
            return flag;
        }
        //负载均衡器选择对应通道
        String stringLoadBalancers = loadBalancers.get(loadBalancerType);
        SendLoadBalancer sendLoadBalancer = registerBeanHandler.getBean(stringLoadBalancers, SendLoadBalancer.class);
        String channelLabel = sendLoadBalancer.chooseChannel(smsTemplates,mobiles);;
        if (EmptyUtil.isNullOrEmpty(channelLabel)){
            log.error("模板templateNo：{}，负载均衡器未找到对应通道",templateNo);
            return flag;
        }
        //查询渠道
        SmsChannel smsChannel= smsChannelService.findChannelByChannelLabel(channelLabel);
        //选择模板
        SmsTemplate smsTemplate = smsTemplates.stream()
            .filter(n->n.getChannelLabel().equals(smsChannel.getChannelLabel()))
            .findFirst().get();
        //发送短信
        String stringSendHandler = sendHandlers.get(channelLabel);
        SmsSendHandler smsSendHandler =registerBeanHandler.getBean(stringSendHandler,SmsSendHandler.class);
        return smsSendHandler.SendSms(smsTemplate,smsChannel,mobiles,templateParam);
    }

    @Override
    public Boolean querySendSms(SmsSendRecord smsSendRecord) throws Exception {
        String channelLabel = smsSendRecord.getChannelLabel();
        String stringSendHandler = sendHandlers.get(channelLabel);
        SmsSendHandler smsSendHandler =registerBeanHandler.getBean(stringSendHandler,SmsSendHandler.class);
        return smsSendHandler.querySendSms(smsSendRecord);
    }

    @Override
    public Boolean retrySendSms(SmsSendRecord smsSendRecord) throws Exception {
        String channelLabel = smsSendRecord.getChannelLabel();
        String stringSendHandler = sendHandlers.get(channelLabel);
        SmsSendHandler smsSendHandler =registerBeanHandler.getBean(stringSendHandler,SmsSendHandler.class);
        return smsSendHandler.retrySendSms(smsSendRecord);
    }

}