package com.yjh.platform.module.user.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.FtpsUtil;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.common.utils.ThreadPoolUtil;
import com.yjh.platform.configuration.UpFtpsConfig;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.quartz.SilentTaskJob;
import com.yjh.platform.module.patrol.service.IntelAnalysisService;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.CameraPresetCheckResult;
import com.yjh.platform.module.user.entity.SilentConf;
import com.yjh.platform.module.user.entity.TCameraInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.impl.common.ConcurrentReaderHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

/**
* @author yc
* @since 2020-08-18
*/
@Service
public class TCameraPresetService {

    @Autowired
    private TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCameraInfoDao tCameraInfoDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IntelAnalysisService intelAnalysisService;
    @Autowired
    private UpFtpsConfig upFtpsConfig;
    /**
     * 变电站编码
     */
    @Value("${station.code}")
    private String stationCode;

    /**
     * 是否调用调用算法组相机偏移校验功能
     */
    @Value("${camera.preset.second.check}")
    private boolean cameraPresetSecondCheck;

    private static final Long LOCK_REDIS_TIMEOUT = 10L;

    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(10);

    private static Map<Integer,ScheduledFuture> silentConfMap = new ConcurrentReaderHashMap();


    private Logger log = LoggerFactory.getLogger(TCameraPresetService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraPreset tCameraPreset) {
        if (tCameraPreset.getPresetType() == null || tCameraPreset.getPresetType() == 0) {
            tCameraPreset.setPresetType(1);
        }
        return this.tCameraPresetDao.insert(tCameraPreset);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long presetId) {
        List<Long> instanceIdList = tCameraPresetDao.selectInstanceIdList(presetId);
        {//检查此预置位是否被配成巡检点
            if(instanceIdList != null && instanceIdList.size()>0){
                return -1;
            }
        }
        if(instanceIdList != null && instanceIdList.size() >0){
            tCameraPresetDao.deleteInstance(instanceIdList);//tcpi
            tCameraPresetDao.deletePlanInstance(instanceIdList);//tcplan
            tCameraPresetDao.deletePointInstance(instanceIdList);//tcpattr
            tAlgorithmConfDao.deleteByPrimaryId(presetId);//tac
        }
        return this.tCameraPresetDao.deleteByPrimaryId(presetId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteSelectedPreset(String presetIds) {
        List<String> list= Arrays.asList(presetIds.split(","));
        for (String item:list) {
            this.deleteByPrimaryId(Long.valueOf(item));
        }
        return this.tCameraPresetDao.deleteSelectedPreset(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraPreset tCameraPreset) {
        return this.tCameraPresetDao.update(tCameraPreset);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCameraPreset selectByPrimaryId(Long presetId) {
        return this.tCameraPresetDao.selectByPrimaryId(presetId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> selectByCameraId(Long cameraId) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.selectByCameraId(cameraId);
        return tCameraPresetList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> selectByPresetName(String presetName) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.selectByPresetName(presetName);
        return tCameraPresetList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPreset> select(Long presetId, Long cameraId, Integer presetNum, String presetName, String creatorUser, Date creatorTime, Integer isUse, String presetImg, Integer inspectionPostion, Integer collectStatus, Integer calibrationStatus) {
        List<TCameraPreset> tCameraPresetList = tCameraPresetDao.select(presetId, cameraId, presetNum, presetName, creatorUser, creatorTime, isUse, presetImg, inspectionPostion, collectStatus, calibrationStatus);
        return tCameraPresetList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCameraPresetExpand> selectByPage(TCameraPreset tCameraPreset) {
        return tCameraPresetDao.selectByPage(tCameraPreset);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraPreset> list) {
        for(TCameraPreset item:list){
            List<Long> listHave = tCameraPresetDao.selectInstanceIdList(item.getPresetId());
            if(listHave!= null && listHave.size()>0){
                return -1;
            }
        }
        return this.tCameraPresetDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCameraPreset selectLastOne() {
        return this.tCameraPresetDao.selectLastOne();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> selectPresetTree(Long cameraId) {
        List<AreaInfo> tree = new LinkedList<>();
        TCameraInfo tCameraInfo = tCameraInfoDao.selectCamera(cameraId);
        if(tCameraInfo == null){
            return null;
        }
        List<TCameraPreset> presetList = tCameraPresetDao.selectByCameraId(cameraId);
        AreaInfo camera = new AreaInfo();
        camera.setUpId(-1L);
        camera.setId(cameraId);
        camera.setLabel(tCameraInfo.getCameraName());
        camera.setInfoType("camera");
        List<AreaInfo> child = new LinkedList<>();
        if(presetList != null && presetList.size()>0){
            for (TCameraPreset item:presetList) {
                AreaInfo preset = new AreaInfo();
                preset.setUpId(cameraId);
                preset.setId(item.getPresetId());
                preset.setLabel(item.getPresetName());
                preset.setInfoType("preset");
                preset.setUpName(tCameraInfo.getCameraName());
                child.add(preset);
            }
            camera.setChildren(child);
        }
        tree.add(camera);

        return tree;
    }
    public boolean judgePresentNum(Long cameraId,Integer presentNum) {
        boolean flag = false;
        List<TCameraPreset> tCameraPresetList = this.tCameraPresetDao.select(null, cameraId, presentNum, null, null, null, null, null, null, null,null);
        if (tCameraPresetList.size()>0) {
            flag=true;
        }
        return flag;
    }
    private   long getFileSize(File f){long size = 0;
        //取得文件夹大小
        try {
            File flist[] = f.listFiles();
            for (int i = 0; i < flist.length; i++) {
                if (flist[i].isDirectory()) {
                    size = size + getFileSize(flist[i]);
                } else {
                    size = size + flist[i].length();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return size;
    }

    @Transactional(rollbackFor = Exception.class)
    public Result download(List<Long> cameraIdList, List<Long> presetList){
        Result result =new Result();
        Map<String,String> mapForPreset = redisTemplate.opsForHash().entries("t_sys_param:presetImgPath");
        String picPath = mapForPreset.get("content");///home/yjh_iot_center/iot-picture/presets
        Map<String,String> mapForZip = redisTemplate.opsForHash().entries("t_sys_param:zipPath");
        String zipPath = mapForZip.get("content");///home/yjh_iot_center/iot-picture/zip
        Map<String,String> mapForZipReal = redisTemplate.opsForHash().entries("t_sys_param:zipRealPath");
        String zipPathReal = mapForZipReal.get("content");//http://192.168.9.40:10086/imgs/zip

        {
            //判断文件大小
            Map<String,String> mapForZipSize = redisTemplate.opsForHash().entries("t_sys_param:zipFileSize");
            String zipFileSize = "20";
            if(mapForZipSize != null && mapForZipSize.size()>0){
                    zipFileSize= mapForZipSize.get("content");
            }
            //picPath ="D:\\code\\qhTest\\presets";
            File file = new File(picPath);
            if(file.exists()){
                long size = getFileSize(file)/(1024*1024);
//                log.info("file.length():"+size);
                log.info("采集文件大小："+size+"M");
                if(size > Long.valueOf(zipFileSize)){
                    result.setCode(209,"采集文件大于"+zipFileSize+"M，禁止下载");
                    return result;
                }
            }
        }

        if(cameraIdList == null){
            cameraIdList = tCameraPresetDao.selectCameraIdList();
        }
        if(cameraIdList != null && cameraIdList.size()==0){
            cameraIdList = tCameraPresetDao.selectCameraIdList();
        }
        if(cameraIdList != null && cameraIdList.size()>0){

            List<Long> cameraHavePresetList = tCameraPresetDao.selectCameraHavePreset(cameraIdList);
            if(cameraHavePresetList == null){
                result.setCode(209,"fail");
                return result;
            }
            if(cameraHavePresetList.size() == 0 ){
                result.setCode(209,"fail");
                return result;
            }
            //删除zipPath下的所有文件
            try {
                String cmd= "rm -rf "+zipPath+"/*";
                String[] cmds = new String[]{"sh","-c",cmd};
                Runtime.getRuntime().exec(cmds);
                log.info("linux命令："+cmd);
                //Runtime.getRuntime().exec(cmd);
                cmd = "mkdir "+zipPath;
                log.info("linux命令："+cmd);
                cmds = new String[]{"sh","-c",cmd};
                Runtime.getRuntime().exec(cmds);
                cmd = "mkdir "+zipPath+"/picture";
                log.info("linux命令："+cmd);
                cmds = new String[]{"sh","-c",cmd};
                Runtime.getRuntime().exec(cmds);
            } catch (IOException e) {
                log.error("复制文件错误："+e);
            }

            for(Long cameraId: cameraIdList){
                //找到这个cameraId下的所有预置位
                if(presetList == null ){
                    presetList = tCameraPresetDao.selectForThisPreset(cameraId);
                }
                log.info("cameraId: "+cameraId);
                log.info("presetList: "+presetList);
                if(presetList!=null && presetList.size()>0){
                    for(Long presetId:presetList){
                        try {
                        //将所有的文件移动到一个文件内
                        //String url = "cp -r " + picPath+"/"+presetId + " " + zipPath+"/picture/"+cameraId+"/";
                        String url = "cp -r " + picPath+"/"+presetId + " " + zipPath+"/picture/";
                        String[] cmds = new String[]{"sh","-c",url};
                        Runtime.getRuntime().exec(cmds);
                        } catch (IOException e) {
                            log.error("复制文件错误："+e);
                        }
                    }

                }else {
                    if(cameraIdList.size() == 1){
                        result.setCode(209,"fail");
                        return result;
                    }
                }
                presetList =null;
            }
            //压缩
            try {
                Thread.sleep(1000);
                log.info("开始压缩");
                String zipCmd= "cd "+zipPath+" && zip -r -y picture.zip picture/*";
                log.info("linux命令："+zipCmd);
                String[] cmds = new String[]{"sh","-c",zipCmd};
                Process p = Runtime.getRuntime().exec(cmds);
                BufferedReader in = null;
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String str = null;
                while ((str = in.readLine()) != null) {
                    log.info("结果："+str);
                }
                in.close();
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("复制文件错误："+e);
            }
           result.setData(zipPathReal+"/picture.zip");
            return result;
        }
        result.setCode(209,"fail");
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public int upload(MultipartFile file) {
        if (!FileUtil.checkFileName(file.getOriginalFilename())){
            throw new BusinessException("请上传指定的文件");
        }
        Map<String,String> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:zipPath");
        String path =  mapForPicModelPath.get("content");///home/yjh_iot_center/iot-picture/zip

        Map<String,String> mapForModelPath  = redisTemplate.opsForHash().entries("t_sys_param:zipTargetPath");
        String modelPath =  mapForModelPath.get("content");///home/yjh/iot-picture/model-picture/sync/Template/BigImg
        //String modelPath =  "/home/yjh_iot_center/iot-picture/zip";///home/yjh/iot-picture/model-picture/sync/Template/BigImg

        String fileName = "copyZip.zip";
        // 将上传文件写入
        try {
            deleteDir(new File(path +"/"+ fileName));
            file.transferTo(new File(path +"/"+ fileName));
            //解压 unzip -o xxx.zip -d /home/yjh_iot_center/iot-  覆盖原有文件
            String cmd= "unzip -o "+path+"/copyZip.zip"+" -d "+modelPath;
            String[] cmds = new String[]{"sh","-c",cmd};
            log.info("linux命令："+cmd);
            Runtime.getRuntime().exec(cmds);
//            Thread.sleep(1000);
//            //解压到zip目录下 再将相关文件复制到对应目录下  /home/yjh/iot-picture/model-picture/sync/Template/BigImg  /预置位
//            File zipFile = new File(path+"/copyZip/picture");
//            File[] zipFileList = zipFile.listFiles();
//            if(zipFileList != null && zipFileList.length>0){
//                for(File item:zipFileList){
//                    if(item.isDirectory()){
//                        {
//                            //不覆盖原有的文件
//                            File targetFile = new File(modelPath+"/"+item.getName());
//                            log.info("文件："+modelPath+"/"+item.getName()+" 结果："+ targetFile.exists());
//                            if(targetFile.exists()){
//                                continue;
//                            }
//                        }
//                        String url = "cp -rf " + path+"/copyZip/picture/"+item.getName()+" "+modelPath+"/"+item.getName();
//                        String[] cpCmd = new String[]{"sh","-c",url};
//                        log.info("linux复制命令："+url);
//                        Runtime.getRuntime().exec(cpCmd);
//                    }
//                }
//            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return 1;
    }

    /**
     * 查询所有配置了静默预置位的相机及对应预置位
     *
     * @return Long
     */
    public List<Map<String, Object>> selectCameraBySilent(){
        return tCameraPresetDao.selectCameraBySilent();
    }

    public TCameraPreset countKeepWatch(Long cameraId){
        return tCameraPresetDao.selectKeepWatch(cameraId);
    }

    public TCameraPreset countKeepWatchTask(Long cameraId,Long selfPreset){
        return tCameraPresetDao.selectKeepWatchTask(cameraId,selfPreset);
    }

    public String getPresetBasePath(){
        return (String)redisTemplate.opsForHash().get("t_sys_param:presetImgPath", "content");
    }

    public String getPresetUrlPath(){
        return (String)redisTemplate.opsForHash().get("t_sys_param:presetRealImgPath", "content");
    }

    public Map<String, Object> selectInstanceInfo(Long presetId) {
        return tCameraPresetDao.selectInstanceInfo(presetId);
    }

    public String selectAlarmLevel(String defectModel ,String desc) {
        return tCameraPresetDao.selectAlarmLevel(defectModel,desc);
    }

    public int selectSilentByCameraIdAndPresetId(String cameraId, String presetId) {
        return tCameraPresetDao.selectSilentByCameraIdAndPresetId(cameraId,presetId);
    }

    public List<Map<String, Object>> selectCameraBySecondSilent() {
        return  tCameraPresetDao.selectCameraBySecondSilent();
    }

    public int selectCameraPresetInTask(String presetId) {
        return tCameraPresetDao.selectCameraPresetInTask(presetId);
    }

    public void startSilentTask(){
        List<SilentConf> silentConfs = tCameraPresetDao.selectSilentInfo(null);
        silentConfs.forEach(this::creatSilentTask);
    }

    public void stopSilentTask(){
        silentConfMap.forEach((integer, scheduledFuture) -> {
            scheduledFuture.cancel(true);
            executor.setRemoveOnCancelPolicy(true);
        });
    }

    public void creatSilentTask(SilentConf silentConf){
        // 静默任务开关
        String silentFlag = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isSilentTask", "content"));
        if (StringUtils.equals("false", silentFlag)) {
            log.info("静默任务开关：isSilentTask 没开");
            return;
        }
        ScheduledFuture future = silentConfMap.get(silentConf.getPresetType());
        if (future != null){
            future.cancel(true);
            executor.setRemoveOnCancelPolicy(true);
        }
        SilentTaskJob silentTaskJob = new SilentTaskJob(tCameraPresetDao,redisTemplate,intelAnalysisService,
                silentConf.getPresetType(),upFtpsConfig,stationCode);
        future = executor.scheduleAtFixedRate(silentTaskJob,0,silentConf.getChillTime(), TimeUnit.SECONDS);
        silentConfMap.put(silentConf.getPresetType(),future);
    }

    public List<SilentConf> selectSilentConfInfo(){
        return tCameraPresetDao.selectPresetTypeInfo();
    }

    public int updateSilentConf(SilentConf silentConf){
        this.creatSilentTask(silentConf);
        return tCameraPresetDao.updateSilentConf(silentConf);
    }

    /**
     * 查询redis，获取相机预置位校验结果
     *
     * @param cameraId cameraId
     * @return result
     */
    public Map<Long, Integer> queryPresetCheckResult(Long cameraId) {
        String redisKey = getPresetRedisKeyByCameraId(cameraId);
        Map<Long, Integer> resultMap = (Map<Long, Integer>) redisTemplate.opsForValue().get(redisKey);
        return resultMap;
    }

    /**
     * 异步线程进行相机预置位偏移校验，将校验结果实时写入redis
     *
     * @param cameraId cameraId
     */
    public void cameraPresetCheck(Long cameraId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    // 先获取锁，再进行处理
                    if (getLock(cameraId)) {
                        checkCameraPreset(cameraId);
                    }
                } catch (Exception e) {
                    log.error("预置位校验失败，错误: {}" + e.getMessage());
                }
            }
        };

        ThreadPoolUtil.COMMON_POOL.addThread(runnable);
    }

    /**
     * 对相机进行预置位校验，并实时将校验结果同步到redis
     *
     * @param cameraId cameraId
     */
    private void checkCameraPreset(Long cameraId) {
        try {
            List<TCameraPreset> presetList = getAllCameraPreset(cameraId);
            if (CollectionUtils.isEmpty(presetList)) {
                return;
            }

            // 对所有预置位的校验结果进行初始化，值为 0 进行中……，并立即上传到redis
            List<CameraPresetCheckResult> checkResults = initPresetCheckResult(presetList);
            setPresetCheckResultToRedis(checkResults);

            // 循环遍历所有预置位，进行PTZ比对和图片对比，每处理完一个预置位，立即同步结果到redis
            boolean cameraIsUnused = true; // 相机是否空闲
            for (CameraPresetCheckResult item : checkResults) {
                try {
                    if (!cameraIsUnused) {
                        // 相机不可控，检测结果置为终止态
                        item.setPresetCheckResult(-2);
                    } else {
                        // 否则继续尝试采集图片和ptz信息，并进行检测
                        Integer result = checkOnePreset(item.getPreset());
                        if (result == -2) {
                            cameraIsUnused = false;
                        }

                        item.setPresetCheckResult(result);
                    }

                    // 检测结果及时同步到redis
                    setPresetCheckResultToRedis(checkResults);
                } catch (Exception e) {
                    log.error("checkCameraPreset err: {}", e.getMessage());
                }
            }
        } finally {
            // 释放锁
            releaseLock(cameraId);
        }
    }

    /**
     *  加锁，因为setIfAbsent操作无法设置超时时间，只能采用普通设值的方式
     **/
    public Boolean getLock(Long cameraId){
        String lockKey = getPresetRedisLockByCameraId(cameraId);
        if (redisTemplate.opsForValue().get(lockKey) == null) {
            redisTemplate.opsForValue().set(lockKey, lockKey, 30, TimeUnit.MINUTES);
            return true;
        } else {
            return false;
        }
    }

    /**
     *  释放锁
     **/
    public Long releaseLock(Long cameraId) {
        String locoKey = getPresetRedisLockByCameraId(cameraId);
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        RedisScript<Long> redisScript = new DefaultRedisScript<>(luaScript, Long.class);
        Long releaseStatus = (Long) this.redisTemplate.execute(redisScript, Collections.singletonList(locoKey), locoKey);
        return releaseStatus;
    }

    /**
     * 获取相机对应的所有预置位
     *
     * @param cameraId cameraId
     * @return result
     */
    private List<TCameraPreset> getAllCameraPreset(Long cameraId) {
        List<TCameraPreset> presetList = tCameraPresetDao.selectByCameraId(cameraId);
        return presetList;
    }

    /**
     * 对所有预置位的校验结果进行初始化，值为 0 进行中……
     *
     * @param presetList presetList
     * @return result
     */
    private List<CameraPresetCheckResult> initPresetCheckResult(List<TCameraPreset> presetList) {
        List<CameraPresetCheckResult> checkResults = new ArrayList<>();
        presetList.forEach(item -> {
            CameraPresetCheckResult checkResult = new CameraPresetCheckResult();
            checkResult.setPreset(item);
            checkResult.setPresetCheckResult(0);
            checkResults.add(checkResult);
        });

        return checkResults;
    }

    /**
     * 校验单个预置位
     *
     * @param tCameraPreset tCameraPreset
     * @return result
     */
    private Integer checkOnePreset(TCameraPreset tCameraPreset) {
        try {
            Map<String, Object> resultMap = getPTZAndPicOnLine(tCameraPreset);
            String onLinePTZStr = "";
            String picOnline = "";
            if (resultMap != null && resultMap.size() > 0) {
                onLinePTZStr = resultMap.get("cameraPtz").toString();
                picOnline = resultMap.get("absPath").toString();
            } else {
                // 图片采集失败，终止校验
                return -2;
            }

            if (!cmpPTZ(onLinePTZStr, tCameraPreset.getPresetPtz())) {
                return -1;
            }

            // 判断是都需要调用算法接口进行图片比对
            if (cameraPresetSecondCheck) {
                String idStr = String.format("%d_%d", tCameraPreset.getCameraId(), tCameraPreset.getPresetId());
                sendPicToAnalyse(picOnline, tCameraPreset.getPresetImg(), idStr);
            }

            return 1;
        } catch (Exception e) {
            log.error("checkOnePreset err: {}", e.getMessage());
            return 1;
        }
    }

    /**
     * 比较相机获取的和库中存储的相机预置位PTZ值
     *
     * @param onLinePTZStr onLinePTZStr
     * @param dbPTZStr dbPTZStr
     * @return result
     */
    private boolean cmpPTZ(String onLinePTZStr, String dbPTZStr) {
        // 当两个ptz都不为空且不相等时，返回false，否则返回true
        if (StringUtils.isNotEmpty(onLinePTZStr) && StringUtils.isNotEmpty(dbPTZStr) && !onLinePTZStr.equals(dbPTZStr)) {
            return false;
        }

        return true;
    }

    /**
     * 获取线上相机的对应预置位的PTZ值
     *
     * @param tCameraPreset tCameraPreset
     * @return result
     */
    private Map<String, Object> getPTZAndPicOnLine(TCameraPreset tCameraPreset) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("cameraId",tCameraPreset.getCameraId());
        params.put("presetId",tCameraPreset.getPresetId());
        params.put("meteName",tCameraPreset.getPresetName());
        Result response = sendPostRequest(Constant.GET_PRESET_PTZ_AND_PIC_URL,params);
        if (response != null && !org.springframework.util.StringUtils.isEmpty(response.getData())) {
            return (Map<String, Object>) response.getData();
        }

        return null;
    }

    /**
     * 发送图片到算法接口进行比对
     *
     * @param pic1 pic1
     * @param pic2 pic2
     * @return result
     */
    private boolean sendPicToAnalyse(String pic1, String pic2, String idStr) {
        // 发送预置位图片和实时抓取图片到算法，进行对比
        // 调用智能分析主机接口进行分析
        try {
            List<Analysis> analysisList = intelAnalysisService.getAnalysisList(pic1, pic2, idStr);
            intelAnalysisService.picAnalyseNoDetection(analysisList);
        }catch (Exception e){
            log.error("调用智能分析主机进行缺陷分析异常：", e);
        }

        return true;
    }

    /**
     * 将当前相机预置位检测结果写入redis
     *
     * @param checkResults checkResults
     */
    private void setPresetCheckResultToRedis(List<CameraPresetCheckResult> checkResults) {
        String presetRedisKey = getPresetRedisKey(checkResults);
        Map<Long, Integer> presetMap = getPresetCheckResultMap(checkResults);
        redisTemplate.opsForValue().set(presetRedisKey,presetMap,1, TimeUnit.DAYS);
    }

    /**
     * 生成相机预置位的redisKey
     *
     * @param checkResults checkResults
     * @return result
     */
    private String getPresetRedisKey(List<CameraPresetCheckResult> checkResults) {
        Long cameraId = getCameraIdByCheckResults(checkResults);
        return getPresetRedisKeyByCameraId(cameraId);
    }

    /**
     * 生成相机预置位的redisKey
     *
     * @param cameraId cameraId
     * @return result
     */
    private String getPresetRedisKeyByCameraId(Long cameraId) {
        return String.format("CAMERA_PRESET_CHECK_RESULT_%d", cameraId);
    }

    /**
     * 生成相机预置位的redisKey
     *
     * @param cameraId cameraId
     * @return result
     */
    private String getPresetRedisLockByCameraId(Long cameraId) {
        return String.format("CAMERA_PRESET_CHECK_LOCK_%d", cameraId);
    }

    /**
     * 获取CameraId
     *
     * @param checkResults checkResults
     * @return result
     */
    private Long getCameraIdByCheckResults(List<CameraPresetCheckResult> checkResults) {
        Optional<Long> result = checkResults.stream().map(item -> item.getPreset().getCameraId()).findFirst();
        return result.get();
    }

    /**
     * 从预置位列表中获取预置位和校验结果映射关系，用于上传到redis
     *
     * @param checkResults checkResults
     * @return result
     */
    private Map<Long, Integer> getPresetCheckResultMap(List<CameraPresetCheckResult> checkResults) {
        return checkResults.stream().collect(Collectors.toMap(
            item ->item.getPreset().getPresetId(),
            obj -> obj.getPresetCheckResult(),
            (key1 , key2) -> key1
        ));
    }

    /**
     * 同步调用远程接口
     *
     * @param url url
     * @param params params
     * @return result
     */
    public Result sendPostRequest(String url,HashMap<String, Object> params) {
        Result response = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                response = serviceRestTemplate.getForObject(url, Result.class,params);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return response;
    }

    /**
     * 将相机采集到的图片覆盖到原预置位图片
     *
     * @param urlPath urlPath
     * @param presetImg presetImg
     * @return result
     */
    public String saveImgToFtpsToCoverOriImg(String urlPath, String presetImg) {
        return intelAnalysisService.upLoadFileToCoverHttpPath(urlPath, presetImg);
    }

    /**
     * 相机预置位图片和ptz信息从巡视系统同步到边缘节点
     *
     * @param tCameraPreset tCameraPreset
     */
    public void SycPresetToEdge(TCameraPreset tCameraPreset) {
        String originIdStr = tCameraPreset.getOriginId();
        if (StringUtils.isEmpty(originIdStr)) {
            // 不是下级节点同步的预置位，不用处理
            return;
        }

        TCameraPreset edgePreset = new TCameraPreset();
        edgePreset.setPresetId(Long.parseLong(originIdStr));
        edgePreset.setPresetPtz(tCameraPreset.getPresetPtz());
        edgePreset.setPresetImg(tCameraPreset.getPresetImg());

        sendMsg(tCameraPreset);
    }

    /**
     * 发送同步预置位消息
     *
     * @param tCameraPreset tCameraPreset
     */
    private void sendMsg(TCameraPreset tCameraPreset) {
        try {
            Map<String, String> jasonMap = new HashMap<>();
            jasonMap.put("presetId", tCameraPreset.getPresetId().toString());
            jasonMap.put("presetPtz", tCameraPreset.getPresetPtz());
            jasonMap.put("presetImg", tCameraPreset.getPresetImg());
            String jsonMessage = JSONUtil.toJSONString(tCameraPreset);
            log.info("需要同步的相机预置位消息：" + jsonMessage);
            Constant.websocketSendMsg(Constant.CAMERA_PRESET_UPDATE_URL, jasonMap);
        } catch (Exception e) {
            log.error("SycPresetToEdge err", e);
        }
    }
}

