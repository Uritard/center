package com.yjh.accesstcp.module.device.controller;

import com.google.common.collect.Maps;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.commons.result.ResultCodeEnum;
import com.yjh.accesstcp.module.device.callback.handler.SyncSendHandler;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.IUpSystemService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.module.device.service.UpSystemServiceFactory;
import com.yjh.accesstcp.module.device.service.impl.UpType;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import com.yjh.accesstcp.thread.MessageThread;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author lqh
 * @since 2021/1/12
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/sendToUpSystem/v1")
@Api(value = "/sendToUpSystem", description = "上报服务")
public class SendToUpSystemController {

    private final SendToUpSystemServices sendToUpSystemService;
    private final RedisTemplate redisTemplate;
    private final UpSystemServiceFactory upSystemServiceFactory;

    private Logger log = LoggerFactory.getLogger(SendToUpSystemController.class);

    @ApiOperation(value = "发送")
    @RequestMapping(value = "/send", method = RequestMethod.GET)
    public Result send(@RequestParam(value = "list", required = true) List<Map<String,Object>> list,
                       @RequestParam(value = "type", required = true) String type,
                       @RequestParam(value = "command", required = false) String command,
                       @RequestParam(value = "code", required = false) String code) {
        Result result = new Result();
        try {
            sendToUpSystemService.sendResponse(0L, type,command,code,list, true);
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }


    @ApiOperation(value = "发送")
    @RequestMapping(value = "/sendXML", method = RequestMethod.POST)
    public Result sendXML(@RequestBody Map<String,List<XMLBaseModel>> robotMap) {
        Result result = new Result();
        try {
            String flag = Constant.upSystemFlag();
            if (StringUtils.isEmpty(flag) || Constant.ZERO.equals(flag)){
                log.error("上级系统连接开关为空或者没开！{}", flag);
            } else {
                XMLBaseModel xmlBaseModel = robotMap.get("list").get(0);
                IUpSystemService upSystemService = upSystemServiceFactory.getService(UpType.getEnum(NumberUtils.toInt(flag)));
                upSystemService.sendUp(xmlBaseModel);
            }
            result.setData(1);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("发送失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "发送")
    @RequestMapping(value = "/sendCloudXml", method = RequestMethod.POST)
    public Result sendCloudXml(@RequestBody XMLBaseModel xmlBaseModel) {

        Result result = new Result();
        try {
            String flag = Constant.managerSystemFlag();
            if (Objects.isNull(flag) || "0".equals(flag)) {
                log.error("上级系统算法连接开关为空或者没开！{}", flag);
            } else {
                SocketAddress socketAddress = new InetSocketAddress(Constant.managerSystemIp(), Constant.managerSystemPort());
                TCPClientHandler tcpClientHandler = TCPClientHandler.getTcpClientHandlerHashMap(socketAddress);
                if (tcpClientHandler != null) {
                    if (StringUtils.isBlank(xmlBaseModel.getCode())){
                        xmlBaseModel.setCode(Constant.stationCode());
                    }
                    tcpClientHandler.send(xmlBaseModel, 0L, true);
                } else {
                    log.error("服务未连接，请重试，port: {}", Constant.upSystemPort());
                    result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
                }
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("发送失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "异步消息同步处理-算法管理平台发送")
    @RequestMapping(value = "/syncSendCloudXml", method = RequestMethod.POST)
    public Result syncSendCloudXml(@RequestBody XMLBaseModel xmlBaseModel) {

        Result result = new Result();
        try {
            String flag = Constant.managerSystemFlag();
            if (Objects.isNull(flag) || "0".equals(flag)) {
                log.error("上级系统算法连接开关为空或者没开！{}", flag);
            } else {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(Constant.managerSystemIp(), Constant.managerSystemPort());
                TCPClientHandler tcpClientHandler = TCPClientHandler.getTcpClientHandlerHashMap(inetSocketAddress);
                if (tcpClientHandler != null) {
                    long sendSessionId = tcpClientHandler.getSessionId();
                    SyncSendHandler syncSendHandler = new SyncSendHandler(tcpClientHandler);
                    result.setData(syncSendHandler.send(xmlBaseModel, sendSessionId, 0L, true));
                } else {
                    log.error("服务未连接，请重试，port: {}", Constant.upSystemPort());
                    result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
                }
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("发送失败描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "上传文件")
    @RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
    public Result uploadFile(@RequestBody Map<String, String> fileMap) {
        Result result = new Result();
        try {
            result.setData(sendToUpSystemService.putFile(fileMap.get("filePath"), fileMap.get("targetPath")));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("发送失败描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "模型同步")
    @RequestMapping(value = "/creatModel", method = RequestMethod.GET)
    public Result creatFile(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
            result.setData(sendToUpSystemService.creatModel(type));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }
    @ApiOperation(value = "模型同步")
    @RequestMapping(value = "/creatFile22", method = RequestMethod.GET)
    public Result creatFile22(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
            result.setData(sendToUpSystemService.creatModelList(type));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "巡视结果查询")
    @RequestMapping(value = "/testCount", method = RequestMethod.GET)
    public Result testCount(@RequestParam(value = "type",required = false) String type,@RequestParam(value = "cmd") String cmd,
                            @RequestParam(value = "startTime") String startTime,
                            @RequestParam(value = "endTime") String endTime) {
        Result result = new Result();
        try {
            Map<String,Object> map = Maps.newHashMap();
            map.put("type",type);
            map.put("cmd",cmd);
            map.put("begin_time",startTime);
            map.put("end_time",endTime);
            result.setData(sendToUpSystemService.resultStatistical(map));
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "模型同步上报")
    @RequestMapping(value = "/modelUpload", method = RequestMethod.GET)
    public Result modelUpload(@RequestParam(value = "type") String type) {
        Result result = new Result();
        try {
            String flag = (String) redisTemplate.opsForHash().get("systemConfigKey:upSystem", "upSystemFlag");
            if ("1".equals(flag)) {
                sendToUpSystemService.creatFile(type);
            } else {
                log.error("上级系统连接开关为空或者没开！{}",flag);
            }
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "模型下载")
    @RequestMapping(value = "/modelDownload", method = RequestMethod.POST)
    public Result modelDownload(@RequestBody Map<String,Object> map) {
        Result result = new Result();
        try {
            Map<String,String> mapForPath = redisTemplate.opsForHash().entries("t_sys_param:modelAbsolutePath");
            String abspath = mapForPath.get("content");
            String realPath = Constant.MODEL_RELATIVE_PATH;

            mapForPath = redisTemplate.opsForHash().entries("t_sys_param:fileAbsPath");
            String fileAbsPath = mapForPath.get("content");
            String fileRealPath = Constant.FILE_REAL_PATH;

            String path  = sendToUpSystemService.downloadFile(String.valueOf(map.get("type")));
//            String path  = sendToUpSystemService.downloadFile("1");
            if ("9".equals(map.get("type")) || "10".equals(map.get("type"))){
                result.setData(path.replace(fileAbsPath,fileRealPath));
            }else {
                File file = cn.hutool.core.util.ZipUtil.zip(path);
                path = file.getPath();
                result.setData(path.replace(abspath, realPath));
            }
        } catch (BusinessException e){
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), e.getMessage());
            log.error("失败查询描述：", e);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新TCP连接")
    @RequestMapping(value = "/updateTcpContent", method = RequestMethod.GET)
    public Result register() {
        Result result = new Result();
        try {
            sendToUpSystemService.updateTcpContent();
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "recvTest")
    @RequestMapping(value = "/recvTest", method = RequestMethod.GET)
    public Result recvTest(@RequestParam(value = "xml")String xml) {
        Result result = new Result();
        try {
            Document document = DocumentHelper.parseText(xml);//String转XML
            XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);//解析xml
            MessageHeader header =  new MessageHeader();
            MessageThread.doProcessMessage(ProtocolEnum.IOT, null, xmlRes, header);
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.UPDATEERROR.getCode(), ResultCodeEnum.UPDATEERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

}
