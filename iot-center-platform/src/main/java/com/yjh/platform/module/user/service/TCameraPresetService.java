package com.yjh.platform.module.user.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.controller.TCameraPresetController;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TCameraInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.*;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    private Logger log = LoggerFactory.getLogger(TCameraPresetService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraPreset tCameraPreset) {
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

    @Transactional(rollbackFor = Exception.class)
    public String download(List<Long> cameraIdList){
        Map<String,String> mapForPreset = redisTemplate.opsForHash().entries("t_sys_param:presetImgPath");
        String picPath = mapForPreset.get("content");///home/yjh_iot_center/iot-picture/presets
        Map<String,String> mapForZip = redisTemplate.opsForHash().entries("t_sys_param:zipPath");
        String zipPath = mapForZip.get("content");///home/yjh_iot_center/iot-picture/zip
        Map<String,String> mapForZipReal = redisTemplate.opsForHash().entries("t_sys_param:zipRealPath");
        String zipPathReal = mapForZipReal.get("content");//http://192.168.9.40:10086/imgs/zip

        if(cameraIdList == null){
            cameraIdList = tCameraPresetDao.selectCameraIdList();
        }
        if(cameraIdList != null && cameraIdList.size()==0){
            cameraIdList = tCameraPresetDao.selectCameraIdList();
        }
        if(cameraIdList != null && cameraIdList.size()>0){

            List<Long> cameraHavePresetList = tCameraPresetDao.selectCameraHavePreset(cameraIdList);
            if(cameraHavePresetList == null){
                return "fail";
            }
            if(cameraHavePresetList.size() == 0 ){
                return "fail";
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
                List<Long>presetList = tCameraPresetDao.selectForThisPreset(cameraId);
                if(presetList!=null && presetList.size()>0){
//                    try {
//                    String mk = "mkdir "+zipPath+"/picture/"+cameraId;
//                    String[] cmds = new String[]{"sh","-c",mk};
//                    Runtime.getRuntime().exec(cmds);
//                    } catch (IOException e) {
//                        log.error("复制文件错误："+e);
//                    }
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
                        return "fail";
                    }
                }
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
//                {
//                    Runtime run = Runtime.getRuntime();
//                    File wd = new File("/bin");
//                    Process proc = run.exec("/bin/bash", null, wd);
//                    BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
//                    PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())), true);
//                    out.println("cd " + zipPath);
//                    out.println("pwd");
//                    out.println("zip -r -y picture.zip . -i picture/*");
//                    out.println("exit");
//                    String line;
//                    StringBuilder sb = new StringBuilder();
//                    while ((line = in.readLine()) != null) {
//                        sb.append(line).append("\r\n");
//                        log.info("结果"+sb);
//                    }
//                    proc.waitFor();
//                    in.close();
//                    out.close();
//                    proc.destroy();
//                }
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("复制文件错误："+e);
            }

            return zipPathReal+"/picture.zip";
        }
        return "fail";
    }

    @Transactional(rollbackFor = Exception.class)
    public int upload(MultipartFile file) {
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
            String cmd= "unzip -o "+path+"/copyZip.zip"+" -d"+path+"/copyZip";
            String[] cmds = new String[]{"sh","-c",cmd};
            log.info("linux命令："+cmd);
            Runtime.getRuntime().exec(cmds);
            //解压到zip目录下 再将相关文件复制到对应目录下  /home/yjh/iot-picture/model-picture/sync/Template/BigImg  /预置位
            File zipFile = new File(path+"/copyZip/picture");
            File[] zipFileList = zipFile.listFiles();
            if(zipFileList != null && zipFileList.length>0){
                for(File item:zipFileList){
                    if(item.isDirectory()){
                        {
                            //不覆盖原有的文件
                            File targetFile = new File(modelPath+"/"+item.getName());
                            if(targetFile.exists()){
                                continue;
                            }
                        }
                        String url = "cp -rf " + path+"/copyZip/picture/"+item.getName()+" "+modelPath+"/"+item.getName();
                        String[] cpCmd = new String[]{"sh","-c",url};
                        log.info("linux复制命令："+url);
                        Runtime.getRuntime().exec(cpCmd);
                    }
                }
            }
        }catch (NullPointerException e) {
            e.getMessage();
        } catch (IOException e) {
            e.getMessage();
        }
        return 1;
    }



}

