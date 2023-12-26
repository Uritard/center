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
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.TRobotInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean robotIotWarn(Map<String, String> iotWarn) {
        String robotCode = iotWarn.get("robotCode");
        String channelNum = iotWarn.get("type_device_num");
        TRobotInfo tRobotInfo = tRobotInfoDao.selectByRobotCode(robotCode);
        String ip = "-1";
        if (StringUtils.isNotBlank(tRobotInfo.getRobotIp())) {
            ip = tRobotInfo.getRobotIp();
        } else {
            return false;
        }
        Long id = getBaseMapper().selectIdByIpAndNum(ip, channelNum);
        TIotDeviceWarn tIotDeviceWarn = new TIotDeviceWarn();
        if (Objects.nonNull(id)) {
            tIotDeviceWarn.setId(id);
            if (StringUtils.isNotBlank(MapUtils.getString(iotWarn, "deleteFlag"))) {
                Long deleteFlag = Long.valueOf(MapUtils.getString(iotWarn, "deleteFlag"));
                tIotDeviceWarn.setDeleteTime(DateTimeUtil.getDate(iotWarn.get("deleteTime")));
                tIotDeviceWarn.setDeleteFlag(deleteFlag);
                this.updateById(tIotDeviceWarn);
            }
        } else {
            tIotDeviceWarn = getBaseMapper().selectDeviceByIpAndNum(ip, channelNum);
            tIotDeviceWarn.setCreateTime(new Date());
            tIotDeviceWarn.setAlarmTime(DateTimeUtil.getDate(iotWarn.get("alarmTime")));
            tIotDeviceWarn.setRobotCode(robotCode);
            tIotDeviceWarn.setChannelNum(channelNum);
            tIotDeviceWarn.setAlarmContent(MapUtils.getString(iotWarn, "value"));
            tIotDeviceWarn.setDeleteFlag(1L);
            this.save(tIotDeviceWarn);
        }
        Map<String, String> jasonMaps = new HashMap<>(16);
        jasonMaps.put("type", "alarmPopUp");
        jasonMaps.put("warnLevel", "132");
        jasonMaps.put("warnId", String.valueOf(tIotDeviceWarn.getId()));
        jasonMaps.put("warnType", "2");
        jasonMaps.put("source", "iot");
        String json = JSON.toJSONString(jasonMaps);
        log.info("发送给前端的消息：{}", json);
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
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
                item.put("sn", tdw.getChannelNum());
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




