package com.yjh.platform.module.wvp.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.video.api.CameraVendor;
import com.yjh.video.api.entity.*;
import com.yjh.video.api.result.Result;
import com.yjh.video.api.service.IWvpPlatformService;
import com.yjh.video.api.service.VideoServiceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2024/08/07
 */
@Slf4j
@RestController
@RequestMapping("/wvp/v1")
@Api(value = "/wvp", tags = {"wvp 服务迁移"})
public class WvpController {

    private IWvpPlatformService wvpPlatformService = VideoServiceFactory.loadSnapService(CameraVendor.DEF, IWvpPlatformService.class);

    @ApiOperation(value = "分页查询级联平台")
    @RequestMapping(value = "/selectByPage", method = RequestMethod.GET)
    public com.yjh.platform.common.result.Result platforms(@RequestParam(value = "pageNum", required = false, defaultValue = "1") int pageNum,
                                                           @RequestParam(value = "pageSize", required = false, defaultValue = "0") int pageSize) {

        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            PageEntity pageEntity = PageEntity.builder().count(pageSize).page(pageNum).build();
            Result wvpRes = wvpPlatformService.platforms(pageEntity);
            JSONObject jsonRes = (JSONObject) wvpRes.getData();
            Integer count = jsonRes.getInteger("size");
            JSONArray list = jsonRes.getJSONArray("list");
            List<ParentPlatformEntity> list1 = list.toJavaList(ParentPlatformEntity.class);
            resultMap.put("count", count);
            resultMap.put("list", list1);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("分页查询级联平台失败:", e);
        }
        return result;
    }


    @ApiOperation(value = "添加国标级联配置")
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    com.yjh.platform.common.result.Result add(@RequestBody ParentPlatformEntity parentPlatform) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result re = wvpPlatformService.add(parentPlatform);
            if (re.getCode() != 200){
                throw new BusinessException("添加国标级联配置失败!");
            }
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加国标级联配置失败:", e);
        }
        return result;
    }

    /**
     * 修改国标级联配置
     *
     * @return
     */
    @ApiOperation(value = "修改国标级联配置")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    com.yjh.platform.common.result.Result update(@RequestBody ParentPlatformEntity parentPlatform) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            wvpPlatformService.update(parentPlatform);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加国标级联配置失败:", e);
        }
        return result;
    }

    /**
     * 删除国标级联配置
     *
     * @return
     */
    @ApiOperation(value = "删除国标级联配置")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result delete(@RequestParam(value = "serverGBId", required = false)String serverGBId) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            wvpPlatformService.delete(serverGBId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加国标级联配置失败:", e);
        }
        return result;
    }

    /**
     * 分页查询级联平台的所有所有通道
     *
     * @return
     */
    @ApiOperation(value = "分页查询级联平台的所有所有通道")
    @RequestMapping(value = "/channelList", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result channelList(@RequestParam(value = "page", required = false)Integer pageNum,
                                                      @RequestParam(value = "count", required = false)Integer pageSize,
                                                      @RequestParam(value = "query", required = false)String query,
                                                      @RequestParam(value = "online", required = false)Boolean online,
                                                      @RequestParam(value = "catalogId", required = false)String catalogId,
                                                      @RequestParam(value = "platformId", required = false)String platformId,
                                                      @RequestParam(value = "channelType", required = false)Boolean channelType
                                                      ) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            ChannelListEntity channelListEntity = ChannelListEntity.builder()
                    .count(pageSize)
                    .page(pageNum)
                    .page(pageNum)
                    .query(query)
                    .online(online)
                    .catalogId(catalogId)
                    .platformId(platformId)
                    .channelType(channelType)
                    .build();

            Result wvpRes = wvpPlatformService.channelList(channelListEntity);
            JSONObject jsonRes =(JSONObject) wvpRes.getData();
            Integer count = jsonRes.getInteger("size");
            JSONArray list = jsonRes.getJSONArray("list");
            List<ChannelReduce> list1 = list.toJavaList(ChannelReduce.class);
            resultMap.put("count", count);
            resultMap.put("list", list1);
            result.setData(resultMap);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("分页查询级联平台的所有所有通道失败:", e);
        }
        return result;
    }


    @ApiOperation(value = "向上级平台添加国标通道")
    @RequestMapping(value = "/updateChannelForGB", method = RequestMethod.POST)
    com.yjh.platform.common.result.Result updateChannelForGB(@RequestBody UpdateChannelEntity param) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.updateChannelForGB(param);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("向上级平台添加国标通道失败:", e);
        }
        return result;
    }


    @ApiOperation(value = "获取国标服务的配置")
    @RequestMapping(value = "/serverConfig", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result serverConfig(){
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.serverConfig();
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取国标服务的配置失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取国标服务的配置")
    @RequestMapping(value = "/serverConfigB", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result serverConfigB(){
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.serverConfigB();
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取国标服务的配置失败:", e);
        }
        return result;
    }


    @ApiOperation(value = "移除国标服务的配置")
    @RequestMapping(value = "/delChannelForGB", method = RequestMethod.POST)
    com.yjh.platform.common.result.Result delChannelForGB(@RequestBody UpdateChannelEntity param){
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.delChannelForGB(param);
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("移除国标服务的配置失败:", e);
        }
        return result;
    }


    @ApiOperation(value = "同步设备通道")
    @RequestMapping(value = "/devicesSync", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result devicesSync(@RequestParam(value = "deviceId", required = false)String deviceId) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            PlayEntity entity = PlayEntity.builder().deviceId(deviceId).build();
            Result wvpRes = wvpPlatformService.devicesSync(entity);
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("同步设备通道失败:", e);
        }
        return result;
    }

    /**
     * 同步设备通道状态
     *
     * @return
     */
    @ApiOperation(value = "同步设备通道")
    @RequestMapping(value = "/devicesSyncStatus", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result devicesSyncStatus(@RequestParam(value = "deviceId", required = false)String deviceId) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            PlayEntity entity = PlayEntity.builder().deviceId(deviceId).build();
            Result wvpRes = wvpPlatformService.devicesSyncStatus(entity);
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("同步设备通道失败:", e);
        }
        return result;
    }

    /**
     * 获取目录
     *
     * @return
     */
    @ApiOperation(value = "获取目录")
    @RequestMapping(value = "/catalog", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result catalog(@RequestParam(value = "platformId", required = false)String platformId,
                                                  @RequestParam(value = "parentId", required = false)String parentId) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            result.setData(new ArrayList<>());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取目录失败:", e);
        }
        return result;
    }

    /**
     * 获取服务配置
     *
     * @return
     */
    @ApiOperation(value = "获取服务配置")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result info(@RequestParam(value = "id", required = false)String id) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.info(id);
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取服务配置失败:", e);
        }
        return result;
    }

    /**
     * 查询上级平台是否存在
     *
     * @return
     */
    @ApiOperation(value = "查询上级平台是否存在")
    @RequestMapping(value = "/exit", method = RequestMethod.GET)
    com.yjh.platform.common.result.Result platformExit(@RequestParam(value = "serverGBid", required = false)String serverGBid) {
        com.yjh.platform.common.result.Result result = new com.yjh.platform.common.result.Result();
        try {
            Result wvpRes = wvpPlatformService.platformExit(serverGBid);
            result.setData(wvpRes.getData());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取服务配置失败:", e);
        }
        return result;
    }

}
