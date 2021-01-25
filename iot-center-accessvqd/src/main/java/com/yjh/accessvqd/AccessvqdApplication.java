package com.yjh.accessvqd;

import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import com.yjh.accessvqd.module.diagnose.service.PlansService;
import com.yjh.accessvqd.socket.SocketServerListenHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * @Description
 * @Author tt
 * @Date 2020/6/18
 **/
@SpringBootApplication(scanBasePackages = {"com.yjh.accessvqd", "com.yjh.accessvqd.commons.logs"})
@ComponentScan(nameGenerator = AnnotationBeanNameGenerator.class,basePackages = "com.yjh")
@EnableFeignClients
@Slf4j
public class AccessvqdApplication implements CommandLineRunner {

    @Autowired
    private ChanResultService chanResultService;

    @Autowired
    private TDiagnosePlanDao tDiagnosePlanDao;

    @Autowired
    private RedisTemplate redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AccessvqdApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        SocketServerListenHandler socketServerListenHandler=new SocketServerListenHandler(18725,chanResultService,tDiagnosePlanDao,redisTemplate);
        socketServerListenHandler.listenClientConnect();
        log.info("socket服务开启------------------------");
    }


//    @Bean
//    public static ConfigureRedisAction configureRedisAction() {
//        return ConfigureRedisAction.NO_OP;
//    }


}
