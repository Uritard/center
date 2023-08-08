package com.yjh.accessrobot.module.command.service;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.PlatformRobotStatus;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/2/7
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class RobotStatusObserver {

    private static final PlatformRobotStatus platformRobotStatus = new PlatformRobotStatus();

    private static final Object platformRobotStatusLock = new Object();

    public static void postNetStatus(String patrolDeviceName, String patrolDeviceCode, String netStatus) {
        boolean flag = false;
        synchronized (platformRobotStatusLock) {
            if (!netStatus.equals(platformRobotStatus.getNetStatus())) {
                platformRobotStatus.setNetStatus(netStatus);
                flag = true;
            }
        }
        if (flag) {
            send(patrolDeviceName, patrolDeviceCode, netStatus);
        }
    }

    private static void send(String patrolDeviceName, String patrolDeviceCode, String status) {
        List<XMLBaseModel> list = new ArrayList<>();
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> xmlItems = new ArrayList<>();
        Map<String, Object> robotStatusMap = new HashMap<>(16);
        robotStatusMap.put("patroldevice_name", patrolDeviceName);
        robotStatusMap.put("patroldevice_code", patrolDeviceCode);
        robotStatusMap.put("time", DateTimeUtil.format(new Date()));
        robotStatusMap.put("type", "2");
        robotStatusMap.put("value", "离线".equals(status) ? "1" : "0");
        robotStatusMap.put("value_unit", "");
        robotStatusMap.put("unit", "");
        xmlItems.add(robotStatusMap);
        xmlBaseModel.setType("1");
        xmlBaseModel.setItems(xmlItems);
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> map = new HashMap<>(3);
        map.put("list", list);
        log.info("机器人通信状态更新上报 {} ", list);
        Constant.mapToOtherServer(map, Constant.TCP_URL);
    }
}
