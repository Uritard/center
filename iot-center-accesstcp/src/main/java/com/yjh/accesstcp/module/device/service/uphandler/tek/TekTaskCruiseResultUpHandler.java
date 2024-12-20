/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.HttpClientUtils;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.file.FileUtil;
import com.yjh.accesstcp.configuration.RabbitMqConfig;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.uphandler.UpHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TekTaskCruiseResultUpHandler extends AbstractTekHandler {
    private final RabbitTemplate rabbitTemplate;
    private final UrlPathHandler urlPathHandler;
    private final TekUpTransforServer tekUpTransforServer;
    private final RedisTemplate redisTemplate;

    private static final String PIC_UPLOAD_URL = "/distribute/data/service/taskResultPic/receive";


    @Override
    public UpHandlerEnum subType() {
        return UpHandlerEnum.CRUISE_RESUL;
    }

    @Override
    public int sendUpHandler(XMLBaseModel xmlBaseModel) {
        // 图片上传
        this.uploadPic(xmlBaseModel, urlPathHandler.getTekUrl(PIC_UPLOAD_URL));
        // 结果上报
        this.uploadResult(xmlBaseModel);
        return Result.SUCCESS;
    }

    private void uploadPic(XMLBaseModel xmlBaseModel, String url) {
        String ftpsBase = (String)redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content");
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            Map<String, Object> params = new HashMap<>(8);
            String filePaths = String.valueOf(item.get("file_path"));
            String[] split = filePaths.split(",");
            for (String filePath : split) {
                try {
                    params.put("file", new File(FileUtil.concatPath(ftpsBase, filePath)));
                    params.put("planNo", String.valueOf(item.get("task_code")));
                    params.put("taskNo", String.valueOf(item.get("task_patrolled_id")));
                    params.put("infoCode", String.valueOf(item.get("patroldevice_code")));
                    params.put("source", 1);
                    String result = HttpClientUtils.getInstance().doPost(url, params);
                    log.info("上报图片返回： {}", result);
                } catch (IOException e) {
                    log.error("上传文件失败", e);
                }
            }
        }
    }

    private void uploadResult(XMLBaseModel xmlBaseModel) {
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            JSONObject param = new JSONObject();
            LocalDateTime localDateTime = DateUtil.parseLocalDateTime((String) item.get("time"));
            String taskCode = MapUtils.getString(item,"task_code");
            param.put("dataId", StrUtil.uuid());
            param.put("planNo", tekUpTransforServer.getTaskPlanCode(taskCode));
            param.put("taskNo", taskCode);
            param.put("dataTime", localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            param.put("dataUnit", MapUtils.getString(item,"unit"));
            String value = MapUtils.getString(item,"value");
            if (NumberUtil.isNumber(value)) {
                param.put("dataValue", value);
                param.put("textValue", "");
            } else {
                param.put("dataValue", "");
                param.put("textValue", value);
            }
            param.put("dataStatus", "1");
            param.put("pointStatus", MapUtils.getString(item,"valid","0"));
            param.put("infoCode", MapUtils.getString(item,"device_id"));
            param.put("inspectionType", "1".equals(MapUtils.getString(item,"data_type")) ? "1" : "0");
            String[] edges = StringUtils.split(Constant.edgeCode(), "-", 2);
            param.put("stationCode", ArrayUtil.get(edges, 1));
            param.put("lineCode", ArrayUtil.get(edges, 0));
            param.put("patrolDeviceUniqueCode", Constant.edgeCode() + "-" + MapUtils.getString(item,"patroldevice_code"));
            param.put("year", localDateTime.getYear());
            param.put("month", localDateTime.getMonthValue());
            param.put("day", localDateTime.getDayOfMonth());

            String result = param.toJSONString();
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, result);
            log.info("上报任务结果进入mq： {}", result);
        }
    }

}
