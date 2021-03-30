package com.yjh.accessvideo.module.control.controller;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.commons.utils.http.HttpClientUtils;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.entity.TemperatureInfo;
import com.yjh.accessvideo.module.control.service.CameraConService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/camera/v1")
@Api(value = "/cameraControl", description = "相机接口")
public class CameraConController {

    private Logger log = LoggerFactory.getLogger(CameraConController.class);

    @Autowired
    private CameraConService cameraConService;

   /* @Autowired
    private DoorMachineService doorMachineService;*/

    @Value("${nginx.picture.reflact}")
    private String capturePath;//图片路径

    @Value("${nginx.picture.preset}")
    private String capturePathPreset;//预置位图片路径

    @Value("${nvr.capture.result}")
    private String captureResultPath;//结果路径

    @Value("${nvr.capture.Preset}")
    private String capturePresetPath;//预置位路径

    @Value("${srs.stop.url}")
    private String srsStopUrl;//srs停止播流

    @ApiOperation(value = "相机播放")
    @RequestMapping(value = "/startRealPlay", method = RequestMethod.GET)
    public Result startRealPlay(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.startRealPlay(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "视频进程关闭")
    @RequestMapping(value = "/stopProcess", method = RequestMethod.POST)
    public void stopProcess(){

        String cid=null;
        try{
            //获取所有视频流信息
            //发送请求获取所有STREAM
            String getInfoUrl="http://"+srsStopUrl+":8082/api/v1/streams/";
            JSONObject jsonList = HttpClientUtils.sendGet(getInfoUrl, null);
            List<String> streamsJsonObjectList = JSONArray.parseArray(jsonList.getString("streams"),String.class);
            for (int i = 0; i < streamsJsonObjectList.size(); i++) {
                String streambeanStr = streamsJsonObjectList.get(i);
                JSONObject streambeanJson = JSONObject.parseObject(streambeanStr);

                //解析每一个stream，循环比对，找到页面传递的设备ID对应的流，并判断是否需要关闭
                String publish = streambeanJson.getString("publish");
                JSONObject publishjson = JSONObject.parseObject(publish);
                Integer clients = streambeanJson.getInteger("clients"); //观看人数

                if(clients==0 || Objects.equals("false",publishjson.getString("active"))){
                    //符合无人观看的条件
                    cid = publishjson.getString("cid");
                    //踢掉
                    if(StringUtils.isNotEmpty(cid)){
                        String delteUrl="http://"+srsStopUrl+":8082/api/v1/clients/"+cid;
                        HttpClientUtils.httpDelete(delteUrl,null);
                    }
                    break;
                }
            }
        }catch (Exception e){
            log.error("关流异常",e,this.getClass());
        }
    }

    @ApiOperation(value = "相机停止播放")
    @RequestMapping(value = "/stopRealPlay", method = RequestMethod.GET)
    public Result stopRealPlay(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "rtmpUrl", required = false) String rtmpUrl) {
        Result result = new Result();
        try {
            result.setData(cameraConService.stopRealPlay(cameraId, rtmpUrl));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机批量播放")
    @RequestMapping(value = "/batchStartRealPlay", method = RequestMethod.GET)
    public Result batchStartRealPlay(@RequestParam(value = "cameraIds") String cameraIds) {
        Result result = new Result();
        try {
            result.setData(cameraConService.batchStartRealPlay(cameraIds));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机批量播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人相机播放")
    @RequestMapping(value = "/robotStartRealPlay", method = RequestMethod.GET)
    public Result robotStartRealPlay(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.robotStartRealPlay(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人相机播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人相机停止播放")
    @RequestMapping(value = "/robotStopRealPlay", method = RequestMethod.GET)
    public Result robotStopRealPlay(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.robotStopRealPlay(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机批量停止播放")
    @RequestMapping(value = "/batchStopRealPlay", method = RequestMethod.POST)
    public Result batchStopRealPlay(@RequestBody List<Map<String, String>> list) {
        Result result = new Result();
        try {
            List<String> resultList = new ArrayList<>();
            for (Map<String, String> map:list) {
                String resultBack = cameraConService.stopRealPlay(Long.valueOf(map.get("cameraId")), map.get("rtmpUrl"));
                resultList.add(resultBack);
            }
            result.setData(resultList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机批量停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "视频回放")
    @RequestMapping(value = "/startPlayBack", method = RequestMethod.GET)
    public Result startPlayBack(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "startTime") String startTime,
                                @RequestParam(value = "stopTime") String stopTime) {
        Result result = new Result();
        try {
            result.setData(cameraConService.startPlayBack(cameraId, startTime, stopTime));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("视频回放失败:", e);
        }
        return result;
    }

//    TILT_UP 21 云台上仰 TILT_DOWN 22 云台下俯 PAN_LEFT 23 云台左转 PAN_RIGHT 24 云台右转
//    11 焦距变大(倍率变大) 12 焦距变小(倍率变小) 25 云台上仰和左转 26 云台上仰和右转 27 云台下俯和左转 28 云台下俯和右转 29 云台左右自动扫描
    @ApiOperation(value = "云台控制")
    @RequestMapping(value = "/ptzControl", method = RequestMethod.GET)
    public Result ptzControl(@RequestParam(value = "dwPTZCommand") int dwPTZCommand,
                             @RequestParam(value = "cameraId") Long cameraId,
                             @RequestParam(value = "dStop") int dStop,
                             @RequestParam(value = "speed") int speed) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            result.setData(cameraConService.pTZControl(dwPTZCommand, cameraId, dStop, speed));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("云台控制错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机抓图")
    @RequestMapping(value = "/capturePicture", method = RequestMethod.GET)
    public Result capturePicture(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            cameraConService.isCameraControlled(cameraId);
            int max=9999,min=1;
            int ran = (int) (Math.random()*(max-min)+min);
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
            String filePathTem = "/" + formatter.format(new Date())+ ran + ".jpg";
            String filePath = captureResultPath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId);
            String urlPath = capturePath+filePathTem;
            resultMap.put("urlPath", urlPath);
            resultMap.put("absPath", filePath);
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务中相机抓图")
    @RequestMapping(value = "/capturePictureForTask", method = RequestMethod.GET)
    public Result capturePictureForTask(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int max=9999,min=1;
            int ran = (int) (Math.random()*(max-min)+min);
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
            String filePathTem = "/" + formatter.format(new Date())+ ran + ".jpg";
            String filePath = captureResultPath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId);
            String urlPath = capturePath+filePathTem;
            resultMap.put("urlPath", urlPath);
            resultMap.put("absPath", filePath);
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务中相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "预置位抓图")
    @RequestMapping(value = "/capturePresetPicture", method = RequestMethod.GET)
    public Result capturePresetPicture(@RequestParam(value = "presetId") Long presetId,
                                 @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            cameraConService.isCameraControlled(cameraId);
            String filePathTem = "/"+presetId+"/" + presetId + ".jpg";
            String filePath = capturePresetPath + filePathTem;
            String mkdir = "mkdir "+capturePresetPath+"/"+presetId;
            log.info("mkdir: "+mkdir);
            Runtime.getRuntime().exec(mkdir);
            Thread.sleep(2000);
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId);
            String urlPath = capturePathPreset+filePathTem;
            resultMap.put("urlPath", urlPath);
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "转到预置点")
    @RequestMapping(value = "/moveToPreset", method = RequestMethod.GET)
    public Result moveToPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("转到预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务中转到预置点")
    @RequestMapping(value = "/moveToPresetForTask", method = RequestMethod.GET)
    public Result moveToPresetForTask(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务转到预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "设置预置点")
    @RequestMapping(value = "/setPreset", method = RequestMethod.GET)
    public Result setPreset(@RequestParam(value = "presetId") Long presetId,
                            @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.SET_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设置预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "清除预置点")
    @RequestMapping(value = "/cancelPreset", method = RequestMethod.GET)
    public Result cancelPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
//            String filePathTem = "/"+presetId+"/" + presetId + ".jpg";
//            String filePath = capturePresetPath + filePathTem;
            String cmd = "rm -rf "+capturePresetPath+"/"+presetId;
            log.info("删除语句"+cmd);
            Runtime.getRuntime().exec(cmd);
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.CLE_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("清除预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取相机状态")
    @RequestMapping(value = "/getCameraStatus", method = RequestMethod.GET)
    public Result getCameraStatus(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getCameraStatus(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取相机状态失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取NVR下挂相机树状态")
    @RequestMapping(value = "/getCameraTreeStatus", method = RequestMethod.GET)
    public Result getCameraTreeStatus(@RequestParam(value = "cameraName",required = false) String cameraName,
                                      @RequestParam(value = "flag",required = false) Integer flag) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getCameraStatusTree(cameraName,flag));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR下挂相机树失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取NVR存储状态")
    @RequestMapping(value = "/getNVRStoreInfo", method = RequestMethod.GET)
    public Result getNVRStoreInfo(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getNVRStoreInfo(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR存储状态失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "NVR注册接口")
    @RequestMapping(value = "/registerNVR", method = RequestMethod.GET)
    public Result registerNVR(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.registerNVR(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("NVR注册失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取到视频存入指定文件中 保存为Mp4格式文件")
    @RequestMapping(value = "/getDVRToPlace", method = RequestMethod.GET)
    public Result getDVRToPlace(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "startTime") String startTime,
                                @RequestParam(value = "endTime") String endTime)  {
        Result result = new Result();
        try {
            String  url= cameraConService.getDVRToPlace(cameraId,startTime,endTime);
            log.info("service返回值："+url);
            if(null!=url) {
                result.setData(url);
            } else { result.setData("下载失败请重新下载"); }
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }
    @ApiOperation(value = "获取到视频文件列表")
    @RequestMapping(value = "/getFileList", method = RequestMethod.GET)
    public Result getFileList(@RequestParam(value = "cameraId") Long cameraId,
                              @RequestParam(value = "startTime") String startTime,
                              @RequestParam(value = "endTime") String endTime){
        Result result = new Result();
        try {
                List<HCNetSDK.NET_DVR_FIND_DATA> list= cameraConService.geifile(cameraId,startTime,endTime);
                Map<String,String> map =new HashMap<String,String>();
                String[] s = new String[2];
                int iTemp;
                String MyString;
                if (list.size()>0)
                {
                    for (HCNetSDK.NET_DVR_FIND_DATA po :list)
                    {
                        s = new String(po.sFileName).split("\0", 2);
                        map.put("FileName",new String(s[0]));
                        map.put("startTime",po.struStartTime.toStringTime());
                        map.put("endTime",po.struStopTime.toStringTime());
                        if (po.dwFileSize < 1024 * 1024)
                        {
                            iTemp = (po.dwFileSize) / (1024);
                            MyString = iTemp + "K ";
                        }
                        else
                        {
                            iTemp = (po.dwFileSize) / (1024 * 1024);
                            MyString = iTemp + "M";
                            iTemp = ((po.dwFileSize) % (1024 * 1024)) / (1204);
                            MyString = MyString + iTemp + "K";
                        }
                        map.put("fileSize",MyString);
                    }
                    result.setData(map);
                   // result.setData(list);
                }else {result.setData("文件不存在");}
        }catch (Exception e)
        {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.info(e.getMessage());
        }
        return result;

    }
    @ApiOperation(value = "获取全屏最大温度值")
    @RequestMapping(value = "/getTemperature", method = RequestMethod.GET)
    public Result getTemperature(@RequestParam(value = "cameraId",required = false) Long cameraId)
    {
        Result result = new Result();
        List<String> list=cameraConService.getTemperature(cameraId);
        if (list.size()>0)
        {
            result.setData(list);
        }else {
            result.setData("获取温度失败");
        }

        return result;
    }

    @ApiOperation(value = "获取文件")
    @RequestMapping(value = "/givePicFir", method = RequestMethod.GET)
    public Result givePicFir(@RequestParam(value = "presetId",required = false) Long presetId,
                               @RequestParam(value = "cameraId",required = false) Long cameraId) {
        Result result = new Result();
        Map<String,String> map=cameraConService.givePicFir(cameraId,presetId);
        if (map.size()>0)
        {
            result.setData(map);
            result.setMessage("success");
        }else
            {
                result.setData("获取文件失败");
            }

        return result;
    }
    @ApiOperation(value = "开启可视对讲")
    @RequestMapping(value = "/startVoiceTalk", method = RequestMethod.GET)
    public Result startVoiceTalk( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
    {
        Result result = new Result();
        int re=cameraConService.startVoiceTalk(videoIntercomId);
        if (re>0)
        {
            result.setData("开启对讲");
        }else
            {
                result.setData("无法开启对讲");
            }

        return result;
    }
    @ApiOperation(value = "关闭可视对讲")
    @RequestMapping(value = "/stopVoiceTalk", method = RequestMethod.GET)
    public Result stopVoiceTalk()
    {
        Result result = new Result();
        try {
            cameraConService.stopVoiceTalk();
            result.setData("关闭对讲");

        }catch (Exception e){
            result.setData("系统异常");
        }
        return result;
    }

    @ApiOperation(value = "可视对讲播放")
    @RequestMapping(value = "/startVideoRealPlay", method = RequestMethod.GET)
    public Result startVideoRealPlay( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
    {
        Result result = new Result();
       try {
           Map<String, Object> map=cameraConService.startVideoRealPlay(videoIntercomId);
           if (map.size()>0)
           {
               result.setData(map);
           }else
               {
                   result.setData("地址不存在");
               }
       }catch (Exception e)
       {
           result.setData("系统异常");
           log.info(e.getMessage());
       }

        return result;
    }

    /**
     * \获取可视对讲在  状态
     * @param videoIntercomId
     * @return
     */
    @RequestMapping(value = "/getVidemoIntercomStatus", method = RequestMethod.GET)
    public Result  getVidemoIntercomStatus ( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
    {
        Result result = new Result();
        result.setData(cameraConService.getVidemoIntercomStatus(videoIntercomId));
        return result;
    }

    /**
     *
     * @param
     * @return
     */

    @ApiOperation(value = "根据坐标获取温度")
    @RequestMapping(value = "/PointTemperature", method = RequestMethod.POST)
    public Result  PointTemperature ( @RequestBody TemperatureInfo temperatureInfo)
    {
        log.info(temperatureInfo.toString());
        Result result = new Result();
       if (temperatureInfo.getPicPath()!=null&&temperatureInfo.getPicPath()!="")
       {
           result.setData(cameraConService.lineTemperature(temperatureInfo.getPicPath(),temperatureInfo.getPoints()));
       }
       else{
           result.setData(cameraConService.getlineTemperature(temperatureInfo.getCameraId(),temperatureInfo.getPoints()));
       }
        return result;
    }



    /*@ApiOperation(value = "门口机布防")
    @RequestMapping(value = "/doorMa", method = RequestMethod.GET)
    public Result doorMa(@RequestParam(value = "presetId",required = false) Long presetId,
                             @RequestParam(value = "cameraId",required = false) Long cameraId) {
        Result result = new Result();

        try {
            doorMachineService.SetupAlarmChan();
            result.setData("布防成功");
        }catch (Exception e)
        {
            result.setData("布防失败");
        }
        return result;
    }*/





}
