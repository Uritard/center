/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.utils.HttpClientUtils;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.utils.DateTimeUtil;
import com.yjh.accesstcp.module.device.dao.PatrolTaskDao;
import com.yjh.accesstcp.module.device.entity.UPatrolTask;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.uphandler.UpHandlerEnum;
import com.yjh.accesstcp.module.device.service.uphandler.tek.entity.TaskStatusEntity;
import com.yjh.accesstcp.module.device.service.uphandler.tek.entity.TekResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/12/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TekTaskStateUpHandler extends AbstractTekHandler {

    @Resource
    private PatrolTaskDao patrolTaskDao;
    @Resource
    private UrlPathHandler urlPathHandler;

    private final static String TASK_STATUS_URL = "/distribute/data/service/taskStatus/receive";

    @Override
    public UpHandlerEnum subType() {
        return UpHandlerEnum.TASK_STATE;
    }

    @Override
    public int sendUpHandler(XMLBaseModel xmlBaseModel) {
        //102 自定义的 不处理
        if (xmlBaseModel.getItems() == null || xmlBaseModel.getItems().isEmpty() || "102".equals(xmlBaseModel.getCommand())) {
            return -1;
        }
        try {
            TaskStatusEntity entity = new TaskStatusEntity();
            Map<String, Object> item = xmlBaseModel.getItems().get(0);
            entity.setPlanNo(MapUtils.getString(item, "task_code"));
            entity.setTaskNo(MapUtils.getString(item, "task_patrolled_id"));
            entity.setPatrolDeviceType(isRobotOrCameraTask(entity.getPlanNo(), entity.getTaskNo()));
            String taskStatus = MapUtils.getString(item, "task_state");
            entity.setTaskStatus(aInterfaceApiToUP(taskStatus));
            String startDateStr = MapUtils.getString(item, "start_time");
            Date startTime = DateTimeUtil.parse(startDateStr);
            entity.setTaskStart(startTime.getTime());
            if ("146".contains(taskStatus)) {
                //任务结束状态 1-已执行 4-终止 6-超期
                entity.setTaskStop(System.currentTimeMillis());
            }
            List<TaskStatusEntity> list = new ArrayList<>();
            list.add(entity);
            String result = HttpClientUtils.getInstance().postUrl(urlPathHandler.getTekUrl(TASK_STATUS_URL), JSON.toJSONString(list));
            log.info("发送任务状态返回： {}", result);
            TekResult tekResult = JSON.parseObject(result,TekResult.class);
            if (tekResult == null || !(0 == tekResult.getCode())) {
                log.info("上级返回失败！");
                return -1;
            }
        } catch (Exception e) {
            log.error("发送任务状态失败！ xml=>{}", xmlBaseModel, e);
            return -1;
        }
        return Result.SUCCESS;
    }

    /**
     * 将A接口任务状态转换为科大状态
     * A接口状态        1-已执行 2-正在执行 3-暂停 4-终止 5-未执行 6-超期
     * 科大上级状态      2-已检测 1-检测中  4-已暂停 3-已取消 0-待检测 2-超期
     *
     * @param aTaskStatus A接口任务状态
     * @return 科大上级状态
     */
    private String aInterfaceApiToUP(String aTaskStatus) {
        switch (aTaskStatus) {
            case "1":
            case "6":
                return "2";
            case "2":
                return "1";
            case "3":
                return "4";
            case "4":
                return "3";
            case "5":
            default:
                return "0";
        }
    }

    /**
     * 判断任务相机任务还是机器人任务
     * 包含机器人就是机器人任务
     *
     * @param taskCode 任务编码
     * @return
     */
    private String isRobotOrCameraTask(String taskCode, String taskPatrolledId) {
        // 增加时间判断，避免预先初始化导致数据传入下一个任务
        String timeStr = StringUtils.substringAfterLast(taskPatrolledId, "_");
        Date date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
        //根据taskCode找到taskId
        UPatrolTask uPatrolTask = patrolTaskDao.selectTaskIdByTaskCode(taskCode, date);
        //查询任务下面的点位巡视类型
        List<Integer> cruiseTypeList = patrolTaskDao.selectCruiseType(uPatrolTask.getTaskId());
        String type = "0";
        //包含相机就是相机任务 其他的视为机器人任务
        if (cruiseTypeList.contains(230) || cruiseTypeList.contains(229)) {
            type = "1";
        }
        return type;
    }

}
