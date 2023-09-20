package com.yjh.platform.module.patrol.controller;

import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.patrol.entity.interlanalysis.Response;
import com.yjh.platform.module.patrol.service.DataMigrationService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * @Author: lqh
 * @Date: 2023/09/19
 */
@RestController
@RequestMapping("/migration/v1")
@Api(value = "/migration", tags = "数据迁移接口")
public class DataMigrationController {

    @Autowired
    private DataMigrationService dataMigrationService;

    @PostMapping(value = "/task-migration")
    public void taskMigration() {
        dataMigrationService.taskDataMigration();
    }
}
