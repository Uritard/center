package com.yjh.platform.module.task.controller;


import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.service.TStdRegionService;
import com.yjh.platform.module.task.entity.RegionPath;
import com.yjh.platform.module.task.entity.StationCount;
import com.yjh.platform.module.task.service.HomePageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/12/14
 */
@RestController
@RequestMapping("/homePage/v1")
@Api(value = "/homePage", description = "首页相关接口")
public class HomePageController {

    private Logger log = LoggerFactory.getLogger(HomePageController.class);

    @Autowired
    private HomePageService homePageService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TStdRegionService tStdRegionService;

    @ApiOperation(value = "巡视任务数据概览")
    @RequestMapping(value = "/taskInfo", method = RequestMethod.GET)
    public Result taskInfo(@RequestParam(value = "date", required = true) Integer date) {
        Result result = new Result();
        try {
            result.setData(homePageService.taskInfo(date));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取巡视任务数据概览错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "统计缺陷")
    @RequestMapping(value = "/countByDefectLevel", method = RequestMethod.GET)
    public Result countByDefectLevel() {
        Result result = new Result();
        try {
            result.setData(homePageService.countByDefectLevel());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取巡视任务数据概览错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "正在执行的任务")
    @RequestMapping(value = "/taskOnExecute", method = RequestMethod.GET)
    public Result taskOnExecute() {
        Result result = new Result();
        try {
            result.setData(homePageService.taskOnExecute());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取正在执行的任务错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "告警级别数据")
    @RequestMapping(value = "/countByAlarmLevel", method = RequestMethod.GET)
    public Result countByAlarmLevel() {
        Result result = new Result();
        try {
            result.setData(homePageService.countByAlarmLevel());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取告警级别数据错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "告警内容数据")
    @RequestMapping(value = "/warnInfo", method = RequestMethod.GET)
    public Result warnInfo(@RequestParam(value = "alarmLevel", required = false) Integer alarmLevel) {
        Result result = new Result();
        try {
            result.setData(homePageService.selectThereWarn(alarmLevel));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取告警内容数据错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "机器人数据")
    @RequestMapping(value = "/robotInfoForHomePage", method = RequestMethod.GET)
    public Result robotInfoForHomePage(@RequestParam(value = "robotPosition", required = false) String robotPosition) {
        Result result = new Result();
        try {
            result.setData(homePageService.robotInfoForHomePage(robotPosition));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取机器人数据错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "变电站概况数据")
    @RequestMapping(value = "/stationInfo", method = RequestMethod.GET)
    public Result stationInfo() {
        Result result = new Result();
        try {
            result.setData(homePageService.stationInfo());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取变电站概况数据错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "获取摄像机分组信息")
    @RequestMapping(value = "/getCameraGroupInfo", method = RequestMethod.GET)
    public Result getCameraGroupInfo() {
        Result result = new Result();
        try {
            result.setData(homePageService.getCameraGroupInfo());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取摄像机分组信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取摄像机id信息")
    @RequestMapping(value = "/getCameraIdInfo", method = RequestMethod.GET)
    public Result getCameraIdInfo(@RequestParam(value = "groupId", required = false) Long groupId) {
        Result result = new Result();
        try {
            if(groupId == null){
                result.setData(homePageService.getCameraIdInfo());
            }else {
                result.setData(homePageService.getCameraIdInfoForGroup(groupId));
            }

        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取摄像机id信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "用来接受微气象服务数据的接口")
    @RequestMapping(value = "/getWeatherInfoForService", method = RequestMethod.POST)
    public Result getWeatherInfoForService(@RequestBody Map<String, String> map)  {
        Result result = new Result();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            log.info("天气信息"+map);
            //Map mapa = JSON.parseObject(map);
            Map mapa = map;
            if(map.get("windSpeed") != null){
                Double sp = Double.valueOf(map.get("windSpeed"));
                if(sp!= null && 0==sp){
                    mapa.put("windDirection","--");
                }
            }

//            Integer temp1 = Integer.valueOf(mapa.get("windDirection").toString());
//            String windDirection="";
//            if(temp1 == 0 || temp1 ==360){
//                windDirection = "北";
//            }
//            if(temp1 == 90){
//                windDirection = "东";
//            }
//            if(temp1 == 180){
//                windDirection = "南";
//            }
//            if(temp1 == 270){
//                windDirection = "西";
//            }
//            if(temp1 > 0 && temp1 < 90){
//                windDirection = "东北";
//            }
//            if(temp1 > 90 && temp1 < 1800){
//                windDirection = "东南";
//            }
//            if(temp1 > 180 && temp1 < 270){
//                windDirection = "西南";
//            }
//            if(temp1 > 270 && temp1 < 360){
//                windDirection = "西北";
//            }
//            mapa.put("windDirection",windDirection);
            mapa.put("getTime",simpleDateFormat.format(new Date()));
            //备注，根据站所id  stationId 存放redis中， stationId 根据机器人编码 从数据库中查询
            redisTemplate.opsForHash().putAll("weatherInfoForLastValue",mapa);


            Constant.weatherInfo = mapa;
            result.setData(1);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取天气信息错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取天气信息")
    @RequestMapping(value = "/getWeatherInfo", method = RequestMethod.GET)
    public Result getWeatherInfo()  {
        Result result = new Result();
        try {
//            if(Constant.weatherInfo == null || Constant.weatherInfo.size() ==0){
//                Map<String,String> map  = redisTemplate.opsForHash().entries("weatherInfoForLastValue");
//                result.setData(map);
//            }else {
//                result.setData(Constant.weatherInfo);
//            }
            Map<String,String> map  = redisTemplate.opsForHash().entries("weatherInfoForLastValue");
            result.setData(map);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取天气信息错误:", e);
        }
        return result;
    }

    /**
     * 查询环境设备告警数据
     * @date 2022
     */
    @ApiOperation(value = "查询环境设备告警数据")
    @RequestMapping(value = "/queryEnvWarning", method = RequestMethod.POST)
    public Result envWarningQuery (@RequestBody JSONObject obj){
        log.info( "查询环境设备告警");
        Result result = new Result();
        try{
            HashMap<String,Object> map = new HashMap<>();
            Page<Object> page = PageHelper.startPage(obj.getInteger("pageNum"), obj.getInteger("pageSize"));
            List<HashMap<String, String>> list = homePageService.envWarningQuery(obj);
            map.put("count", page.getTotal());
            map.put("list",list);
            result.setData(map);
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch(Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询环境设备告警数据错误:", e);
        }
        return result;
    }

    /**
     * 查詢微气象数据信息
     * station  站所id
     */
    @ApiOperation(value = "查询微气象数据信息")
    @RequestMapping(value = "/queryWeatherInfo", method = RequestMethod.POST)
    public Result queryWeatherInfo(@RequestBody JSONObject object){
        Long regionId = object.getLong("regionId");
        Result result = new Result();
        try {
            if (regionId!=null){
                result.setData(homePageService.queryWeatherInfo(regionId));
            }
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询微气象数据信息错误:", e);
        }
        return result;
    }

    /**
    * 站所概况统计
    * @date 2022/3/1
    */
    @ApiOperation(value = "站所概况统计")
    @RequestMapping(value = "/queryStations",method = RequestMethod.GET)
    public Result queryStations(@RequestParam(value = "regionId",required = false)Long regionId){
        Result result = new Result();
        try {
            //查询该regionId的子节点
            List<Long> regionIdList = tStdRegionService.selectDownId(regionId);
            StationCount sta = new StationCount();
            sta.setType("环控设备");
            //环控数量
            List weather = (List) redisTemplate.opsForHash().get("Weather", regionId+"");

            if (weather!=null && weather.size()>0){
                sta.setCount(weather.size()+"");
            }else {
                sta.setCount("0");
            }
            List<StationCount> list = homePageService.queryStations(regionIdList);
            list.add(sta);
            result.setData(list);
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("站所概况统计信息错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "站所地图查询")
    @RequestMapping(value = "/queryRegionPath",method = RequestMethod.GET)
    public Result queryRegionPath(@RequestParam(value = "regionId",required = false)Long regionId){
        Result result = new Result();
        try {
            RegionPath regionPath = homePageService.queryRegionPath(regionId);
            result.setData(regionPath);
            result.setCode(ResultCodeEnum.NORMAL.getCode(), ResultCodeEnum.NORMAL.getName());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("站所地图查询错误:", e);
        }
       return result;
    }


    /**
     * 告警内容数据
     * @date 2022/3/1
     */
    @ApiOperation(value = "首页告警统计数据展示")
    @RequestMapping(value = "/deviceWarnInfo", method = RequestMethod.GET)
    public Result deviceWarnInfo(@RequestParam(value ="state",required = false,defaultValue = "275")String state) {
        Result result = new Result();
        try {
            result.setData(homePageService.deviceWarnInfo(state));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取告警内容数据错误:", e);
        }
        return result;
    }
}
