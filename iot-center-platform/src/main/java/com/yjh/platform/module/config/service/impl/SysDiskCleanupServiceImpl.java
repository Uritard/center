package com.yjh.platform.module.config.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.module.config.entity.CleanStep;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import com.yjh.platform.module.config.dao.SysDiskCleanupMapper;
import com.yjh.platform.module.config.service.FileProcess;
import com.yjh.platform.module.config.service.ISysDiskCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>
 * 磁盘清理记录 服务实现类
 * </p>
 *
 * @author Chenfei
 * @since 2023-06-30
 */
@Slf4j
@Service
public class SysDiskCleanupServiceImpl extends ServiceImpl<SysDiskCleanupMapper, SysDiskCleanup> implements ISysDiskCleanupService {
    private final RedisTemplate redisTemplate;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>(8);
    private static final Map<Integer, String> STEP_MAP = new HashMap<>(16);

    static {
        // 1: 清理中  2: 清理完成  3：待清理  -1: 清理失败
        STATUS_MAP.put(0, "不清理");
        STATUS_MAP.put(1, "清理中");
        STATUS_MAP.put(2, "完成");
        STATUS_MAP.put(3, "待清理");
        STATUS_MAP.put(-1, "清理失败");

        STEP_MAP.put(0, "开始清理");
        STEP_MAP.put(1, "清理临时文件");
        STEP_MAP.put(2, "清理任务文件");
        STEP_MAP.put(3, "清理任务报告");
        STEP_MAP.put(4, "备份数据库");
        STEP_MAP.put(5, "删除任务数据");
        STEP_MAP.put(6, "删除日志数据");
        STEP_MAP.put(7, "清理完成");
    }

    public SysDiskCleanupServiceImpl(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Map<String, Object> runningCleanup() {
        SysDiskCleanup cleanup = super.query().ne("clean_status", 2).one();
        if (cleanup == null) {
            return Collections.emptyMap();
        }

        List<CleanStep> stepList = new ArrayList<>();

        boolean flag = false;
        int i = 0;
        // 开始
        stepProcess(stepList, 0, 2, false);
        flag = stepProcess(stepList, ++i, cleanup.getDelTmp(), false);
        flag = stepProcess(stepList, ++i, cleanup.getDelTaskFile(), flag);
        flag = stepProcess(stepList, ++i, cleanup.getDelTaskReport(), flag);
        flag = stepProcess(stepList, ++i, cleanup.getBackDatabase(), flag);
        flag = stepProcess(stepList, ++i, cleanup.getDelTaskData(), flag);
        flag = stepProcess(stepList, ++i, cleanup.getDelLogs(), flag);
        // 结束
        stepProcess(stepList, 7, !flag ? 1 : 2, flag);

        Map<String, Object> map = new HashMap<>(4);
        map.put("id", cleanup.getId());
        return map;
    }

    private boolean stepProcess(List<CleanStep> stepList, int order, int status, boolean waitClean) {
        if (status == 0) {
            return false;
        }
        if (waitClean && status == 1) {
            status = 3;
        }
        CleanStep step =
            new CleanStep().setOrder(order).setStepName(STEP_MAP.get(order)).setStatus(status).setStateName(STATUS_MAP.get(status));
        stepList.add(step);

        return status == 1 || waitClean;
    }

    @Override
    public boolean addTask(SysDiskCleanup sysDiskCleanup) {
        SysDiskCleanup cleanup = super.query().ne("clean_status", 2).one();
        if (cleanup != null) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "当前已存在一个未结束磁盘清理任务，请等待结束或确认！");
        }

        boolean needCheckDate =
            ArrayUtils.contains(new int[] {sysDiskCleanup.getDelTmp(), sysDiskCleanup.getDelTaskFile(), sysDiskCleanup.getDelTaskReport()},
                1);

        if (needCheckDate) {
            String retainedDataTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedDataTime", "content");

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MONTH, -NumberUtils.toInt(retainedDataTime, 36));
            if (sysDiskCleanup.getExpiryDate() == null || calendar.getTime().compareTo(sysDiskCleanup.getExpiryDate()) < 0) {
                throw new BusinessException(ResultCodeEnum.CODE10018, "清理数据时间不正确，请重新选择！");
            }
        }

        String backPath = (String)redisTemplate.opsForHash().get("t_sys_param:backPath", "content");
        String backName = DateTimeUtil.format3(new Date()) + RandomStringUtils.randomAlphanumeric(3);
        if (needCheckDate && sysDiskCleanup.getBackFile() == 1) {
            String backFilePath = FilenameUtils.concat(backPath, backName + ".zip");
            sysDiskCleanup.setBackFilePath(backFilePath);
        }
        if (sysDiskCleanup.getBackDatabase() == 1) {
            String backDbPath = FilenameUtils.concat(backPath, backName + ".sql");
            sysDiskCleanup.setBackDataPath(backDbPath);
        }

        sysDiskCleanup.setId(null);
        boolean ins = super.save(sysDiskCleanup);

        cleanup(sysDiskCleanup);

        return ins;
    }

    @Override
    public boolean recoveryTask(int cleanId, int type) {
        SysDiskCleanup cleanup = super.getById(cleanId);
        if (cleanup == null) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "恢复记录不存在！");
        }

        return true;
    }

    @Override
    public boolean deleteBack(int cleanId) {
        SysDiskCleanup cleanup = super.getById(cleanId);
        if (cleanup == null) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "删除记录不存在！");
        }

        try {
            if (StringUtils.isNotEmpty(cleanup.getBackFilePath())) {
                Files.deleteIfExists(Paths.get(cleanup.getBackFilePath()));
            }
        } catch (IOException e) {
            log.error("删除失败： {}", cleanup.getBackFilePath(), e);
        }

        try {
            if (StringUtils.isNotEmpty(cleanup.getBackDataPath())) {
                Files.deleteIfExists(Paths.get(cleanup.getBackDataPath()));
            }
        } catch (IOException e) {
            log.error("删除失败： {}", cleanup.getBackDataPath(), e);
        }
        cleanup.setBackExpire(2);
        cleanup.setUpdateTime(new Date());
        cleanup.setBackFilePath("");
        cleanup.setBackDataPath("");

        return super.updateById(cleanup);
    }

    private void cleanup(SysDiskCleanup sysDiskCleanup) {

        // 临时文件保留时长
        String retainedTempTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedTempTime", "content");
        // 报告报表保留时长
        String retainedReportTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedReportTime", "content");

        ThreadPoolUtil.COMMON_POOL.addThread(() -> {
            // 清理临时文件
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -NumberUtils.toInt(retainedTempTime, 7));
            String tmpFilePath = (String)redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content");
            FileProcess process = new FileProcess(calendar.getTime(), false, tmpFilePath, sysDiskCleanup.getBackFilePath(), "");
            process.cleanup();
            // 清理顺控文件
            String sequentialPath = (String)redisTemplate.opsForHash().get("t_sys_param:videoPath", "content");
            process.nextProcess(sequentialPath, "");
            process.cleanup();

            // 清理报告和报表
            Calendar calendar2 = Calendar.getInstance();
            calendar2.add(Calendar.DAY_OF_MONTH, -NumberUtils.toInt(retainedTempTime, 31));
            String reportFilePath = (String)redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content");
            String reportReflect = (String)redisTemplate.opsForHash().get("t_sys_param:reportReflect", "content");
            process.nextProcess(reportFilePath, "");
            process.cleanup();
            process.nextProcess(reportReflect, "");
            process.cleanup();

        });
    }

    private void recovery(SysDiskCleanup sysDiskCleanup) {

        ThreadPoolUtil.COMMON_POOL.addThread(() -> {

        });
    }
}