package com.yjh.accessvqd.module.diagnose.controller;

import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.module.diagnose.entity.Channel;
import com.yjh.accessvqd.module.diagnose.entity.DataServer;
import com.yjh.accessvqd.module.diagnose.service.ChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/channelOperate/v1")
@Api(value = "/channelOperate", description = "监测点操作")
public class ChannelController {

    private Logger log = LoggerFactory.getLogger(ChannelController.class);

    @Autowired
    private ChannelService channelService;
    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "密码加密")
    @RequestMapping(value = "/userPwdEncrypt",method = RequestMethod.GET)
    public Result userPwdEncrypt(@RequestParam String pass, HttpServletRequest request){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
           result.setData(channelService.userPwdEncrypt(pass));
        }catch (Exception e){
            log.error("加密失败："+e);
        }
        return result;
    }

    @ApiOperation(value = "数据服务器配置操作-查询")
    @RequestMapping(value = "/dataServers",method = RequestMethod.GET)
    public  Result dataServers(HttpServletRequest request){
        Result result=new Result();
        try{
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.dataServerConfig());
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("操作失败"+e);
        }
        return result;
    }

    @ApiOperation(value ="新增数据服务器")
    @RequestMapping(value = "/addDataServers",method = RequestMethod.POST)
    public Result addDataServers(@RequestBody DataServer dataServer,
                                 HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.addDataServer(dataServer));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("新增失败"+e);
        }
        return result;
    }

    @ApiOperation(value = "修改数据服务器")
    @RequestMapping(value = "/updateDataServers",method = RequestMethod.PUT)
    public Result update(@RequestBody DataServer dataServer,
                         HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.updateDataServer(dataServer));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("修改失败"+e);
        }
        return result;
    }

    @ApiOperation(value = "删除数据服务器")
    @RequestMapping(value = "deleteDataServers",method = RequestMethod.DELETE)
    public Result deleteDataServers(@RequestParam String serverId,
                                    HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.deleteDateServer(serverId));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除失败"+e);
        }
        return  result;
    }

    @ApiOperation(value = "查询监测点列表")
    @RequestMapping(value = "channelListInfo",method = RequestMethod.GET)
    public Result channelListInfo(HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.channelInfo());
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败"+e);
        }
        return result;
    }

    @ApiOperation(value = "查询单个监测点信息")
    @RequestMapping(value = "getChannel",method = RequestMethod.GET)
    public Result getChannel(@RequestParam String channelId,
                             HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.getChannel(channelId));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("查询失败"+e);
        }
        return result;
    }

    @ApiOperation(value = "新增&修改单个监测点")
    @RequestMapping(value = "updateChannel",method = RequestMethod.POST)
    public Result updateChannel(@RequestBody Channel channel,
                                HttpServletRequest request ){
        Result result=new Result();
        try {
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.addChannel(channel));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("修改失败"+e);
        }
        return result;
    }

    @ApiOperation(value = "删除单个监测点")
    @RequestMapping(value = "/deleteChannel",method = RequestMethod.DELETE)
    public Result deleteChannel(@RequestParam String channelId,
                                HttpServletRequest request ){
        Result result=new Result();
        try{
            Long userId = Long.valueOf(request.getHeader("userId"));
            String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            /*if(!"1235".equals(userRole)){
                //权限不够；
                result.setCode(10008,"用户无权限");
                //throw new BusinessException(10008,"用户无权限");
                //return -1;
            }*/
            result.setData(channelService.deleteChannel(channelId));
        }catch (Exception e){
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除失败"+e);
        }
        return  result;
    }




}
