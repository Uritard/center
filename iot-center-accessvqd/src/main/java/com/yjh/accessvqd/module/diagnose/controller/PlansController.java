package com.yjh.accessvqd.module.diagnose.controller;


import com.yjh.accessvqd.commons.result.Result;
import com.yjh.accessvqd.module.diagnose.service.PlansService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author czh
 * @since 2021-01-14
 */
@RestController
@RequestMapping("/plansOperate/v1")
@Api(value = "/plansOperate", description = "诊断任务操作")
public class PlansController {
    private Logger log = LoggerFactory.getLogger(PlansController.class);

    @Autowired
    private PlansService plansService;


}
