package com.yjh.platform.module.device.controller;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.entity.TVoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDevice;
import com.yjh.platform.module.device.entity.VoiceDeviceAllInfoDetail;
import com.yjh.platform.module.device.service.TVoiceDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
            int re = tVoiceDeviceService.add(tVoiceDevice);
            if(re == -1){
                result.setCode(209,"监视设备不存在");
            }else {
                result.setData(re);
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
    @RequestMapping(value = "/delete", method = RequestMethod.DELETE)
    @Logs(title = "删除声纹设备数据", content = "根据用户传递的参数删除声纹设备数据", logType = 4,authority = "1234")
    public Result delete(@RequestParam(value = "voiceDeviceId", required = true) Long voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.deleteByPrimaryId(voiceDeviceId));
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
    @RequestMapping(value = "/update", method = RequestMethod.PUT)
    @Logs(title = "修改声纹设备数据", content = "根据用户传递的参数修改声纹设备数据", logType = 3,authority = "1234")
    public Result update(@Validated @RequestBody VoiceDeviceAllInfoDetail tVoiceDevice) {
        Result result = new Result();
        try {
            int re = tVoiceDeviceService.update(tVoiceDevice);
            if(re == -1){
                result.setCode(209,"监视设备不存在");
            }else {
                result.setData(re);
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
                         @RequestParam(value = "upRegionId", required = false) Long upRegionId) {
        Result result = new Result();
        try {
            List<TVoiceDevice> list = tVoiceDeviceService.select(voiceDeviceId, voiceDeviceName, stdDeviceId, deviceType, configId, upRegionId);
            result.setData(list);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "分页查询")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    @Logs(title = "查询声纹设备数据", content = "根据用户传递的参数分页查询声纹设备", logType = 1,authority = "1234")
    public Result selectByPage(@RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName,
                               @RequestParam(value = "deviceType", required = false) String deviceType,
                               @RequestParam(value = "upRegionId", required = false) Long upRegionId,
                               @RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                               @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            result = tVoiceDeviceService.selectByPage(voiceDeviceName,deviceType,upRegionId,pageNum,pageSize);
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
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("批量插入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "批量删除")
    @RequestMapping(value = "/batchDelete", method = RequestMethod.DELETE)
    @Logs(title = "删除声纹设备数据", content = "根据用户传递的参数批量删除声纹设备", logType = 4)
    public Result batchDelete(@RequestParam(value = "voiceDeviceIds") String voiceDeviceIds) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.batchDelete(voiceDeviceIds));
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
    @Logs(title = "查询音频设备树", content = "音频设备树查询", logType = 1)
    public Result selectVoiceDeviceTree(@RequestParam(value = "voiceDeviceName", required = false) String voiceDeviceName) {
        Result result = new Result();
        try {
            List<VoiceDevice> list = tVoiceDeviceService.selectVoiceDeviceTree(voiceDeviceName);
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
    @Logs(title = "频率分析",content = "频率分析",logType = 5,authority = "1235")
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

    @ApiOperation(value = "开启拾音器")
    @RequestMapping(value = "/startRecord", method = RequestMethod.GET)
//    @Logs(title = "开启拾音器",content = "拾音器开始录音",logType = 5,authority = "1235")
    public Result startRecord(@RequestParam(value = "voiceDeviceId", required = false) Long voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.startRecord(voiceDeviceId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "关闭拾音器")
    @RequestMapping(value = "/stopRecord", method = RequestMethod.GET)
//    @Logs(title = "关闭拾音器",content = "拾音器停止录音",logType = 5,authority = "1235")
    public Result stopRecord(@RequestParam(value = "voiceDeviceId", required = false) Long voiceDeviceId) {
        Result result = new Result();
        try {
            result.setData(tVoiceDeviceService.stopRecord(voiceDeviceId));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败描述：", e);
        }
        return result;
    }

}
