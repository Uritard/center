package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.controller.TStdMetemodelController;
import com.yjh.platform.module.user.service.TemplateToImportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import static com.yjh.platform.common.utils.ExcelFormatUtil.export;

/**
 * @author YC
 * @date 2020/9/1 - 13:46
 */
@RestController
@RequestMapping("/template/v1")
@Api(value = "/template", description = "模板操作接口")
public class TemplateDownloadController {

    @Autowired
    private final TemplateToImportService templateToImportService;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelController.class);

    public TemplateDownloadController(TemplateToImportService templateToImportService) {
        this.templateToImportService = templateToImportService;
    }

    @ApiOperation(value = "模板生成", notes = "生成")
    @RequestMapping(value = "/templateGenerate", method = RequestMethod.POST)
    public Result templateGenerate(@RequestParam("modelName") String modelName) {
        Result result = new Result();
        try {
            String titleName = modelName;
            String[] title = {"姓名","性别","年龄"};
            String[] example = {"123","456","789"};
            templateDownload(export(title,titleName,example),"用户表.xls");
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导出失败：" + e);
        }
        return result;
        }
    @ApiOperation(value = "模板下载", notes = "模板下载")
    @RequestMapping(value = "/templateDownload", method = RequestMethod.POST)
    public ResponseEntity<byte[]> templateDownload(InputStream is, String name) throws Exception {
        System.out.println("开始下载文件");

        if (this.log.isDebugEnabled())
            this.log.debug("download: " + name);

        HttpHeaders header = new HttpHeaders();
        String fileSuffix = name.substring(name.lastIndexOf('.') + 1);
        fileSuffix = fileSuffix.toLowerCase();

        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");//xlsx对应的Content-type类型
        arguments.put("xls", "application/vnd.ms-excel");//xls对应的Content-type类型

        String contentType = arguments.get(fileSuffix);
        header.add("Content-Type", (StringUtils.hasText(contentType) ? contentType : "application/x-download"));

        if(is!=null && is.available()!=0){
            header.add("Content-Length", String.valueOf(is.available()));
            header.add("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(name, "UTF-8"));
            byte[] bs = IOUtils.toByteArray(is);

            System.out.println("结束下载文件-有记录");
            System.out.println("结束导出excel");
            return new ResponseEntity<>(bs, header, HttpStatus.OK);

        }else{

            String string="数据为空";
            header.add("Content-Length", "0");
            header.add("Content-Disposition", "attachment;filename*=utf-8'zh_cn'" + URLEncoder.encode(name, "UTF-8"));
            System.out.println("结束下载文件-无记录");
            System.out.println("结束导出excel");
            System.out.println("结果是："+new ResponseEntity<>(string.getBytes(), header, HttpStatus.OK));
            return new ResponseEntity<>(string.getBytes(), header, HttpStatus.OK);

        }
    }

}
