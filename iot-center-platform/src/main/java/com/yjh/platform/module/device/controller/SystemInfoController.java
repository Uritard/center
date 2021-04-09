package com.yjh.platform.module.device.controller;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.SystemInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author lqh
 * @since 2020/10/20
 */
@RestController
@RequestMapping("/systemInfo/v1")
@Api(value = "/systemInfo", description = "系统监控")
public class SystemInfoController {

    @Autowired
    private SystemInfoService systemInfoService;
    @Resource
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(SystemInfoController.class);

    public SystemInfoController(SystemInfoService systemInfoService) {
        this.systemInfoService = systemInfoService;
    }


    @ApiOperation(value = "获取内存信息")
    @RequestMapping(value = "/getMemory", method = RequestMethod.GET)
    public Result getMemory(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            String secureVerify=String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify","content"));
            String[] secures=secureVerify.split(",");
            for(String memory:secures){
                if(memory.equals("MemoryFreeMin")){
                    if(userId!=10001){
                        result.setCode(200,"");
                        return result;
                    }
                }
            }
            result.setData(systemInfoService.getMemory());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取内存信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取cpu信息")
    @RequestMapping(value = "/getCPU", method = RequestMethod.GET)
    public Result getCPU(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            String secureVerify=String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify","content"));
            String[] secures=secureVerify.split(",");
            for(String memory:secures){
                if(memory.equals("cpuFreeMin")){
                    if(userId!=10001){
                        result.setCode(200,"");
                        return result;
                    }
                }
            }
            result.setData(systemInfoService.getCPU());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取cpu信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取磁盘信息")
    @RequestMapping(value = "/getDisk", method = RequestMethod.GET)
    public Result getDisk(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            String secureVerify=String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify","content"));
            String[] secures=secureVerify.split(",");
            for(String memory:secures){
                if(memory.equals("DiskFreeMin")){
                    if(userId!=10001){
                        result.setCode(200,"");
                        return result;
                    }
                }
            }
            result.setData(systemInfoService.getSwap());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取磁盘信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取cpu利用率")
    @RequestMapping(value = "/getCpuOnUse", method = RequestMethod.GET)
    public Result getCpuOnUse(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            String secureVerify=String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify","content"));
            String[] secures=secureVerify.split(",");
            for(String memory:secures){
                if(memory.equals("cpuFreeMin")){
                    if(userId!=10001){
                        result.setCode(200,"");
                        return result;
                    }
                }
            }
            result.setData(systemInfoService.getCpuOnUse());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取cpu利用率错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取硬盘利用率")
    @RequestMapping(value = "/getDiskOnUse", method = RequestMethod.GET)
    public Result getDiskOnUse(HttpServletRequest request) {
        Result result = new Result();
        try {
            Long userId=Long.valueOf(request.getHeader("userId"));
            String secureVerify=String.valueOf(redisTemplate.opsForHash().get("t_sys_param:secureVerify","content"));
            String[] secures=secureVerify.split(",");
            for(String memory:secures){
                if(memory.equals("DiskFreeMin")){
                    if(userId!=10001){
                        result.setCode(200,"");
                        return result;
                    }
                }
            }
            result.setData(systemInfoService.getDeskOnUse(request,userId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取硬盘利用率错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取关键服务")
    @RequestMapping(value = "/getServices", method = RequestMethod.GET)
    public Result getServices() {
        Result result = new Result();
        try {
            result.setData(systemInfoService.getServices());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取关键服务错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取NVR信息")
    @RequestMapping(value = "/getNVRInfo", method = RequestMethod.GET)
    public Result getNVRInfo() {
        Result result = new Result();
        try {
            List<Map<String,String>> list = systemInfoService.getNVRInfo();
            result.setData(list);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR信息错误:", e);
        }
        return result;
    }

//    @ApiOperation(value = "获取天气信息")
//    @RequestMapping(value = "/getWeather", method = RequestMethod.GET)
//    public Result getWeather(@RequestParam(value = "data") String data) {
//        Result result = new Result();
////        try {
////            Enumeration portList = CommPortIdentifier.getPortIdentifiers();
////            while (portList.hasMoreElements()) {
////                CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
////                System.out.println("获取到的串口名称为：COM" + portId.getPortType());
////            }
////        } catch (Exception e) {
////            System.out.println(e.getMessage());
////        }
//        try {
//            systemInfoService.getWeather(data);
//            result.setData(1);
//        } catch (BusinessException b) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
//        } catch (Exception e) {
//            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
//            log.error("获取天气信息错误:", e);
//        }
//        return result;
//    }


}
