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
        String ftpsBase = (String) redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content");
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            Map<String, Object> params = new HashMap<>(8);
            String filePaths = String.valueOf(item.get("file_path"));
            if (StringUtils.isEmpty(filePaths)) {
                log.warn("当前测点没有文件需要传输");
                continue;
            }
            String[] split = filePaths.split(",");
            String taskCode = MapUtils.getString(item,"task_code");
            for (String filePath : split) {
                try {
                    File file = new File(FileUtil.concatPath(ftpsBase, filePath));
                    if (!file.isFile() || !file.exists()) {
                        log.error("文件不存在: {}", file.getAbsolutePath());
                        continue;
                    }
                    params.put("file", file);
                    params.put("planNo", tekUpTransforServer.getTaskPlanCode(taskCode));
                    params.put("taskNo", taskCode);
                    params.put("infoCode", MapUtils.getString(item,"device_id"));
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
            if (NumberUtil.isNumber(StringUtils.substringBefore(value, ","))) {
                param.put("dataValue", value);
                param.put("textValue", "");
            } else {
                param.put("dataValue", "");
                param.put("textValue", value);
            }
            if (Constant.ONE.equals(MapUtils.getString(item, "valid"))) {
                param.put("dataStatus", Constant.ONE);
            } else {
                param.put("dataStatus", Constant.ZERO);
            }
            param.put("pointStatus", AbnormalResDescEnum.contains(value) ? "3" : "2");
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

    enum AbnormalResDescEnum{
        /**
         * 机器人离线,未执行
         */
        ROBOT_OFFLINE("机器人离线,未执行"),
        /**
         * 无人机离线,未执行
         */
        DRONE_OFFLINE("无人机离线,未执行"),
        /**
         * 机器人处于检修状态,未执行
         */
        ROBOT_OVERHAUL("机器人处于检修状态,未执行"),
        /**
         * 无人机处于检修状态,未执行
         */
        DRONE_OVERHAUL("无人机处于检修状态,未执行"),
        /**
         * 设备操作任务中
         */
        EQUIPMENT_OPERATE("机器人操作任务中,未执行"),
        /**
         * 设备检修中
         */
        EQUIPMENT_MAINTENANCE("设备检修中"),
        /**
         * 任务超期
         */
        TASK_TIMEOUT("任务超期"),
        /**
         * 任务终止
         */
        TERMINATION_OF_TASK("任务终止"),
        /**
         * 机器人任务异常
         */
        ROBOT_ANOMALY_TASK("机器人任务异常"),
        /**
         * 无人机任务异常
         */
        DRONE_ANOMALY_TASK("无人机任务异常"),
        /**
         * 可见光相机任务异常
         */
        VIDEO_ANOMALY_TASK("可见光相机任务异常"),
        /**
         * 红外相机任务异常
         */
        INFRARED_ANOMALY_TASK("红外相机任务异常"),
        /**
         * 声纹任务异常
         */
        VOICE_ANOMALY_TASK("声纹任务异常"),
        /**
         * 任务异常
         */
        ANOMALY_TASK("任务异常");

        public String getDesc() {
            return desc;
        }

        @Override
        public String toString(){
            return desc;
        }

        final String desc;

        AbnormalResDescEnum(String desc){
            this.desc = desc;
        }

        private static final Map<String, AbnormalResDescEnum> ABNORMAL_ENUM_MAP = new HashMap<>();

        static {
            for (AbnormalResDescEnum value : AbnormalResDescEnum.values()) {
                ABNORMAL_ENUM_MAP.put(value.getDesc(), value);
            }
        }

        public static boolean contains(String desc) {
            return ABNORMAL_ENUM_MAP.containsKey(desc);
        }
    }
}
