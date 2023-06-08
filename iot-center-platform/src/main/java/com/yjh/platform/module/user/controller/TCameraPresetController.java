package com.yjh.platform.module.user.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.SilentConf;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TCameraPresetExpand;
import com.yjh.platform.module.user.service.TCameraPresetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * @author yc
 * @since 2020-08-18
 */
@RestController
@RequestMapping("/tCameraPreset/v1")
@Api(value = "/tCameraPreset")
public class TCameraPresetController {

    @Autowired
    private final TCameraPresetService tCameraPresetService;
    @Autowired
    private  TCameraPresetDao tCameraPresetDao;
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TCameraPresetController.class);

    public TCameraPresetController(TCameraPresetService tCameraPresetService) {
        this.tCameraPresetService = tCameraPresetService;
    }

    @ApiOperation(value = "插入")
    @PostMapping(value = "/add")
    @Logs(title = "新增预置位信息",content = "根据用户传递的参数新增预置位信息",logType = 2,authority = "1234,1235")
    public Result insert( @Validated @RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            int resultNum = 0;
            //判断该预置位是否已被设置
            if(tCameraPresetService.judgePresentNum(tCameraPreset.getCameraId(),tCameraPreset.getPresetNum())){
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), "此相机该预置位点已被设置");
                return result;
            }else {
                int isMicro = tCameraPresetService.microCamera(tCameraPreset);
                if (isMicro > 0) {
                    result.setMessage(ResultCodeEnum.CODE10009.getCode(), "微型相机只可以设置一个预置位");
                    return result;
                }
                //操作数据库
                resultNum = tCameraPresetService.insert(tCameraPreset);

                if (resultNum != 0) {
                    //操作预置位
//                    TCameraPreset tCameraPreset1 =  tCameraPresetService.selectLastOne();

                    HashMap<String, Object> params = new HashMap<>();
                    params.put("cameraId",tCameraPreset.getCameraId());
                    params.put("presetId",tCameraPreset.getPresetId());
                    params.put("meteName",tCameraPreset.getPresetName());
                    params.put("edgeCode",tCameraPreset.getEdgeCode());

                    Result response1 = sendPostRequest(Constant.SET_PRESET_URL,params);//设置预置点
                    String resData = Optional.ofNullable(response1.getData()).orElse("").toString();
                    if (!StringUtils.isEmpty(resData) || isMicro == 0) {
                        Result response2 = sendPostRequest(Constant.CAPTURE_PRESET_URL, params);//预置位抓图
                        JSONObject json = (JSONObject) JSON.toJSON(response2.getData());
                        tCameraPreset.setPresetImg((String) json.get("urlPath"));
                        tCameraPreset.setPresetPtz(resData);
                        tCameraPresetService.update(tCameraPreset);//存图
                        result.setData(resultNum);
                    } else {
                        tCameraPresetDao.deleteByPrimaryId(tCameraPreset.getPresetId());
                        resultNum = 0;
                        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
                        result.setData(resultNum);
                    }
                }
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加预置位信息错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "删除")
    @PostMapping(value = "/delete")
    @Logs(title = "删除新增预置位信息",content = "根据用户传递的参数删除预置位信息",logType = 4,authority = "1234,1235")
    public Result delete(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        Map<String, Object> mapResult = new HashMap<>();
        try {
            TCameraPreset tCameraPreset = tCameraPresetService.selectByPrimaryId(presetId);
            int resultNum = tCameraPresetService.deleteByPrimaryId(presetId);
            if(resultNum == -1){
                result.setCode(209,"此预置位已被配置到巡视点");
            }else {
                //操作预置位
                HashMap<String, Object> params = new HashMap<>();

                params.put("cameraId", tCameraPreset.getCameraId());
                params.put("presetId", tCameraPreset.getPresetId());
                Result response = sendPostRequest(Constant.CANCEL_PRESET_URL, params);

                //int resultNum = 0;
                //操作数据库
//                if (response.getData().equals(true)) {
//                    log.info("nvr删除预置位成功");
//                }
                //resultNum = tCameraPresetService.deleteByPrimaryId(presetId);
                if (1 == resultNum){
                    mapResult.put("code", 200);
                }else {
                    mapResult.put("code", 209);
                }
                mapResult.put("data", resultNum);
                result.setData(mapResult);
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }
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
    @ApiOperation(value = "批量删除")
    @PostMapping(value = "/deleteSelectedPreset")
    @Logs(title = "批量删除新增预置位信息",content = "根据用户传递的参数批量删除预置位信息",logType = 4,authority = "1234")
    public Result deleteSelectedPreset(@RequestParam(value = "presetIds") String presetIds) {
        Result result = new Result();
        try {
            String presetIdArray[] = presetIds.split(",");
            Long pr1 = Long.valueOf(presetIdArray[0]);
            TCameraPreset tCameraPreset =  tCameraPresetService.selectByPrimaryId(pr1);
            String capturePresetPath = tCameraPresetService.getPresetBasePath();
            for (int i = 0;i<presetIdArray.length;i++){
                HashMap<String, Object> params = new HashMap<>();
                params.put("cameraId",tCameraPreset.getCameraId());
                params.put("presetId",Long.valueOf(presetIdArray[i]));
                Result response = sendPostRequest(Constant.CANCEL_PRESET_URL,params);
                String delPresetPic = "rm -rf "+capturePresetPath + "/"+Long.valueOf(presetIdArray[i]);
                try { Runtime.getRuntime().exec(delPresetPic); } catch (Exception e) { e.getMessage(); }
                //操作数据库
                int resultNum = 0;
                //if (response.getData().equals(true)) {
                resultNum = tCameraPresetService.deleteByPrimaryId(Long.valueOf(presetIdArray[i]));
                //}
                if(resultNum == -1){
                    result.setCode(209,"预置位已配置到巡视点");
                    return result;
                }
                result.setData(resultNum);
            }

        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), e.getMessage());
            log.error("删除异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除错误:", e);
        }
        return result;
    }
    @ApiOperation(value = "更新")
    @PostMapping(value = "/update")
    @Logs(title = "修改新增预置位信息",content = "根据用户传递的参数修改预置位信息",logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {
            if( tCameraPreset.getPresetType() != null && tCameraPreset.getPresetType() != 1 ){//设置预置位特殊预置位
                //判断这个相机是否已有特殊预置位
                TCameraPreset keepWatchPreset = tCameraPresetService.countKeepWatchTask(tCameraPreset.getCameraId(),tCameraPreset.getPresetId());
                if (keepWatchPreset != null){
                    result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),"此相机已有特殊预置位，是预置位："+keepWatchPreset.getPresetName());
                    return result;
                }
            }
            result.setData(tCameraPresetService.update(tCameraPreset));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新预置位信息异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新预置位信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @GetMapping(value = "/selectByPrimaryId")
    @Logs(title = "查询新增预置位信息",content = "根据用户传递的参数查询预置位信息",logType = 1)
    public Result selectByPrimaryId(@RequestParam(value = "presetId", required = true) Long presetId) {
        Result result = new Result();
        try {
            TCameraPreset tCameraPreset = tCameraPresetService.selectByPrimaryId(presetId);
            result.setData(tCameraPreset);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("主键查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据摄像机id查询所有预置位信息")
    @GetMapping(value = "/selectByCameraId")
    @Logs(title = "根据摄像机id查询所有预置位信息",content = "根据摄像机查询预置位信息",logType = 1)
    public Result selectByCameraId(@RequestParam(value = "cameraId", required = false) Long cameraId,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraPreset> list = tCameraPresetService.selectByCameraId(cameraId);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "根据预置位名称查询信息")
    @GetMapping(value = "/selectByPresetName")
    @Logs(title = "根据预置位名称查询信息",content = "根据用户传递的参数预置位信息",logType = 1)
    public Result selectByPresetName(@RequestParam(value = "presetName", required = false) String presetName,
                                   @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                   @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(pageNum, pageSize,true,null,true);
            List<TCameraPreset> list = tCameraPresetService.selectByPresetName(presetName);
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("查询失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "查询")
    @GetMapping(value = "/select")
    @Logs(title = "查询预置位信息",content = "根据用户传递的参数查询预置位信息",logType = 1)
    public Result select(@RequestParam(value = "presetId", required = false) Long presetId,
                         @RequestParam(value = "cameraId", required = false) Long cameraId,
                         @RequestParam(value = "presetNum", required = false) Integer presetNum,
                         @RequestParam(value = "presetName", required = false) String presetName,
                         @RequestParam(value = "creatorUser", required = false) String creatorUser,
                         @RequestParam(value = "creatorTime", required = false) Date creatorTime,
                         @RequestParam(value = "isUse", required = false) Integer isUse,
                         @RequestParam(value = "presetImg", required = false) String presetImg,
                         @RequestParam(value = "inspectionPostion", required = false) Integer inspectionPostion,
                         @RequestParam(value = "collectStatus", required = false) Integer collectStatus,
                         @RequestParam(value = "calibrationStatus", required = false) Integer calibrationStatus) {
        Result result = new Result();
        try {
            List<TCameraPreset> list = tCameraPresetService.select(presetId, cameraId, presetNum, presetName, creatorUser, creatorTime, isUse, presetImg, inspectionPostion, collectStatus, calibrationStatus);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @PostMapping(value = "/selectByPage")
    @Logs(title = "查询预置位信息",content = "根据用户传递的参数分页查询预置位信息",logType = 1,authority = "1234")
    public Result selectByPage(@RequestBody TCameraPreset tCameraPreset
                               ) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            Page page = PageHelper.startPage(tCameraPreset.getPageNum() != null ? tCameraPreset.getPageNum() : 1, tCameraPreset.getPageSize() != null ? tCameraPreset.getPageSize() : 0, true, null, true);
            List<TCameraPresetExpand> list = tCameraPresetService.selectByPage(tCameraPreset);
//            for (TCameraPresetExpand tCameraPresetExpand : list) {
//                if (Constant.INTEGER_1.equals(tCameraPresetExpand.getIsKeepWatchTask())) {// 静默位
//                    tCameraPresetExpand.setPresetType(1);
//                } else if (Constant.INTEGER_1.equals((tCameraPresetExpand.getIsKeepWatch()))) {// 守望位
//                    tCameraPresetExpand.setPresetType(2);
//                } else if (Constant.INTEGER_1.equals(tCameraPresetExpand.getIsSecondKeepWatchTask())) {// 秒级静默位
//                    tCameraPresetExpand.setPresetType(3);
//                } else {// 预置位
//                    tCameraPresetExpand.setPresetType(0);
//                }
//            }
            resultMap.put("count", page.getTotal());
            resultMap.put("list", list);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @PostMapping(value = "/batchInsert")
    @Logs(title = "批量插入预置位信息",content = "根据用户传递的参数批量插入预置位信息",logType = 2)
    public Result batchInsert( @RequestBody List<TCameraPreset> list) {
        Result result = new Result();
        try {
            int re = tCameraPresetService.batchInsert(list);
            if(re ==-1){
                result.setCode(209,"预置位已配置到巡视点");
            }else {
                result.setData(re);
            }
        //result.setData(tCameraPresetService.batchInsert(list));
        } catch (Exception e) {
        result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询预置位树")
    @GetMapping(value = "/selectPresetTree")
    @Logs(title = "查询预置位树",content = "根据用户传递的参数查询预置位树信息",logType = 1)
    public Result selectPresetTree(@RequestParam(value = "cameraId", required = false) Long cameraId) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.selectPresetTree(cameraId));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询预置位树失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "根据巡视点，查询预置位树")
    @GetMapping(value = "/selectPresetTreeList")
    @Logs(title = "根据巡视点，查询预置位树",content = "根据用户传递的参数查询预置位树信息",logType = 1)
    public Result selectPresetTreeList(@RequestParam(value = "cruisePoint", required = false) Long cruisePoint) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.selectPresetTreeList(cruisePoint));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询预置位树失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "启动执行相机预置位校验")
    @GetMapping(value = "/startPresetCheck")
    @Logs(title = "启动执行相机预置位校验",content = "根据用户传递的相机ID，启动执行相机预置位校验",logType = 1)
    public Result startPresetCheck(@RequestParam(value = "cameraId", required = false) Long cameraId) {
        Result result = new Result();
        try {
            tCameraPresetService.cameraPresetCheck(cameraId);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("启动执行相机预置位校验：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询预置偏移校验结果")
    @GetMapping(value = "/queryPresetCheckResult")
    @Logs(title = "查询预置偏移校验结果",content = "根据用户传递的参数查询预置偏移校验结果",logType = 1)
    public Result queryPresetCheckResult(@RequestParam(value = "cameraId", required = false) Long cameraId) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.queryPresetCheckResult(cameraId));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询预置偏移校验结果：" + e);
        }
        return result;
    }


    @ApiOperation(value = "下载预置位图片")
    @GetMapping(value = "/download")
    @Logs(title = "下载预置位图片",content = "下载摄像机下预置位的图片",logType = 9)
    public Result download(@RequestParam(value = "cameraIdList", required = false) List<Long> cameraIdList,
                           @RequestParam(value = "presetIdList", required = false) List<Long> presetIdList) {
        Result result = new Result();
        try {
            result=tCameraPresetService.download(cameraIdList,presetIdList);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询预置位树失败：" + e);
        }
        return result;
    }
    @ApiOperation(value = "上传标定图片")
    @PostMapping(value = "/upload")
    @Logs(title = "上传标定图片",content = "上传标定图片",logType = 8)
    public Result upload(@RequestParam(value="file", required=false) MultipartFile file) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.upload(file));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询预置位树失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "查询预置位配置信息")
    @GetMapping(value = "/selectSilentConf")
    @Logs(title = "查询预置位配置信息",content = "根据用户传递的参数查询预置位配置信息",logType = 1)
    public Result selectSilentConf() {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.selectSilentConfInfo());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新预置位配置信息")
    @PostMapping(value = "/updateSilentConf")
//    @Logs(title = "修改新增预置位配置信息",content = "根据用户传递的参数修改预置配置位信息",logType = 3,authority = "1234")
    public Result updateSilentConf(@Validated @RequestBody SilentConf silentConf) {
        Result result = new Result();
        try {
            result.setData(tCameraPresetService.updateSilentConf(silentConf));
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新预置位信息异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新预置位信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "重置预置位")
    @PostMapping(value = "/reset")
    @Logs(title = "重置预置位",content = "根据用户传递的参数更新预置位角度",logType = 3,authority = "1234,1235")
    public Result reset( @Validated @RequestBody TCameraPreset tCameraPreset) {
        Result result = new Result();
        try {

            TCameraPreset cameraPreset = tCameraPresetService.selectByPrimaryId(tCameraPreset.getPresetId());
            if (cameraPreset == null) {
                result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), "预置位信息有误，请检查预置位信息或联系管理员");
                result.setData(0);
                return result;
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("cameraId", cameraPreset.getCameraId());
            params.put("presetId", cameraPreset.getPresetId());
            params.put("meteName", cameraPreset.getPresetName());
            params.put("edgeCode", "");

            Result response1 = sendPostRequest(Constant.SET_PRESET_URL, params);//设置预置点
            if (!StringUtils.isEmpty(response1.getData().toString())) {
                Result response2 = sendPostRequest(Constant.CAPTURE_PRESET_URL, params);//预置位抓图
                JSONObject json = (JSONObject)JSON.toJSON(response2.getData());
                log.info("重置预置位相机抓图结果：{}", response2.getData());

                String urlPath = (String)json.get("urlPath");
                String remotePath = tCameraPresetService.saveImgToFtpsToCoverOriImg(urlPath, cameraPreset.getPresetImg());

                String presetPtz = response1.getData().toString();
                cameraPreset.setPresetPtz(presetPtz);
                tCameraPresetService.update(cameraPreset); // 更新PTZ信息

                cameraPreset.setPresetImg(remotePath);
                tCameraPresetService.SycPresetToEdge(cameraPreset); // 向边缘节点同步预置位图片和ptz信息
                result.setData(1);
            } else {
                result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
                result.setData(0);
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加预置位信息错误:", e);
        }
        return result;
    }
}
