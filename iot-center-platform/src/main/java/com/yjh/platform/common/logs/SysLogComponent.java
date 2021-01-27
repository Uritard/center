package com.yjh.platform.common.logs;

import com.yjh.platform.common.logs.track.HttpTracing;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * @author lichensi
 * @date 2020/12/10 17:59
 */
@Component
@Slf4j
public class SysLogComponent {

    @EventListener
    @Async
    public void recordLog(OperateLogEvent event){
        log.info("begin record operate log...");
        HttpTracing.setHttpRouteTrackInfo(event.getTrackInfo());
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            String response = serviceRestTemplate.postForObject("http://iot-center-logs/sysOperateLog/v1/add", event.getOperateLogDto(), String.class);
            log.info("response: "+response);
        }catch (Exception e){
            log.error("record operate log failed : ", e);
        }finally {
            HttpTracing.removeHttpRouteTrackInfo();
        }
        log.info("record operate log finish...");
    }
}
