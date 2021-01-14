package com.yjh.accessvqd.module.diagnose.controller;

import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.commons.result.ResultCodeEnum;
import com.yjh.accessvqd.module.diagnose.service.ChannelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.yjh.accessvqd.module.diagnose.entity.TestList;

import java.util.Base64;


/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/channelOperate/v1")
@Api(value = "/channelOperate", description = "监测点操作")
public class ChannelController {

    private Logger log = LoggerFactory.getLogger(ChannelController.class);

    private ChannelService channelService;

    @ApiOperation(value = "密码加密")
    @RequestMapping(value = "userPwdEncrypt",method = RequestMethod.GET)
    public Result userPwdEncrypt(@RequestParam String pass){
        Result result=new Result();
        try {
            String key = "ivms6@hikvision$";
            String iv="8807599889957088";
            TestList test = new TestList();
            byte[] js = test.encrypt(pass.getBytes(),key.getBytes(),iv.getBytes());
            result.setData(Base64.getEncoder().encodeToString(js));
        }catch (Exception e){
            log.error("加密失败："+e);
        }
        return result;
    }


}
