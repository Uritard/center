package com.yjh.platform.module.iot.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.iot.entity.TIotDeviceWarn;
import com.yjh.platform.module.iot.service.TIotDeviceWarnService;
import com.yjh.platform.module.iot.dao.TIotDeviceWarnMapper;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import com.yjh.platform.module.user.dao.AlarmShieldDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.AlarmShield;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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
    private AlarmShieldDao alarmShieldDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean robotIotWarn(Map<String, String> iotWarn) {
        String robotCode = iotWarn.get("robotCode");
        String channelNum = iotWarn.get("type_device_num");
        TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
        if (Objects.isNull(tRobotInfo)) {
            log.error("未找到 robotCode={} 的机器人信息", robotCode);
            return false;
        }
        String ip;
        if (StringUtils.isNotBlank(tRobotInfo.getRobotIp())) {
            ip = tRobotInfo.getRobotIp();
        } else {
            log.error("机器人未配置ip");
            return false;
        }
        if (StringUtils.isBlank(channelNum)){
            log.error("type_device_num is null");
            return false;
        }
        String flag = "deleteFlag";
        TIotDeviceWarn tIotDeviceWarn = getBaseMapper().selectIdByIpAndNum(ip, channelNum);
        if (Objects.nonNull(tIotDeviceWarn)) {
            if (StringUtils.isNotBlank(MapUtils.getString(iotWarn, flag))) {
                Long deleteFlag = Long.valueOf(MapUtils.getString(iotWarn, flag));
                tIotDeviceWarn.setDeleteTime(DateTimeUtil.getDate(iotWarn.get("deleteTime")));
                tIotDeviceWarn.setDeleteFlag(deleteFlag);
                this.updateById(tIotDeviceWarn);
                return true;
            }
        } else {
            tIotDeviceWarn = getBaseMapper().selectDeviceByIpAndNum(ip, channelNum);
            if (Objects.nonNull(tIotDeviceWarn)) {
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
            } else {
                log.warn("该设备未找到,告警不入库!");
                return false;
            }
        }

        //告警屏蔽判断
        AtomicReference<Boolean> isWarn = new AtomicReference<>(true);
        List<AlarmShield> alarmShieldList = alarmShieldDao.selectAlarmShield(tIotDeviceWarn.getPointId(), tIotDeviceWarn.getAlarmContent());
        if (alarmShieldList.size() > 0) {
            alarmShieldList.forEach(alarmShield -> {
                Date now = new Date();
                Date endTime = alarmShield.getEndTime();
                if (alarmShield.getEnable() == 1 && now.before(endTime)) {
                    log.info("告警屏蔽：{}", alarmShield);
                    isWarn.set(false);
                }
            });
        }
        if (isWarn.get()) {
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

    @Override
    public TWarnInfoDetail selectIotDeviceWarn(Long warnId) {
        return getBaseMapper().selectIotDeviceWarn(warnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(TIotDeviceWarn tIotDeviceWarn) {
        tIotDeviceWarn.setDeleteTime(new Date());
        boolean flag = this.updateById(tIotDeviceWarn);
        if (flag && Constant.upSystemFlag()) {
            ThreadPoolUtil.COMMON_POOL.addThread(() -> {
                Map<String, List<XMLBaseModel>> map = Maps.newHashMap();
                TIotDeviceWarn tdw = getBaseMapper().selectById(tIotDeviceWarn.getId());
                TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(tdw.getRobotCode());
                List<XMLBaseModel> xmlBaseModelList = new ArrayList<>();
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                List<Map<String, Object>> itemList = new ArrayList<>();
                Map<String, Object> item = Maps.newHashMap();
                item.put("patroldevice_code", tRobotInfo.getRobotNum());
                item.put("patroldevice_name", tRobotInfo.getRobotName());
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
            });
        }
        return flag;
    }
}




