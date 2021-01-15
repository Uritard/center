package com.yjh.accessvqd.module.diagnose.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;

/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/chenResultOperate/v1")
@Api(value = "/chenResultOperate", description = "诊断结果操作")
public class ChenResultController {
    private Logger log = LoggerFactory.getLogger(ChenResultController.class);


    @Autowired
    private ChanResultService chanResultService;

}
