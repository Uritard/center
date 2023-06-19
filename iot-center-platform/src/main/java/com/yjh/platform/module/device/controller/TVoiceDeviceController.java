package com.yjh.platform.module.device.controller;

import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.ExcelReadListener;
import com.yjh.platform.common.utils.ModelExcelListener;
import com.yjh.platform.module.device.entity.ExcelEntity;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author lqh
 * @since 2020-12-01
 */
@RestController
@RequestMapping("/tVoiceDevice/v1")
@Api(value = "/tVoiceDevice", description = "声纹设备表操作接口")
public class TVoiceDeviceController {

    @Autowired
    private final TVoiceDeviceService tVoiceDeviceService;
    @Autowired
    private LogsRecord logsRecord;
    @Autowired
    private RedisTemplate  redisTemplate;

    private Logger log = LoggerFactory.getLogger(TVoiceDeviceController.class);

    public TVoiceDeviceController(TVoiceDeviceService tVoiceDeviceService) {
        this.tVoiceDeviceService = tVoiceDeviceService;
    }

    @ApiOperation(value = "插入")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @Logs(title = "新增声纹设备数据", content = "根据用户传递的参数新增声纹设备数据", logType = 2,authority = "1234")
    public Result add(@Validated @RequestBody VoiceDeviceAllInfoDetail tVoiceDevice) {
        Result result = new Result();
        try {
            //tVoiceDevice.getfValue();
            //@RequestBody VoiceDeviceAllInfoDetail tVoiceDevice
            tVoiceDevice.setEdgeCode((String) redisTemplate.opsForHash().get(Constant.T_SYS_PARAM+"edgeCode","content"));
            int re = tVoiceDeviceService.add(tVoiceDevice);
            if(re == -1){
                result.setCode(209,"监视设备不存在");
            }else {
                result.setData(re);
                //有变动 同步模型
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("6");
                }
            }

        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @Logs(title = "删除声纹设备数据", content = "根据用户传递的参数删除声纹设备数据", logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "voiceDeviceId", required = true) Long voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.deleteByPrimaryId(voiceDeviceId));
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                Constant.modelUpload("6");
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
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @Logs(title = "修改声纹设备数据", content = "根据用户传递的参数修改声纹设备数据", logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody VoiceDeviceAllInfoDetail tVoiceDevice) {
        Result result = new Result();
        try {
            int re = tVoiceDeviceService.update(tVoiceDevice);
            if(re == -1){
                result.setCode(209,"监视设备不存在");
            }else {
                result.setData(re);
                //有变动 同步模型
                if (Constant.updateSyncModel()) {
                    Constant.modelUpload("6");
                }
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("更新异常:", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("更新错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "主键查询")
    @RequestMapping(value = "/selectByPrimaryId", method = RequestMethod.GET)
    @Logs(title = "查询声纹设备数据", content = "根据用户传递的参数查询声纹设备", logType = 1,authority = "1234")
    public Result selectByPrimaryId(@RequestParam(value = "voiceDeviceId", required = true) Long voiceDeviceId) {
        Result result = new Result();
        try {
            VoiceDeviceAllInfoDetail tVoiceDevice = tVoiceDeviceService.selectByPrimaryId(voiceDeviceId);
            result.setData(tVoiceDevice);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询")
    @RequestMapping(value = "/select", method = RequestMethod.GET)
    @Logs(title = "查询声纹设备数据", content = "根据用户传递的参数查询声纹设备", logType = 1)
    public Result select(@RequestParam(value = "voiceDeviceId", required = false) Long voiceDeviceId,
                         @RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName,
                         @RequestParam(value = "stdDeviceId", required = false) Long stdDeviceId,
                         @RequestParam(value = "deviceType", required = false) String deviceType,
                         @RequestParam(value = "configId", required = false) Long configId,
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                        @RequestParam(value = "voiceType", required = false) String voiceType,
                        @RequestParam(value = "voiceModel", required = false) String voiceModel,
                        @RequestParam(value = "voiceFactory", required = false) String voiceFactory) {
        Result result = new Result();
        try {
            List<TVoiceDevice> list = tVoiceDeviceService.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId
                , upRegionId, voiceType, voiceModel, voiceFactory);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
//    @Logs(title = "查询声纹设备数据", content = "根据用户传递的参数分页查询声纹设备", logType = 1,authority = "1234")
    public Result selectByPage(@RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName,
                               @RequestParam(value = "deviceType", required = false) String deviceType,
                               @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                               @RequestParam(value = "voiceType", required = false) String voiceType,
                               @RequestParam(value = "voiceModel", required = false) String voiceModel,
                               @RequestParam(value = "voiceFactory", required = false) String voiceFactory,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize, HttpServletRequest request) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if(pageSize==0){
                logsRecord.LogsSend(request,"9","导出台账信息","声纹设备台账导出");
            }else{
                logsRecord.LogsSend(request,"1","查询声纹设备数据","根据用户传递的参数分页查询声纹设备");
            }
            result = tVoiceDeviceService.selectByPage(voiceDeviceName,deviceType,upRegionId, voiceType, voiceModel, voiceFactory,pageNum,pageSize);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "批量插入")
    @RequestMapping(value = "/batchAdd", method = RequestMethod.POST)
    @Logs(title = "批量插入声纹设备数据", content = "根据用户传递的参数批量插入声纹设备数据", logType = 2)
    public Result batchAdd(@RequestBody List<VoiceDeviceAllInfoDetail> list) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.batchAdd(list));
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                Constant.modelUpload("6");
            }
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST)
    @Logs(title = "删除声纹设备数据", content = "根据用户传递的参数批量删除声纹设备", logType = 4)
    public Result batchDelete(@RequestParam(value = "voiceDeviceIds") String voiceDeviceIds) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.batchDelete(voiceDeviceIds));
            //有变动 同步模型
            if (Constant.updateSyncModel()) {
                Constant.modelUpload("6");
            }
        } catch (BusinessException e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除失败：" + e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量删除错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "查询音频设备树")
    @RequestMapping(value = "/selectVoiceDeviceTree", method = RequestMethod.GET)
    @Logs(title = "查询音频设备树", content = "音频设备树查询", logType = 1, authority = "1235,1237")
    public Result selectVoiceDeviceTree(@RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName,
                                        HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            List<VoiceDevice> list = tVoiceDeviceService.selectVoiceDeviceTree(voiceDeviceName,userId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "音频分析")
    @RequestMapping(value = "/voiceAnalyse", method = RequestMethod.GET)
//    @Logs(title = "音频分析",content = "音频频谱分析",logType = 5)
    public Result voiceAnalyse(@RequestParam(value = "voicePath") String voicePath) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.voiceAnalyse(voicePath));
        } catch (BusinessException e){
            result.setCode(209,"读取文件失败，音频文件异常");
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "查询音频设备全部信息")
    @RequestMapping(value = "/selectVoiceDeviceInfo", method = RequestMethod.GET)
    //@Logs(title = "音频分析",content = "",logType = 5)
    public Result selectVoiceDeviceInfo() {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.selectVoiceDeviceInfo());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "从PMS系统同步音频设备信息")
    @RequestMapping(value = "/synchronizeFromPMS", method = RequestMethod.GET)
    @Logs(title = "从PMS系统同步音频设备信息",content = "从pms系统同步音频设备信息",logType = 5,authority = "1234")
    public Result synchronizeFromPMS(@RequestParam(value = "pmsId",required = false) String pmsId,
                                     @RequestParam(value = "voiceDeviceId",required = false) Long voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.synchronizeFromPMS(pmsId,voiceDeviceId));
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("从PMS系统同步摄像机信息失败描述：" + e);
        }
        return result;
    }

    @ApiOperation(value = "频率分析")
    @RequestMapping(value = "/frequencyAnalyse", method = RequestMethod.GET)
    @Logs(title = "频率分析",content = "频率分析",logType = 5,authority = "1235,1237")
    public Result frequencyAnalyse(@RequestParam(value = "frequencyPath") String frequencyPath) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.frequencyAnalyse(frequencyPath));
        } catch (BusinessException e){
            result.setCode(209,"读取文件失败，音频文件异常");
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "获取拾音器树")
    @RequestMapping(value = "/selectVoiceTree", method = RequestMethod.GET)
//    @Logs(title = "关闭拾音器",content = "拾音器停止录音",logType = 5,authority = "1235")
    public Result selectVoiceTree() {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.selectVoiceTree());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "注册声纹设备")
    @RequestMapping(value = "/voiceRegisted", method = RequestMethod.GET)
    public Result voiceRegisted() {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.voiceRegisted());
        } catch (BusinessException e){
            result.setCode(209,"获取注册列表失败");
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "excel导入声纹设备")
    @RequestMapping(value = "/import", method = RequestMethod.POST)
    @Logs(title = "excel导入声纹设备",content = "excel导入声纹设备",logType = 1)
    public Result importExcel(MultipartFile file) {
        Result result = new Result();
        try {
            InputStream inputStream = file.getInputStream();
            String[] heards = new String[] {"设备名称", "所属区域", "IP", "端口号", "通道号", "分贝告警值", "频率告警值", "设备类型", "设备型号", "生产厂家"};
            ExcelReadListener<VoiceDeviceAllInfoDetail> modelExcelListener = new ExcelReadListener<>(heards);
            ReadSheet readSheet = new ReadSheet(0);
            EasyExcelFactory.read(inputStream, VoiceDeviceAllInfoDetail.class, modelExcelListener).headRowNumber(1).build().read(readSheet);

            List<VoiceDeviceAllInfoDetail> excelEntities = modelExcelListener.getExcelEntities();
            List<String> errorExcelList = modelExcelListener.getErrorExcelEntities();

            if (CollectionUtils.isNotEmpty(excelEntities)) {
                List<String> errorList = tVoiceDeviceService.importExcel(excelEntities);
                errorExcelList.addAll(errorList);
            }
            if (CollectionUtils.isNotEmpty(errorExcelList)) {
                result.setData(ExcelReadListener.prettyErrors(errorExcelList));
            }
        } catch (BusinessException e){
            result.setCode(e.getCode(), e.getMessage());
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), "导入失败，请检查上传 excel 格式是否正确!");
            log.error("发生异常:", e);
        }
        return result;
    }
}
