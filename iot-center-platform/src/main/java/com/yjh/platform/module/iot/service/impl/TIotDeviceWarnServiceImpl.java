package com.yjh.platform.module.iot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.iot.dao.TIotDeviceMapper;
import com.yjh.platform.module.iot.dao.TIotDeviceWarnMapper;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.yjh.platform.module.iot.entity.TIotDeviceWarn;
import com.yjh.platform.module.iot.service.TIotDeviceWarnService;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import com.yjh.platform.module.user.dao.AlarmShieldDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.AlarmShield;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author YIJIAHE
 * @description 针对表【t_iot_device_warn(物联设备告警表)】的数据库操作Service实现
 * @createDate 2023-12-19 17:58:29
 */
@Service
@Slf4j
public class TIotDeviceWarnServiceImpl extends ServiceImpl<TIotDeviceWarnMapper, TIotDeviceWarn> implements TIotDeviceWarnService {

    @Resource
    private TRobotInfoDao tRobotInfoDao;
    @Resource
    private ApplicationProperties applicationProperties;
    @Resource
    private AlarmShieldDao alarmShieldDao;
    @Resource
    private TIotDeviceMapper tIotDeviceMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean robotIotWarn(Map<String, String> iotWarn) {
        String channelNum = iotWarn.get("type_device_num");
        String robotCode = iotWarn.get("robotCode");

        String ip = getDeviceIp(iotWarn, robotCode);
        if (StringUtils.isEmpty(ip)) {
            log.error("未获取到有效的ip信息");
            return false;
        }

        if (StringUtils.isBlank(channelNum)){
            log.error("type_device_num is null");
            return false;
        }

        String flag = "deleteFlag";
        TIotDeviceWarn tIotDeviceWarn = getBaseMapper().selectIdByIpAndNum(ip, channelNum);
        if (Objects.nonNull(tIotDeviceWarn)) {
            iotWarn.put("deviceId", String.valueOf(tIotDeviceWarn.getIotDeviceId()));
            if (StringUtils.isNotBlank(MapUtils.getString(iotWarn, flag))) {
                Long deleteFlag = Long.valueOf(MapUtils.getString(iotWarn, flag));
                tIotDeviceWarn.setDeleteTime(DateTimeUtil.getDate(iotWarn.get("deleteTime")));
                tIotDeviceWarn.setDeleteFlag(deleteFlag);
                this.updateById(tIotDeviceWarn);
                uploadWarn(iotWarn);
                return true;
            }
        } else {
            tIotDeviceWarn = getBaseMapper().selectDeviceByIpAndNum(ip, channelNum);
            if (Objects.nonNull(tIotDeviceWarn)) {
                iotWarn.put("deviceId", String.valueOf(tIotDeviceWarn.getIotDeviceId()));
                tIotDeviceWarn.setCreateTime(new Date());
                tIotDeviceWarn.setAlarmTime(DateTimeUtil.getDate(iotWarn.get("alarmTime")));
                tIotDeviceWarn.setRobotCode(robotCode);
                tIotDeviceWarn.setChannelNum(channelNum);
                tIotDeviceWarn.setAlarmContent(MapUtils.getString(iotWarn, "value"));
                long deleteFlag = 1L;
                if (StringUtils.isNotBlank(MapUtils.getString(iotWarn, flag))) {
                    deleteFlag = Long.parseLong(MapUtils.getString(iotWarn, flag));
                }
                tIotDeviceWarn.setDeleteFlag(deleteFlag);
                this.save(tIotDeviceWarn);
                uploadWarn(iotWarn);
            } else {
                log.warn("该设备未找到,告警不入库!");
                return false;
            }
        }

        //告警屏蔽判断
        AtomicReference<Boolean> isWarn = new AtomicReference<>(true);
        alarmShieldCheck(tIotDeviceWarn, isWarn);
        if (Boolean.TRUE.equals(isWarn.get())) {
            Map<String, String> jasonMaps = new HashMap<>(16);
            jasonMaps.put("type", "alarmPopUp");
            jasonMaps.put("warnLevel", "132");
            jasonMaps.put("warnId", String.valueOf(tIotDeviceWarn.getId()));
            jasonMaps.put("warnType", "2");
            jasonMaps.put("source", "iot");
            String json = JSON.toJSONString(jasonMaps);
            log.info("发送给前端的消息：{}", json);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
        }
        return true;
    }

    @Nullable
    private String getDeviceIp(Map<String, String> iotWarn, String robotCode) {
        String ip = iotWarn.get("deviceIp");
        if (StringUtils.isEmpty(ip)) {
            TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
            if (Objects.isNull(tRobotInfo)) {
                log.error("未找到 robotCode={} 的机器人信息", robotCode);
                return null;
            }

            if (StringUtils.isNotBlank(tRobotInfo.getRobotIp())) {
                ip = tRobotInfo.getRobotIp();
            } else {
                log.error("机器人未配置ip");
                return null;
            }
            iotWarn.put("deviceIp", ip);
        }
        return ip;
    }

    private void alarmShieldCheck(TIotDeviceWarn tIotDeviceWarn, AtomicReference<Boolean> isWarn) {
        List<AlarmShield> alarmShieldList = alarmShieldDao.selectAlarmShield(tIotDeviceWarn.getPointId(), tIotDeviceWarn.getAlarmContent());
        if (!alarmShieldList.isEmpty()) {
            alarmShieldList.forEach(alarmShield -> {
                Date now = new Date();
                Date endTime = alarmShield.getEndTime();
                if (alarmShield.getEnable() == 1 && now.before(endTime)) {
                    log.info("告警屏蔽：{}", alarmShield);
                    isWarn.set(false);
                }
            });
        }
    }

    @Override
    public TWarnInfoDetail selectIotDeviceWarn(Long warnId) {
        return getBaseMapper().selectIotDeviceWarn(warnId);
    }

    @Override
    public Boolean update(TIotDeviceWarn tIotDeviceWarn) {
        tIotDeviceWarn.setDeleteTime(new Date());
        boolean flag = this.updateById(tIotDeviceWarn);
        if (flag && applicationProperties.getUpSystemFtps().isEnable()) {
            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                try {
                    Map<String, List<XMLBaseModel>> map = Maps.newHashMap();
                    TIotDeviceWarn tdw = getBaseMapper().selectById(tIotDeviceWarn.getId());
                    List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
                    XMLBaseModel xmlBaseModel = new XMLBaseModel();
                    List<Map<String, Object>> itemList = new ArrayList<>();
                    Map<String, Object> item = Maps.newHashMap();
                    setDeviceInfo(item, tdw.getRobotCode(), tdw.getIotDeviceId());

                    item.put("alarm_time", DateTimeUtil.getDateTimeString(tdw.getAlarmTime()));
                    item.put("type_device_num", tdw.getChannelNum());
                    item.put("value", tdw.getAlarmContent());
                    item.put("delete_flag", 2);
                    item.put("delete_time", DateTimeUtil.getDateTimeString(tdw.getDeleteTime()));
                    itemList.add(item);
                    xmlBaseModel.setItems(itemList);
                    xmlBaseModel.setType("22");
                    xmlBaseModelList.add(xmlBaseModel);
                    map.put("list", xmlBaseModelList);
                    Constant.otherServer(map, Constant.TCP_URL);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
        return flag;
    }

    private void setDeviceInfo(Map<String, Object> item, String robotCode, long deviceId) {
        if (StringUtils.isEmpty(robotCode)) {
            TIotDevice iotDevice = tIotDeviceMapper.selectById(deviceId);
            item.put("device_ip", iotDevice.getIp());
            item.put("patroldevice_code", iotDevice.getId());
            item.put("patroldevice_name", iotDevice.getDeviceName());
        } else {
            TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
            item.put("patroldevice_code", tRobotInfo.getRobotNum());
            item.put("patroldevice_name", tRobotInfo.getRobotName());
        }
    }

    private void uploadWarn(Map<String, String> iotWarn) {
        if (Constant.definedExtensions() && applicationProperties.getUpSystemFtps().isEnable()) {
            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                log.info("向上级推送环控告警 {}", iotWarn);

                Map<String, List<XMLBaseModel>> map = Maps.newHashMap();
                List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                List<Map<String, Object>> itemList = new ArrayList<>();
                Map<String, Object> item = new HashMap<>(iotWarn.size());
                iotWarn.forEach((k,v) -> item.put(StrUtil.toUnderlineCase(k), v));
                itemList.add(item);
                xmlBaseModel.setItems(itemList);
                xmlBaseModel.setType("22");
                xmlBaseModelList.add(xmlBaseModel);
                map.put("list", xmlBaseModelList);
                Constant.otherServer(map, Constant.TCP_URL);
            });
        }
    }
}




