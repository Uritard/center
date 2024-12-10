/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.utils.HttpClientUtils;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.configuration.RabbitMqConfig;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.uphandler.UpHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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

    private static final String PIC_UPLOAD_URL = "/distribute/data/service/taskResultPic/receive";

    @Resource
    private UrlPathHandler urlPathHandler;

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
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            Map params = new HashMap(8);
            String filePaths = String.valueOf(item.get("file_path"));
            String[] split = filePaths.split(",");
            for (String filePath : split) {
                params.put("file", new File(filePath) );
                params.put("planNo", String.valueOf(item.get("task_code")));
                params.put("taskNo", String.valueOf(item.get("task_patrolled_id")));
                params.put("infoCode", String.valueOf(item.get("patroldevice_code")));
                params.put("source", 1);
                HttpClientUtils.getInstance().doPost(url, params);
            }
        }
    }

    private void uploadResult(XMLBaseModel xmlBaseModel) {
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            JSONObject param = new JSONObject();
            UUID uuid = UUID.randomUUID();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(String.valueOf(item.get("time")), formatter);
            param.put("dataId", uuid);
            param.put("planNo", String.valueOf(item.get("task_code")));
            param.put("taskNo", String.valueOf(item.get("task_patrolled_id")));
            param.put("dataTime", localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            param.put("dataUnit", String.valueOf(item.get("unit")));
            param.put("dataValue", String.valueOf(item.get("value")));
            param.put("dataStatus", "1");
            param.put("pointStatus", Objects.nonNull(item.get("valid")) ? String.valueOf(item.get("valid")) : "0");
            param.put("infoCode", String.valueOf(item.get("patroldevice_code")));
            param.put("inspectionType", "1".equals(String.valueOf(item.get("data_type"))) ? "1" : "0");
            param.put("stationCode", "");
            param.put("lineCode", "");
            param.put("patrolDeviceUniqueCode", String.valueOf(item.get("patroldevice_code")));
            param.put("year", localDateTime.getYear());
            param.put("month", localDateTime.getMonthValue());
            param.put("day", localDateTime.getDayOfMonth());
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY, param.toJSONString());
        }
    }

}
