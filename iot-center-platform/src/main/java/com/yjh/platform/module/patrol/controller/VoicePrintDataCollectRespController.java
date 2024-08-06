package com.yjh.platform.module.patrol.controller;

import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.entity.enums.VoiceType;
import com.yjh.platform.module.patrol.entity.voice.*;
import com.yjh.platform.module.patrol.service.PatrolResultHandler;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.patrol.service.VoicePrintDataCollectRespService;
import com.yjh.platform.module.patrol.thread.CruiseRedisStorage;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URL;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.AbnormalResDescEnum.ANALYSE_REQFAILED;
import static com.yjh.platform.module.patrol.CruiseConstant.AbnormalResDescEnum.ANALYSISING;
import static com.yjh.platform.module.patrol.CruiseConstant.*;

/**
 * @Author: lqh
 * @Date: 2023/09/19
 */
@Slf4j
@RestController
@RequestMapping("/")
@Api(value = "/", tags = "接受http声纹数据")
public class VoicePrintDataCollectRespController {

    public static Map<String, Map<String, String>> requestMapCruiseMap = new HashMap<>();

    @Autowired
    private VoicePrintDataCollectRespService voicePrintDataCollectRespService;

    @PostMapping(value = "voiceprintDataCollectRetNotify")
    public Map<String,Object> voiceprintDataCollectRetNotify(@RequestBody VoicePrintDataCollectRetNotifyResp resp) {

        return voicePrintDataCollectRespService.voiceprintDataCollectRetNotify(resp);
    }


    @PostMapping(value = "voiceprintAnalyseRetNotify")
    public Map<String,Object> voiceprintAnalyseRetNotify(@RequestBody VoicePrintAnalyseRetNotifyResp resp) {
        return voicePrintDataCollectRespService.voiceprintAnalyseRetNotify(resp);
    }
}
