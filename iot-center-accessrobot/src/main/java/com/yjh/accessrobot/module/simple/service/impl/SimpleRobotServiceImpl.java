/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.module.simple.service.impl;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.module.simple.service.ISimpleRobotService;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-18
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SimpleRobotServiceImpl implements ISimpleRobotService {
    private final RedisTemplate redisTemplate;
    private final RobotService robotService;
    private final TRobotInfoDao tRobotInfoDao;

    @Override
    public int modelSend(String robotCode, String command, String path) {

        checkRobotStatus(robotCode);

        Map<String, Object> item = new HashMap<>(4);
        if (Files.notExists(Paths.get(path))) {
            log.error("模型文件不存在，cmd: {}, path: {}", command, path);
            throw new BusinessException("模型文件不存在");
        }

        String unixPath = FilenameUtils.separatorsToUnix(path);
        String fileName = StringUtils.substringAfterLast(unixPath, "/");
        String toPath = Constant.stationCode() + "/simpleModel/" + robotCode + "/" + fileName;
        robotService.uploadFileToFtps(path, toPath);
        String pathName;
        switch (command) {
            case "1":
                pathName = "pointcloud_file_path";
                break;
            case "2":
                pathName = "position_file_path";
                break;
            case "3":
            default:
                pathName = "route_file_path";
        }
        item.put(pathName, toPath);

        XMLBaseModel xmlBaseModel = new XMLBaseModel().setSendCode(Constant.sendCode())
            .setReceiveCode(robotCode)
            .setCode(Constant.stationCode())
            .setType("42002")
            .setCommand(command)
            .setItems(Collections.singletonList(item));
        String xmlString = PlatformXMLUtil.generateXml(xmlBaseModel);
        int ret = RobotServerHandler.send(robotService.generateByteOrder(xmlString, robotCode), robotCode);
        if (0 != ret) {
            throw new BusinessException("机器人不在线, 模型同步失败");
        }
        return ret;
    }

    /**
     * 判断机器人状态
     * @param robotCode 简易机器人编码
     * @return 成功
     */
    private boolean checkRobotStatus(String robotCode) {

        String robotStatus = tRobotInfoDao.selectStatusByRobotCode(robotCode);
        if (StringUtils.equals(RobotService.OFF_LINE, robotStatus)) {
            throw new BusinessException("机器人: " + robotCode + " 离线, 指令下发失败!");
        }
        Map<String, String> mapForRobotState = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":41");
        if (StringUtils.equalsAny(mapForRobotState.get("value"), "2", "4")) {
            throw new BusinessException("机器人: " + robotCode + " 处于巡视/检修状态, 指令下发失败!");
        }

        return true;
    }
}
