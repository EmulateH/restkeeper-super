package com.itheima.restkeeper.init;

import com.itheima.restkeeper.constant.AppletCacheConstant;
import com.itheima.restkeeper.pojo.Dish;
import com.itheima.restkeeper.service.IDishService;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * @ClassName InitDish.java
 * @Description 初始化菜品库存
 */
@Component
public class InitDish {


}
