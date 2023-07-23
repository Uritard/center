package com.yjh.platform.module.config.service.impl;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.datasource.ManageDataSourceConfig;
import com.yjh.platform.module.config.dao.SysDiskCleanupMapper;
import com.yjh.platform.module.config.entity.CleanStep;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import com.yjh.platform.module.config.service.FileProcess;
import com.yjh.platform.module.config.service.ISysDiskCleanupService;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final SysUserDao sysUserDao;
    private final Demo demo;

    public static final int CHECKED = 1;
    public static final int SUCCESS = 2;
    public static final int FAILED = -1;
    public static final String CLEAN_PREFIX = "cleanup:";
    private volatile Long recoveryId;
    private static final Map<Integer, String> STATUS_MAP = new HashMap<>(8);
    private static final Map<String, KeyValue<Integer, String>> STEP_MAP = new HashMap<>(16);

    static {
        // 1: 清理中  2: 清理完成  3：待清理  -1: 清理失败
        STATUS_MAP.put(0, "不清理");
        STATUS_MAP.put(1, "清理中");
        STATUS_MAP.put(2, "完成");
        STATUS_MAP.put(3, "待清理");
        STATUS_MAP.put(-1, "清理失败");

        STEP_MAP.put("start", new DefaultKeyValue<>(0, "开始清理"));
        STEP_MAP.put("del_tmp", new DefaultKeyValue<>(1, "清理临时文件"));
        STEP_MAP.put("del_task_report", new DefaultKeyValue<>(2, "清理任务报告"));
        STEP_MAP.put("del_task_file", new DefaultKeyValue<>(3, "清理任务文件"));
        STEP_MAP.put("back_database", new DefaultKeyValue<>(4, "备份数据库"));
        STEP_MAP.put("del_task_data", new DefaultKeyValue<>(5, "删除任务数据"));
        STEP_MAP.put("del_logs", new DefaultKeyValue<>(6, "删除日志数据"));
        STEP_MAP.put("end", new DefaultKeyValue<>(7, "清理完成"));
    }

    public SysDiskCleanupServiceImpl(RedisTemplate redisTemplate, SysUserDao sysUserDao, Demo demo) {
        this.redisTemplate = redisTemplate;
        this.sysUserDao = sysUserDao;
        this.demo = demo;
    }

    @PostConstruct
    public void keepOnCleanTask() {
        List<SysDiskCleanup> cleanups = super.query().ne("clean_status", SUCCESS).list();
        if (CollectionUtils.isEmpty(cleanups)) {
            return;
        }
        boolean cleanContinue = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:cleanContinue", "content"));
        if (cleanContinue) {
            ThreadPoolUtil.COMMON_POOL.addThread(() -> cleanup(cleanups.remove(0)));
        }
        if (CollectionUtils.isNotEmpty(cleanups)) {
            cleanupStateUpdate(cleanups, FAILED);
        }
    }

    private void cleanupStateUpdate(List<SysDiskCleanup> cleanups, int stepState) {
        for (SysDiskCleanup cleanup : cleanups) {
            if (cleanup.getDelTmp() == CHECKED) {
                cleanup.setDelTmp(stepState);
            }
            if (cleanup.getDelTaskReport() == CHECKED) {
                cleanup.setDelTaskReport(stepState);
            }
            if (cleanup.getDelTaskFile() == CHECKED) {
                cleanup.setDelTaskFile(stepState);
            }
            if (cleanup.getDelTaskData() == CHECKED) {
                cleanup.setDelTaskData(stepState);
            }
            if (cleanup.getDelLogs() == CHECKED) {
                cleanup.setDelLogs(stepState);
            }
            if (cleanup.getBackDatabase() == CHECKED) {
                cleanup.setBackDatabase(stepState);
            }

            if (stepState == FAILED) {
                cleanup.setCleanStatus(CHECKED);
            } else {
                cleanup.setCleanStatus(SUCCESS);
            }
            cleanup.setCleanContent(makeContent(cleanup));

            super.updateById(cleanup);
        }
    }

    @Override
    public Map<String, Object> runningCleanup() {
        SysDiskCleanup cleanup = super.query().ne("clean_status", SUCCESS).one();
        if (cleanup == null) {
            return Collections.emptyMap();
        }
        List<CleanStep> stepList = new ArrayList<>();

        boolean flag;
        // 开始
        stepProcess(stepList, "start", SUCCESS, false);
        flag = stepProcess(stepList, "del_tmp", cleanup.getDelTmp(), false);
        flag = stepProcess(stepList, "del_task_report", cleanup.getDelTaskReport(), flag);
        flag = stepProcess(stepList, "del_task_file", cleanup.getDelTaskFile(), flag);
        flag = stepProcess(stepList, "back_database", cleanup.getBackDatabase(), flag);
        flag = stepProcess(stepList, "del_task_data", cleanup.getDelTaskData(), flag);
        flag = stepProcess(stepList, "del_logs", cleanup.getDelLogs(), flag);
        // 结束
        stepProcess(stepList, "end", flag ? 1 : SUCCESS, flag);

        Map<String, Object> map = new HashMap<>(4);
        map.put("id", cleanup.getId());
        map.put("cleanStatus", flag ? 0 : SUCCESS);
        map.put("list", stepList);
        return map;
    }

    private boolean stepProcess(List<CleanStep> stepList, String stpOrder, int status, boolean waitClean) {
        if (status == 0) {
            return waitClean;
        }
        if (waitClean && status == 1) {
            status = 3;
        }
        KeyValue<Integer, String> stp = STEP_MAP.get(stpOrder);
        CleanStep step =
            new CleanStep().setOrder(stp.getKey()).setStepName(stp.getValue()).setStatus(status).setStateName(STATUS_MAP.get(status));
        stepList.add(step);

        return status == 1 || waitClean;
    }

    @Override
    public synchronized boolean addTask(SysDiskCleanup sysDiskCleanup) {
        stateValidate();

        boolean needCheckDate =
            ArrayUtils.contains(new int[] {sysDiskCleanup.getDelTmp(), sysDiskCleanup.getDelTaskFile(), sysDiskCleanup.getDelTaskReport()},
                CHECKED);

        if (needCheckDate) {
            String retainedDataTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedDataTime", "content");

            Calendar calendar = Calendar.getInstance();
            int retainedMonth = NumberUtils.toInt(retainedDataTime, 36);
            calendar.add(Calendar.MONTH, -retainedMonth);
            if (sysDiskCleanup.getExpiryDate() == null || calendar.getTime().compareTo(sysDiskCleanup.getExpiryDate()) < 0) {
                throw new BusinessException(ResultCodeEnum.CODE10018, "清理数据时间不正确，必须是" + retainedMonth + "个月之前，请重新选择！");
            }
        }

        String backPath = (String)redisTemplate.opsForHash().get("t_sys_param:backPath", "content");
        String backName = DateTimeUtil.format3(new Date()) + RandomStringUtils.randomAlphanumeric(3);
        int backExpire = 0;
        if (needCheckDate && sysDiskCleanup.getBackFile() == CHECKED && sysDiskCleanup.getDelTaskFile() == CHECKED) {
            String backFilePath = FilenameUtils.concat(backPath, backName + ".zip");
            sysDiskCleanup.setBackFilePath(backFilePath);
            backExpire = 1;
        } else if (sysDiskCleanup.getBackFile() == CHECKED && sysDiskCleanup.getDelTaskFile() != CHECKED) {
            sysDiskCleanup.setBackFile(0);
        }
        if (sysDiskCleanup.getBackDatabase() == CHECKED) {
            String backDbPath = FilenameUtils.concat(backPath, backName + ".sql");
            sysDiskCleanup.setBackDataPath(backDbPath);
            backExpire = 1;
        }
        sysDiskCleanup.setBackExpire(backExpire);

        boolean ins = super.save(sysDiskCleanup);

        ThreadPoolUtil.COMMON_POOL.addThread(() -> cleanup(sysDiskCleanup));

        return ins;
    }

    @Override
    public synchronized boolean recoveryTask(int cleanId, int type) {
        SysDiskCleanup cleanup = super.getById(cleanId);
        if (cleanup == null) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "恢复记录不存在！");
        }
        String path = type == 1 ? cleanup.getBackDataPath() : cleanup.getBackFilePath();
        if (StringUtils.isEmpty(path) || !FileUtil.exist(path)) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "备份文件不存在或已经被删除，无法执行恢复！");
        }

        stateValidate();

        recoveryId = (long)cleanId;

        ThreadPoolUtil.COMMON_POOL.addThread(() -> recovery(path, type));
        return true;
    }

    private void stateValidate() {
        if (recoveryId != null && recoveryId > 0) {
            SysDiskCleanup backCleanup = super.getById(recoveryId);
            String name = recoveryId + ":" + DateTimeUtil.format(backCleanup.getCreateTime());
            throw new BusinessException(ResultCodeEnum.CODE10009, "当前有一个备份正在恢复，请等待当前恢复完成！" + name);
        }
        SysDiskCleanup cleanup = super.query().ne("clean_status", SUCCESS).one();
        if (cleanup != null) {
            throw new BusinessException(ResultCodeEnum.CODE10010, "当前存在一个未结束磁盘清理任务，请等待结束或确认！");
        }
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
        cleanup.setBackExpire(3);
        cleanup.setUpdateTime(new Date());
        cleanup.setBackFilePath("");
        cleanup.setBackDataPath("");

        return super.updateById(cleanup);
    }

    @Override
    public boolean confirmation(String userId, String pcode, String identifier) {
        SysUser sysUser = sysUserDao.selectByPrimaryId(NumberUtils.toLong(userId));
        String password = pcode;
        String dbPassword = Demo.decryptDB(sysUser.getPassword());
        //判断开关
        boolean isEncryption = Boolean.parseBoolean((String)redisTemplate.opsForHash().get("t_sys_param:isEncryption", "content"));
        if (isEncryption) {
            // 解密
            try {
                password = demo.decryptIdentifier(password, identifier);
                redisTemplate.delete("pubk:" + identifier);
            } catch (IOException e) {
                log.error("密码解密失败", e);
            }
        }
        if (!StringUtils.equals(password, dbPassword)) {
            log.info("验证失败: {} — {}", password, dbPassword);
            throw new BusinessException(ResultCodeEnum.CODE10106, "密码验证失败！");
        }
        return true;
    }

    private void cleanup(SysDiskCleanup sysDiskCleanup) {
        long id = sysDiskCleanup.getId();
        log.info("clean disk start... id: {}", id);
        // 临时文件保留时长
        String retainedTempTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedTempTime", "content");
        // 抓图文件目录
        String resultImgPath = (String)redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content");
        // 清理临时文件
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -NumberUtils.toInt(retainedTempTime, 7));
        String tmpFilePath = (String)redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content");
        // 图片文件目录
        String prefixAbsolutePath = (String)redisTemplate.opsForHash().get("t_sys_param:prefixAbsolutePath", "content");

        long cleanSize = 0L;
        try (FileProcess process = new FileProcess(calendar.getTime(), false, tmpFilePath, sysDiskCleanup.getBackFilePath(), "")) {
            if (sysDiskCleanup.getDelTmp() == CHECKED) {
                log.info("start delete tmp file...");
                boolean p1 = process.cleanup();
                cleanSize += process.truncateSize();
                upStepStatus(id, "del_tmp", 1, 50, "ftps:" + process.truncateSpace());
                // 清理顺控文件 /home/yjh_iot_center/iot-picture/video
                String sequentialPath = (String)redisTemplate.opsForHash().get("t_sys_param:videoPath", "content");
                // 静默文件图片 /home/yjh_iot_center/iot-picture/resultImg/jm
                String jm = FilenameUtils.concat(resultImgPath, "jm");
                // 预置位校验抓图文件 /home/yjh_iot_center/iot-picture/resultImg/presetCheckImg
                String presetCheckImg = FilenameUtils.concat(resultImgPath, "presetCheckImg");
                // 预置位图片压缩文件 /home/yjh_iot_center/iot-picture/zip
                String zips = (String)redisTemplate.opsForHash().get("t_sys_param:zipPath", "content");
                process.nextProcess(new String[] {sequentialPath, jm, presetCheckImg, zips}, "");
                boolean p2 = process.cleanup();
                cleanSize += process.truncateSize();
                int stat = p1 && p2 ? SUCCESS : FAILED;
                upStepStatus(id, "del_tmp", stat, 100, "tmp:" + process.truncateSpace());
            }

            if (sysDiskCleanup.getDelTaskReport() == CHECKED) {
                log.info("start delete task report...");
                // 报告报表保留时长
                String retainedReportTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedReportTime", "content");
                // 清理报告和报表
                Calendar calendar2 = Calendar.getInstance();
                calendar2.add(Calendar.DAY_OF_MONTH, -NumberUtils.toInt(retainedReportTime, 31));
                String reportFilePath = (String)redisTemplate.opsForHash().get("t_sys_param:tempReflect", "content");
                String reportReflect = (String)redisTemplate.opsForHash().get("t_sys_param:reportReflect", "content");

                process.nextProcess(calendar2.getTime(), reportFilePath, "");
                boolean p1 = process.cleanup();
                cleanSize += process.truncateSize();
                upStepStatus(id, "del_task_report", 1, 50, "sheet:" + process.truncateSpace());

                process.nextProcess(calendar2.getTime(), reportReflect, "");
                boolean p2 = process.cleanup();
                cleanSize += process.truncateSize();
                int stat = p1 && p2 ? SUCCESS : FAILED;
                upStepStatus(id, "del_task_report", stat, 100, "report:" + process.truncateSpace());
            }

            Date expiryDate = sysDiskCleanup.getExpiryDate();
            if (sysDiskCleanup.getDelTaskFile() == CHECKED) {
                log.info("start delete task file...");
                // 清理任务数据
                boolean needBack = sysDiskCleanup.getBackFile() == CHECKED;

                String absVoicePath = (String)redisTemplate.opsForHash().get("t_sys_param:absVoicePath", "content");
                String analyseResultImg = FilenameUtils.concat(prefixAbsolutePath, "analyseResultImg");
                String ftpImageAbsolute = (String)redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content");
                String infraredStorePath = (String)redisTemplate.opsForHash().get("t_sys_param:infraredStorePath", "content");

                // /home/yjh_iot_center/iot-files/voiceFile  /home/yjh_iot_center/iot-picture/analyseResultImg  /home/yjh_iot_center/iot-picture/ftpImg
                // /home/yjh_iot_center/iot-picture/infrared  /home/yjh_iot_center/iot-picture/resultImg
                // 排除目录 /home/yjh_iot_center/iot-picture/ftpImg/Map
                String[] folds = new String[] {resultImgPath, absVoicePath, analyseResultImg, ftpImageAbsolute, infraredStorePath};
                String[] excs = new String[] {FilenameUtils.concat(ftpImageAbsolute, "Map")};
                process.nextProcess(expiryDate, needBack, folds, "/home/yjh_iot_center");
                process.setExcludes(excs);
                boolean p1 = process.cleanup();
                cleanSize += process.truncateSize();
                int stat = p1 ? SUCCESS : FAILED;
                upStepStatus(id, "del_task_file", stat, 100, "taskfile:" + process.truncateSpace());
            }

            if (sysDiskCleanup.getBackDatabase() == CHECKED) {
                boolean p1 = backDatabase(sysDiskCleanup.getBackDataPath());
                upStepStatus(id, "back_database", p1 ? SUCCESS : FAILED, 100, "back database");
            }

            if (sysDiskCleanup.getDelTaskData() == CHECKED) {
                log.info("start delete task data from database...");
                int fg = FAILED;
                try {
                    getBaseMapper().deleteTask(expiryDate);
                    getBaseMapper().deleteTaskResult(expiryDate);
                    fg = SUCCESS;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                upStepStatus(id, "del_task_data", fg, 100, "delete task");
            }

            if (sysDiskCleanup.getDelLogs() == CHECKED) {
                log.info("start delete logs data from database...");
                int fg = FAILED;
                try {
                    getBaseMapper().deleteLogs(expiryDate);
                    fg = SUCCESS;
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                upStepStatus(id, "del_logs", fg, 100, "delete logs");
            }
        }
        String totalSpace = CommonUtils.fileSpace(cleanSize);

        SysDiskCleanup cleanup = super.getById(id);
        String content = makeContent(cleanup);
        super.update().set("clean_status", 1).set("clean_content", content).set("remark", "cleanSpace:" + totalSpace).eq("id", id).update();
        log.info("clean disk finished!!! cleanSpace: {}", totalSpace);
    }

    private String makeContent(SysDiskCleanup cleanup) {
        StringJoiner content = new StringJoiner(";");

        stepContent(content, "del_tmp", cleanup.getDelTmp());
        stepContent(content, "del_task_report", cleanup.getDelTaskReport());
        stepContent(content, "del_task_file", cleanup.getDelTaskFile());
        stepContent(content, "back_database", cleanup.getBackDatabase());
        stepContent(content, "del_task_data", cleanup.getDelTaskData());
        stepContent(content, "del_logs", cleanup.getDelLogs());

        return content.toString();
    }

    private String stepContent(StringJoiner content, String step, int status) {
        if (status == 0) {
            return "";
        }
        String name = STEP_MAP.get(step).getValue();
        String statusName = status == SUCCESS ? " - 成功" : " - 失败";
        String stepContent = name + statusName;
        content.add(stepContent);
        return stepContent;
    }

    public boolean recoveryDatabase(String backPath) {
        log.info("start recovery database...");
        long time1 = System.currentTimeMillis();
        boolean flag = false;
        try {
            // mysql -uroot -pxxx db2 < d:\db1.sql
            String command = "mysql -u " + ManageDataSourceConfig.getUsername() + " -p" + ManageDataSourceConfig.getPassword() + " "
                + ManageDataSourceConfig.getDatabase() + " < " + backPath;
            String[] backCmd = new String[] {"/bin/sh", "-c", command};

            Process runtimeProcess = Runtime.getRuntime().exec(backCmd);
            flag = runtimeProcess.waitFor(60, TimeUnit.MINUTES);

            // #72257 【磁盘清理】备份数据库，完成确定，恢复数据库，表sys_disk_cleanup中clean_status为0，页面会一直显示备份数据库的状态
            List<SysDiskCleanup> cleanups = super.query().ne("clean_status", SUCCESS).list();
            if (CollectionUtils.isNotEmpty(cleanups)) {
                cleanupStateUpdate(cleanups, SUCCESS);
            }

        } catch (IOException e) {
            log.error("恢复数据库失败:", e);
        } catch (InterruptedException e) {
            log.error("恢复数据库失败，线程异常:", e);
            Thread.currentThread().interrupt();
        }
        log.info("end recovery database, result: {}, time: {}", flag, System.currentTimeMillis() - time1);
        return flag;
    }

    public boolean backDatabase(String backPath) {
        log.info("start back database...");
        long time1 = System.currentTimeMillis();
        boolean flag = false;
        try {
            FileUtil.mkParentDirs(backPath);
            String command = "mysqldump -u " + ManageDataSourceConfig.getUsername() + " -p" + ManageDataSourceConfig.getPassword() + " "
                + ManageDataSourceConfig.getDatabase() + " > " + backPath;
            log.info("sql back cmd: {}", command);
            String[] backCmd = new String[] {"/bin/sh", "-c", command};

            Process runtimeProcess = Runtime.getRuntime().exec(backCmd);
            flag = runtimeProcess.waitFor(60, TimeUnit.MINUTES);
        } catch (IOException e) {
            log.error("备份数据库失败:", e);
        } catch (InterruptedException e) {
            log.error("备份数据库失败，线程异常:", e);
            Thread.currentThread().interrupt();
        }
        log.info("end back database, result: {}, time: {}", flag, System.currentTimeMillis() - time1);
        return flag;
    }

    /**
     * 更新数据库和Redis中执行状态
     */
    private void upStepStatus(long id, String stpOrder, int status, float rate, String remark) {
        // 更新数据库状态
        if (status != CHECKED) {
            super.update().set(stpOrder, status).eq("id", id).update();
        }

        // 更新 Redis 状态
        KeyValue<Integer, String> stp = STEP_MAP.get(stpOrder);

        String key = CLEAN_PREFIX + id + ":step" + stp.getKey();
        String oldRmk = (String)redisTemplate.opsForHash().get(key, "remark");
        oldRmk = StringUtils.isNotEmpty(oldRmk) ? oldRmk + ";" + remark : remark;

        CleanStep step =
            new CleanStep().setOrder(stp.getKey()).setStepName(stp.getValue()).setStatus(status).setStateName(STATUS_MAP.get(status))
                .setRate(rate).setRemark(oldRmk);

        Map<String, String> stepMap = Object2Map.objectToMap(step, true);
        redisTemplate.opsForHash().putAll(key, stepMap);

    }

    private void recovery(String path, int type) {
        log.info("开始恢复备份数据, path: {}", path);
        long time1 = System.currentTimeMillis();
        try {
            if (type == 1) {
                recoveryDatabase(path);
            } else {
                try (FileProcess process = new FileProcess(new Date(), false, "/home/yjh_iot_center", path, "")) {
                    process.recovery();
                }
            }
        } catch (Exception e) {
            log.error("恢复数据失败", e);
        } finally {
            recoveryId = null;
        }
        log.info("备份恢复完成, path: {}, time: {}", path, System.currentTimeMillis() - time1);
    }

    /**
     * 每天晚上3点半执行
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void refreshRecordsOnSchedule() {

        QueryWrapper<SysDiskCleanup> queryWrapper = new QueryWrapper<>(new SysDiskCleanup()).eq("clean_status", 2).eq("back_expire", 1);
        List<SysDiskCleanup> cleanupList = super.list(queryWrapper);
        // 报告报表保留时长
        String retainedBackTime = (String)redisTemplate.opsForHash().get("t_sys_param:retainedBackTime", "content");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -NumberUtils.toInt(retainedBackTime, 31));
        long backTime = calendar.getTime().getTime();
        for (SysDiskCleanup cleanup : cleanupList) {
            if (backTime > cleanup.getCreateTime().getTime()) {
                if (StringUtils.isNotEmpty(cleanup.getBackFilePath())) {
                    FileUtil.del(cleanup.getBackFilePath());
                }
                if (StringUtils.isNotEmpty(cleanup.getBackDataPath())) {
                    FileUtil.del(cleanup.getBackDataPath());
                }
                cleanup.setBackExpire(2);
                cleanup.setUpdateTime(new Date());
                cleanup.setBackFilePath("");
                cleanup.setBackDataPath("");
                super.updateById(cleanup);
            }
        }
    }

}