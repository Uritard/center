package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.ExcelTemplate;
import com.yjh.platform.module.device.controller.TStdMetemodelController;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.user.entity.*;
import com.yjh.platform.module.user.service.TemplateToImportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author YC
 * @date 2020/8/26 - 10:42
 */
@RestController
@RequestMapping("/tStdMeteModel/v1")
@Api(value = "/templateToImport", description = "模板导入操作接口")
public class TemplateToImportController {

    @Autowired
    private final TemplateToImportService templateToImportService;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelController.class);

    public TemplateToImportController(TemplateToImportService templateToImportService) {
        this.templateToImportService = templateToImportService;
    }

    @ApiOperation(value = "测点模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelMete", method = RequestMethod.POST)
    public Result importExcelMete(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始
            List<TStdMeteModel> tStdMeteModelList = new ArrayList<>();
            List<TStdMeteModelDetail> tStdMeteModelDetailList = new ArrayList<>();
            StringBuffer errMsg = new StringBuffer();
            //测点模板
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);
                //创建系统测点模板表
                TStdMeteModel tStdMeteModel = new TStdMeteModel();

                if (row.getCell(1) == null || row.getCell(1).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(1).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(1).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                    tStdMeteModel.setModelName(row.getCell(1).getStringCellValue());//第2列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(2) == null || row.getCell(2).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(2).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(2).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                if (row.getCell(4) == null || row.getCell(4).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(4).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(4).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tStdMeteModel.setDeviceType
                            (new Double(row.getCell(4).getNumericCellValue()).intValue());//第5列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                String remark = null;
                if (row.getCell(28) != null && row.getCell(28).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AC列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    remark = (row.getCell(28).getStringCellValue() == null || "".equals(row.getCell(28).getStringCellValue())) ? remark : row.getCell(28).getStringCellValue();
                    tStdMeteModel.setRemark(remark);//第29列
                }
                tStdMeteModelList.add(tStdMeteModel);
            }

            int sizeNum = tStdMeteModelList.size();
            //获取模板表的最后一条modelId
            Long lastModelId = templateToImportService.selectLastModelId();
            //测点模板详细
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);
                //创建系统测点模板详细表
                TStdMeteModelDetail tStdMeteModelDetail = new TStdMeteModelDetail();

                if (lastModelId != null) {
                    tStdMeteModelDetail.setModelId(lastModelId - 1 + i);
                } else {
                    long lastModelId2 = -1;
                    tStdMeteModelDetail.setModelId(lastModelId2 + i);
                }

                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tStdMeteModelDetail.setMeteId
                            (new Double(row.getCell(2).getNumericCellValue()).longValue());//第3列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                Integer customType = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType = new Double(row.getCell(3).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setCustomType(customType);//第4列
                } else {
                    tStdMeteModelDetail.setCustomType(0);
                }

                String meteCode = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteCode = (row.getCell(5).getStringCellValue() == null || "".equals(row.getCell(5).getStringCellValue())) ? meteCode : row.getCell(5).getStringCellValue();
                    tStdMeteModelDetail.setMeteCode(meteCode);//第6列
                }

                String meteName = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteName = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? meteName : row.getCell(6).getStringCellValue();
                    tStdMeteModelDetail.setMeteName(meteName);//第7列
                }

                String meteType = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteType = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? meteType : row.getCell(7).getStringCellValue();
                    tStdMeteModelDetail.setMeteType(meteType);//第8列
                }

                String unit = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    unit = (row.getCell(8).getStringCellValue() == null || "".equals(row.getCell(8).getStringCellValue())) ? unit : row.getCell(8).getStringCellValue();
                    tStdMeteModelDetail.setUnit(unit);//第9列
                }

                String alarmNote = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmNote = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? alarmNote : row.getCell(9).getStringCellValue();
                    tStdMeteModelDetail.setAlarmNote(alarmNote);//第10列
                }

                String alarmExplain = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmExplain = (row.getCell(10).getStringCellValue() == null || "".equals(row.getCell(10).getStringCellValue())) ? alarmExplain : row.getCell(10).getStringCellValue();
                    tStdMeteModelDetail.setAlarmExplain(alarmExplain);//第11列
                }

                String alarmType = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmType = (row.getCell(11).getStringCellValue() == null || "".equals(row.getCell(11).getStringCellValue())) ? alarmType : row.getCell(11).getStringCellValue();
                    tStdMeteModelDetail.setAlarmType(alarmType);//第12列
                }

                Float upEffect = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upEffect = (float) row.getCell(12).getNumericCellValue();
                    tStdMeteModelDetail.setUpEffect(upEffect);//第13列
                }

                Float lowEffect = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowEffect = (float) row.getCell(13).getNumericCellValue();
                    tStdMeteModelDetail.setLowEffect(lowEffect);//第14列
                }

                Integer alarmLevel = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(14).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmLevel = new Double(row.getCell(14).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmLevel(alarmLevel);//第15列
                }

                Float highLimit1 = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit1 = (float) row.getCell(15).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit1(highLimit1);//第16列
                }

                Float lowLimit1 = null;
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit1 = (float) row.getCell(16).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit1(lowLimit1);//第17列
                }

                Float highLimit2 = null;
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(17).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit2 = (float) row.getCell(17).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit2(highLimit2);//第18列
                }

                Float lowLimit2 = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit2 = (float) row.getCell(18).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit2(lowLimit2);//第19列
                }
                Float highLimit3 = null;
                if (row.getCell(19) != null && row.getCell(17).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(19).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit3 = (float) row.getCell(19).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit3(highLimit3);//第20列
                }

                Float lowLimit3 = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit3 = (float) row.getCell(20).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit3(lowLimit3);//第21列
                }
                Float highLimit4 = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit4 = (float) row.getCell(21).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit4(highLimit4);//第22列
                }

                Float lowLimit4 = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(22).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit4 = (float) row.getCell(22).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit4(lowLimit4);//第23列
                }
                Integer alarmDelay = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmDelay = new Double(row.getCell(23).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmDelay(alarmDelay);//第24列
                }

                Integer alarmCnt = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmCnt = new Double(row.getCell(24).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmCnt(alarmCnt);//第25列
                }

                BigDecimal thresholdAbs = null;
                if (row.getCell(21) != null && row.getCell(25).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(25).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdAbs = BigDecimal.valueOf(row.getCell(25).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdAbs(thresholdAbs);//第26列
                }

                BigDecimal thresholdPer = null;
                if (row.getCell(26) != null && row.getCell(26).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AA列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(26).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdPer = BigDecimal.valueOf(row.getCell(26).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdPer(thresholdPer);//第27列
                }

                Integer modulus = null;
                if (row.getCell(27) != null && row.getCell(27).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(27).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    modulus = new Double(row.getCell(27).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setModulus(modulus);//第28列
                }
                tStdMeteModelDetailList.add(tStdMeteModelDetail);
            }

            int sizeNum2 = tStdMeteModelDetailList.size();

            int resultNum = 0;
            if (sizeNum == total - 1 && sizeNum2 == total - 1) {
                resultNum = templateToImportService.batchUpdateTStdMeteModel(tStdMeteModelList);
                int resultNum2 = templateToImportService.batchUpdateTStdMeteModelDetail(tStdMeteModelDetailList);
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "设备模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelDevice", method = RequestMethod.POST)
    public Result importExcelDevice(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始
            List<TStdDeviceAttr> tStdDeviceAttrList = new ArrayList<>();
            List<TStdDevice> tStdDeviceList = new ArrayList<>();
            StringBuffer errMsg = new StringBuffer();
            //标准化设备
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建标准化设备
                TStdDevice tStdDevice = new TStdDevice();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(0).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(0).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(0) != null && row.getCell(0).getCellTypeEnum().equals(CellType.STRING)) {
                    tStdDevice.setCustomId(row.getCell(0).getStringCellValue());//第1列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                String deviceCode = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    deviceCode = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? deviceCode : row.getCell(1).getStringCellValue();
                    tStdDevice.setDeviceCode(deviceCode);//第2列
                }

                String deviceName = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    deviceName = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? deviceName : row.getCell(2).getStringCellValue();
                    tStdDevice.setDeviceName(deviceName);//第3列
                }

                String aliasName = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    aliasName = (row.getCell(3).getStringCellValue() == null || "".equals(row.getCell(3).getStringCellValue())) ? aliasName : row.getCell(3).getStringCellValue();
                    tStdDevice.setAliasName(aliasName);//第4列
                }
                Integer deviceType = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    deviceType = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    tStdDevice.setDeviceType(deviceType);//第5列
                }
                String positionType = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    positionType = (row.getCell(5).getStringCellValue() == null || "".equals(row.getCell(5).getStringCellValue())) ? positionType : row.getCell(5).getStringCellValue();
                    tStdDevice.setPositionType(positionType);//第6列
                }
                Long modelId = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    modelId = new Double(row.getCell(4).getNumericCellValue()).longValue();
                    tStdDevice.setModelId(modelId);//第7列
                }
                String regionPath = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    regionPath = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? regionPath : row.getCell(7).getStringCellValue();
                    tStdDevice.setRegionPath(regionPath);//第8列
                }
                Long upRegionId = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upRegionId = new Double(row.getCell(8).getNumericCellValue()).longValue();
                    tStdDevice.setUpRegionId(upRegionId);//第9列
                }
                String upRegionName = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    upRegionName = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? upRegionName : row.getCell(9).getStringCellValue();
                    tStdDevice.setUpRegionName(upRegionName);//第10列
                }
                String customName = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    customName = (row.getCell(10).getStringCellValue() == null || "".equals(row.getCell(10).getStringCellValue())) ? customName : row.getCell(10).getStringCellValue();
                    tStdDevice.setCustomName(customName);//第11列
                }
                Integer customType = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType = new Double(row.getCell(11).getNumericCellValue()).intValue();
                    tStdDevice.setCustomType(customType);//第12列
                }
                Integer status = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    status = new Double(row.getCell(12).getNumericCellValue()).intValue();
                    tStdDevice.setStatus(status);//第13列
                }
                Date updateTime = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    updateTime = (row.getCell(13).getDateCellValue() == null || "".equals(row.getCell(13).getDateCellValue())) ? updateTime : row.getCell(13).getDateCellValue();
                    tStdDevice.setUpdateTime(updateTime);//第14列
                }
                Date createTime = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(14).getDateCellValue() == null || "".equals(row.getCell(14).getDateCellValue())) ? createTime : row.getCell(14).getDateCellValue();
                    tStdDevice.setCreateTime(createTime);//第15列
                }
                tStdDeviceList.add(tStdDevice);
            }


            int sizeNum = tStdDeviceList.size();
            //获取标准化设备表的最后一条DeviceId
            Long lastDeviceId = templateToImportService.selectLastDeviceId();
            //标准化设备参数
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建标准化设备参数
                TStdDeviceAttr tStdDeviceAttr = new TStdDeviceAttr();

                if (lastDeviceId != null) {
                    tStdDeviceAttr.setDeviceId(lastDeviceId - 1 + i);
                } else {
                    long lastDeviceId2 = -1;
                    tStdDeviceAttr.setDeviceId(lastDeviceId2 + i);
                }

                Integer deviceModel = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    deviceModel = new Double(row.getCell(15).getNumericCellValue()).intValue();
                    tStdDeviceAttr.setDeviceModel(deviceModel);//第16列
                }

                String pmsId = null;
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    pmsId = (row.getCell(16).getStringCellValue() == null || "".equals(row.getCell(16).getStringCellValue())) ? pmsId : row.getCell(16).getStringCellValue();
                    tStdDeviceAttr.setPmsId(pmsId);//第17列
                }

                String pmsType = null;
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    pmsType = (row.getCell(17).getStringCellValue() == null || "".equals(row.getCell(17).getStringCellValue())) ? pmsType : row.getCell(17).getStringCellValue();
                    tStdDeviceAttr.setPmsType(pmsType);//第18列
                }

                String manufacturer = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    manufacturer = (row.getCell(18).getStringCellValue() == null || "".equals(row.getCell(18).getStringCellValue())) ? manufacturer : row.getCell(18).getStringCellValue();
                    tStdDeviceAttr.setManufacturer(manufacturer);//第19列
                }
                Date productionDate = null;
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    productionDate = (row.getCell(19).getDateCellValue() == null || "".equals(row.getCell(19).getDateCellValue())) ? productionDate : row.getCell(19).getDateCellValue();
                    tStdDeviceAttr.setProductionDate(productionDate);//第20列
                }
                Date openingDate = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    openingDate = (row.getCell(20).getDateCellValue() == null || "".equals(row.getCell(20).getDateCellValue())) ? openingDate : row.getCell(20).getDateCellValue();
                    tStdDeviceAttr.setOpeningDate(openingDate);//第21列
                }
                Date disableDate = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    disableDate = (row.getCell(21).getDateCellValue() == null || "".equals(row.getCell(21).getDateCellValue())) ? disableDate : row.getCell(21).getDateCellValue();
                    tStdDeviceAttr.setDisableDate(disableDate);//第22列
                }
                Date lastMaintenance = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    lastMaintenance = (row.getCell(22).getDateCellValue() == null || "".equals(row.getCell(22).getDateCellValue())) ? lastMaintenance : row.getCell(22).getDateCellValue();
                    tStdDeviceAttr.setLastMaintenance(lastMaintenance);//第23列
                }
                String maintenanceCount = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    maintenanceCount = (row.getCell(23).getStringCellValue() == null || "".equals(row.getCell(23).getStringCellValue())) ? maintenanceCount : row.getCell(23).getStringCellValue();
                    tStdDeviceAttr.setMaintenanceCount(maintenanceCount);//第24列
                }
                String organization = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    organization = (row.getCell(24).getStringCellValue() == null || "".equals(row.getCell(24).getStringCellValue())) ? organization : row.getCell(24).getStringCellValue();
                    tStdDeviceAttr.setOrganization(organization);//第25列
                }
                String department = null;
                if (row.getCell(25) != null && row.getCell(25).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    department = (row.getCell(25).getStringCellValue() == null || "".equals(row.getCell(25).getStringCellValue())) ? department : row.getCell(25).getStringCellValue();
                    tStdDeviceAttr.setDepartment(department);//第26列
                }
                String responsiblePerson = null;
                if (row.getCell(26) != null && row.getCell(26).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AA列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    responsiblePerson = (row.getCell(26).getStringCellValue() == null || "".equals(row.getCell(26).getStringCellValue())) ? responsiblePerson : row.getCell(26).getStringCellValue();
                    tStdDeviceAttr.setResponsiblePerson(responsiblePerson);//第27列
                }
                String latitude = null;
                if (row.getCell(27) != null && row.getCell(27).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    latitude = (row.getCell(27).getStringCellValue() == null || "".equals(row.getCell(27).getStringCellValue())) ? latitude : row.getCell(27).getStringCellValue();
                    tStdDeviceAttr.setLatitude(latitude);//第28列
                }
                String longitude = null;
                if (row.getCell(28) != null && row.getCell(28).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AC列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    longitude = (row.getCell(28).getStringCellValue() == null || "".equals(row.getCell(28).getStringCellValue())) ? longitude : row.getCell(28).getStringCellValue();
                    tStdDeviceAttr.setLongitude(longitude);//第29列
                }
                String ip = null;
                if (row.getCell(29) != null && row.getCell(29).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AD列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    ip = (row.getCell(29).getStringCellValue() == null || "".equals(row.getCell(29).getStringCellValue())) ? ip : row.getCell(29).getStringCellValue();
                    tStdDeviceAttr.setIp(ip);//第30列
                }
                Integer port = null;
                if (row.getCell(30) != null && row.getCell(30).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    port = new Double(row.getCell(30).getNumericCellValue()).intValue();
                    tStdDeviceAttr.setPort(port);//第30列
                }
                String para1 = null;
                if (row.getCell(31) != null && row.getCell(31).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AF列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    para1 = (row.getCell(31).getStringCellValue() == null || "".equals(row.getCell(31).getStringCellValue())) ? para1 : row.getCell(31).getStringCellValue();
                    tStdDeviceAttr.setPara1(para1);//第32列
                }
                String para2 = null;
                if (row.getCell(32) != null && row.getCell(32).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AG列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    para2 = (row.getCell(32).getStringCellValue() == null || "".equals(row.getCell(32).getStringCellValue())) ? para2 : row.getCell(32).getStringCellValue();
                    tStdDeviceAttr.setPara2(para2);//第33列
                }
                String para3 = null;
                if (row.getCell(33) != null && row.getCell(33).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AH列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    para3 = (row.getCell(33).getStringCellValue() == null || "".equals(row.getCell(33).getStringCellValue())) ? para3 : row.getCell(33).getStringCellValue();
                    tStdDeviceAttr.setPara3(para3);//第34列
                }
                tStdDeviceAttrList.add(tStdDeviceAttr);
            }

            int sizeNum2 = tStdDeviceAttrList.size();

            int resultNum = 0;
            if (sizeNum == total - 1 && sizeNum2 == total - 1) {
                resultNum = templateToImportService.batchUpdateTStdDeviceAttr(tStdDeviceAttrList);
                int resultNum2 = templateToImportService.batchUpdateTStdDevice(tStdDeviceList);
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "设备测点模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelDeviceMete", method = RequestMethod.POST)
    public Result importExcelDeviceMete(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TStdDevice> tStdDeviceList = new ArrayList<>();
            List<TStdMeteModelDetail> tStdMeteModelDetailList = new ArrayList<>();
            List<TStdDeviceMete> tStdDeviceMeteList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //标准化设备
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建标准化设备
                TStdDevice tStdDevice = new TStdDevice();

                String deviceCode = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    deviceCode = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? deviceCode : row.getCell(1).getStringCellValue();
                    tStdDevice.setDeviceCode(deviceCode);//第2列
                }
                String deviceName = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    deviceName = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? deviceName : row.getCell(2).getStringCellValue();
                    tStdDevice.setDeviceName(deviceName);//第3列
                }

                String aliasName = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    aliasName = (row.getCell(3).getStringCellValue() == null || "".equals(row.getCell(3).getStringCellValue())) ? aliasName : row.getCell(3).getStringCellValue();
                    tStdDevice.setAliasName(aliasName);//第4列
                }
                Integer deviceType = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    deviceType = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    tStdDevice.setDeviceType(deviceType);//第5列
                }
                String positionType = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    positionType = (row.getCell(5).getStringCellValue() == null || "".equals(row.getCell(5).getStringCellValue())) ? positionType : row.getCell(5).getStringCellValue();
                    tStdDevice.setPositionType(positionType);//第6列
                }

                Long modelId = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    modelId = new Double(row.getCell(6).getNumericCellValue()).longValue();
                    tStdDevice.setModelId(modelId);//第7列
                }
                String regionPath = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    regionPath = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? regionPath : row.getCell(7).getStringCellValue();
                    tStdDevice.setRegionPath(regionPath);//第8列
                }
                Long upRegionId = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upRegionId = new Double(row.getCell(8).getNumericCellValue()).longValue();
                    tStdDevice.setUpRegionId(upRegionId);//第9列
                }
                String upRegionName = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    upRegionName = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? upRegionName : row.getCell(9).getStringCellValue();
                    tStdDevice.setUpRegionName(upRegionName);//第10列
                }
                Integer status = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "k列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    status = new Double(row.getCell(10).getNumericCellValue()).intValue();
                    tStdDevice.setStatus(status);//第11列
                }
                if (row.getCell(11) == null || row.getCell(11).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(11).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(11).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.STRING)) {
                    tStdDevice.setCustomId(row.getCell(11).getStringCellValue());//第12列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String customName = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    customName = (row.getCell(12).getStringCellValue() == null || "".equals(row.getCell(12).getStringCellValue())) ? customName : row.getCell(12).getStringCellValue();
                    tStdDevice.setCustomName(customName);//第13列
                }
                Integer customType = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType = new Double(row.getCell(13).getNumericCellValue()).intValue();
                    tStdDevice.setCustomType(customType);//第14列
                } else {
                    tStdDevice.setCustomType(0);
                }
                Date updateTime = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    updateTime = (row.getCell(14).getDateCellValue() == null || "".equals(row.getCell(14).getDateCellValue())) ? updateTime : row.getCell(14).getDateCellValue();
                    tStdDevice.setUpdateTime(updateTime);//第15列
                }
                Date createTime = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(15).getDateCellValue() == null || "".equals(row.getCell(15).getDateCellValue())) ? createTime : row.getCell(15).getDateCellValue();
                    tStdDevice.setCreateTime(createTime);//第16列
                }
                tStdDeviceList.add(tStdDevice);
            }

            //系统测点模版详细
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建系统测点模版详细
                TStdMeteModelDetail tStdMeteModelDetail = new TStdMeteModelDetail();

                tStdMeteModelDetail.setModelId(new Double(row.getCell(6).getNumericCellValue()).longValue());//第7列

                if (row.getCell(16) == null || row.getCell(16).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(16).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(16).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tStdMeteModelDetail.setMeteId(new Double(row.getCell(16).getNumericCellValue()).longValue());//第17列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                Integer customType = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType = new Double(row.getCell(13).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setCustomType(customType);//第14列
                } else {
                    tStdMeteModelDetail.setCustomType(0);
                }
                String meteType = null;
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteType = (row.getCell(17).getStringCellValue() == null || "".equals(row.getCell(17).getStringCellValue())) ? meteType : row.getCell(17).getStringCellValue();
                    tStdMeteModelDetail.setMeteType(meteType);//第18列
                }
                String meteName = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteName = (row.getCell(18).getStringCellValue() == null || "".equals(row.getCell(18).getStringCellValue())) ? meteName : row.getCell(18).getStringCellValue();
                    tStdMeteModelDetail.setMeteName(meteName);//第19列
                }
                String meteCode = null;
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteCode = (row.getCell(19).getStringCellValue() == null || "".equals(row.getCell(19).getStringCellValue())) ? meteCode : row.getCell(19).getStringCellValue();
                    tStdMeteModelDetail.setMeteCode(meteCode);//第20列
                }
                String unit = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    unit = (row.getCell(20).getStringCellValue() == null || "".equals(row.getCell(20).getStringCellValue())) ? unit : row.getCell(20).getStringCellValue();
                    tStdMeteModelDetail.setUnit(unit);//第21列
                }
                String alarmNote = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmNote = (row.getCell(21).getStringCellValue() == null || "".equals(row.getCell(21).getStringCellValue())) ? alarmNote : row.getCell(21).getStringCellValue();
                    tStdMeteModelDetail.setAlarmNote(alarmNote);//第22列
                }
                String alarmExplain = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmExplain = (row.getCell(22).getStringCellValue() == null || "".equals(row.getCell(22).getStringCellValue())) ? alarmExplain : row.getCell(22).getStringCellValue();
                    tStdMeteModelDetail.setAlarmExplain(alarmExplain);//第23列
                }
                String alarmType = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    alarmType = (row.getCell(23).getStringCellValue() == null || "".equals(row.getCell(23).getStringCellValue())) ? alarmType : row.getCell(23).getStringCellValue();
                    tStdMeteModelDetail.setAlarmType(alarmType);//第24列
                }
                Float upEffect = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upEffect = (float) row.getCell(24).getNumericCellValue();
                    tStdMeteModelDetail.setUpEffect(upEffect);//第25列
                }

                Float lowEffect = null;
                if (row.getCell(25) != null && row.getCell(25).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(25).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowEffect = (float) row.getCell(25).getNumericCellValue();
                    tStdMeteModelDetail.setLowEffect(lowEffect);//第26列
                }

                Integer alarmLevel = null;
                if (row.getCell(26) != null && row.getCell(26).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AA列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(26).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmLevel = new Double(row.getCell(26).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmLevel(alarmLevel);//第27列
                }

                Float highLimit1 = null;
                if (row.getCell(27) != null && row.getCell(27).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(27).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit1 = (float) row.getCell(27).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit1(highLimit1);//第28列
                }

                Float lowLimit1 = null;
                if (row.getCell(28) != null && row.getCell(28).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AC列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(28).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit1 = (float) row.getCell(28).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit1(lowLimit1);//第29列
                }

                Float highLimit2 = null;
                if (row.getCell(29) != null && row.getCell(29).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AD列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(29).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit2 = (float) row.getCell(29).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit2(highLimit2);//第30列
                }

                Float lowLimit2 = null;
                if (row.getCell(30) != null && row.getCell(30).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AE列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(30).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit2 = (float) row.getCell(30).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit2(lowLimit2);//第31列
                }
                Float highLimit3 = null;
                if (row.getCell(31) != null && row.getCell(31).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AF列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(31).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit3 = (float) row.getCell(31).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit3(highLimit3);//第32列
                }

                Float lowLimit3 = null;
                if (row.getCell(32) != null && row.getCell(32).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AG列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(32).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit3 = (float) row.getCell(32).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit3(lowLimit3);//第33列
                }
                Float highLimit4 = null;
                if (row.getCell(33) != null && row.getCell(33).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AH列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(33).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit4 = (float) row.getCell(33).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit4(highLimit4);//第34列
                }

                Float lowLimit4 = null;
                if (row.getCell(34) != null && row.getCell(34).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AI列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(34).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit4 = (float) row.getCell(34).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit4(lowLimit4);//第35列
                }
                Integer alarmDelay = null;
                if (row.getCell(35) != null && row.getCell(35).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AJ列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(35).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmDelay = new Double(row.getCell(35).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmDelay(alarmDelay);//第36列
                }

                Integer alarmCnt = null;
                if (row.getCell(24) != null && row.getCell(36).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AK列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(36).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmCnt = new Double(row.getCell(36).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmCnt(alarmCnt);//第37列
                }

                BigDecimal thresholdAbs = null;
                if (row.getCell(37) != null && row.getCell(37).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AL列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(37).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdAbs = BigDecimal.valueOf(row.getCell(37).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdAbs(thresholdAbs);//第38列
                }

                BigDecimal thresholdPer = null;
                if (row.getCell(38) != null && row.getCell(38).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AM列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(38).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdPer = BigDecimal.valueOf(row.getCell(38).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdPer(thresholdPer);//第39列
                }

                Integer modulus = null;
                if (row.getCell(39) != null && row.getCell(39).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AN列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(39).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    modulus = new Double(row.getCell(39).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setModulus(modulus);//第40列
                }
                tStdMeteModelDetailList.add(tStdMeteModelDetail);
            }

            //获取标准化设备表的最后一条deviceId
            Long lastModelId = templateToImportService.selectLastDeviceId();

            //标准设备测点
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建标准设备测点
                TStdDeviceMete tStdDeviceMete = new TStdDeviceMete();

                if (lastModelId != null) {
                    tStdDeviceMete.setDeviceId(lastModelId - 1 + i);
                } else {
                    long lastDeviceId2 = -1;
                    tStdDeviceMete.setDeviceId(lastDeviceId2 + i);
                }

                tStdDeviceMete.setCustomId(row.getCell(11).getStringCellValue());
                tStdDeviceMete.setMeteId(new Double(row.getCell(16).getNumericCellValue()).longValue());
                tStdDeviceMete.setMeteType(row.getCell(17).getStringCellValue());
                tStdDeviceMete.setMeteName(row.getCell(18).getStringCellValue());
                tStdDeviceMete.setDeviceType(new Double(row.getCell(4).getNumericCellValue()).intValue());
                Integer customType = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType = new Double(row.getCell(13).getNumericCellValue()).intValue();
                    tStdDeviceMete.setCustomType(customType);
                } else {
                    tStdDeviceMete.setCustomType(0);
                }
                tStdDeviceMete.setPositionType(row.getCell(5).getStringCellValue());
                tStdDeviceMete.setUnit(row.getCell(20).getStringCellValue());
                tStdDeviceMete.setAlarmNote(row.getCell(21).getStringCellValue());
                tStdDeviceMete.setAlarmType(row.getCell(23).getStringCellValue());
                tStdDeviceMete.setUpEffect((float) row.getCell(24).getNumericCellValue());
                tStdDeviceMete.setLowEffect((float) row.getCell(25).getNumericCellValue());
                tStdDeviceMete.setAlarmLevel(new Double(row.getCell(26).getNumericCellValue()).intValue());
                tStdDeviceMete.setHighLimit1((float) row.getCell(27).getNumericCellValue());
                tStdDeviceMete.setLowLimit1((float) row.getCell(28).getNumericCellValue());
                tStdDeviceMete.setHighLimit2((float) row.getCell(29).getNumericCellValue());
                tStdDeviceMete.setLowLimit2((float) row.getCell(30).getNumericCellValue());
                tStdDeviceMete.setHighLimit3((float) row.getCell(31).getNumericCellValue());
                tStdDeviceMete.setLowLimit3((float) row.getCell(32).getNumericCellValue());
                tStdDeviceMete.setHighLimit4((float) row.getCell(33).getNumericCellValue());
                tStdDeviceMete.setLowLimit4((float) row.getCell(34).getNumericCellValue());
                tStdDeviceMete.setAlarmDelay(new Double(row.getCell(35).getNumericCellValue()).intValue());
                tStdDeviceMete.setAlarmCnt(new Double(row.getCell(36).getNumericCellValue()).intValue());
                tStdDeviceMete.setThresholdAbs(BigDecimal.valueOf(row.getCell(37).getNumericCellValue()));
                tStdDeviceMete.setThresholdPer(BigDecimal.valueOf(row.getCell(38).getNumericCellValue()));
                tStdDeviceMete.setModulus(new Double(row.getCell(39).getNumericCellValue()).intValue());

                String remark = null;
                if (row.getCell(40) != null && row.getCell(40).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    remark = (row.getCell(40).getStringCellValue() == null || "".equals(row.getCell(40).getStringCellValue())) ? remark : row.getCell(40).getStringCellValue();
                    tStdDeviceMete.setRemark(remark);//第41列
                }
                tStdDeviceMeteList.add(tStdDeviceMete);
            }

            int sizeNum1 = tStdDeviceList.size();
            int sizeNum2 = tStdDeviceMeteList.size();
            int sizeNum3 = tStdMeteModelDetailList.size();

            int resultNum = 0;
            if (sizeNum1 == total - 1 && sizeNum2 == total - 1 && sizeNum3 == total - 1) {
                resultNum = templateToImportService.batchUpdateTStdDevice(tStdDeviceList);
                if (resultNum == sizeNum1) {
                    int resultNum2 = templateToImportService.batchUpdateTStdMeteModelDetail(tStdMeteModelDetailList);
                    if (resultNum2 == resultNum) {
                        int resultNum3 = templateToImportService.batchUpdateTStdDeviceMete(tStdDeviceMeteList);
                    }
                }
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "组织数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelOrg", method = RequestMethod.POST)
    public Result importExcelOrg(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<SysOrg> sysOrgList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //组织机构
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建组织机构
                SysOrg sysOrg = new SysOrg();

                String orgName = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    orgName = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? orgName : row.getCell(1).getStringCellValue();
                    sysOrg.setOrgName(orgName);//第2列
                }
                String orgCode = null;
                if (row.getCell(2) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    orgCode = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? orgCode : row.getCell(2).getStringCellValue();
                    sysOrg.setOrgCode(orgCode);//第3列
                }
                Long upId = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upId = new Double(row.getCell(3).getNumericCellValue()).longValue();
                    sysOrg.setUpId(upId);//第4列
                }
                Integer sort = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    sort = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    sysOrg.setSort(sort);//第5列
                }
                Date createTime = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(5).getDateCellValue() == null || "".equals(row.getCell(5).getDateCellValue())) ? createTime : row.getCell(5).getDateCellValue();
                    sysOrg.setCreateTime(createTime);//第6列
                }
                Long creatorId = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    creatorId = new Double(row.getCell(6).getNumericCellValue()).longValue();
                    sysOrg.setCreatorId(creatorId);//第7列
                }
                Integer orgLevel = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    orgLevel = new Double(row.getCell(7).getNumericCellValue()).intValue();
                    sysOrg.setOrgLevel(orgLevel);//第8列
                }
                String orgPath = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    orgPath = (row.getCell(8).getStringCellValue() == null || "".equals(row.getCell(8).getStringCellValue())) ? orgPath : row.getCell(8).getStringCellValue();
                    sysOrg.setOrgPath(orgPath);//第9列
                }
                String deptName = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    deptName = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? deptName : row.getCell(9).getStringCellValue();
                    sysOrg.setDeptName(deptName);//第10列
                }
                sysOrgList.add(sysOrg);
            }
            int resultNum = templateToImportService.batchUpdateSysOrg(sysOrgList);
            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "用户数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelUserInfo", method = RequestMethod.POST)
    public Result importExcelUserInfo(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<SysUser> sysUserList = new ArrayList<>();
            List<SysUserBackUp> sysUserBackupList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //系统用户
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建系统用户
                SysUser sysUser = new SysUser();

                String userName = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    userName = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? userName : row.getCell(1).getStringCellValue();
                    sysUser.setUserName(userName);//第2列
                }

                String password = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    password = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? password : row.getCell(2).getStringCellValue();
                    sysUser.setPassword(password);//第3列
                }
                String trueName = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    trueName = (row.getCell(3).getStringCellValue() == null || "".equals(row.getCell(3).getStringCellValue())) ? trueName : row.getCell(3).getStringCellValue();
                    sysUser.setTrueName(trueName);//第4列
                }
                Integer userType = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    userType = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    sysUser.setUserType(userType);//第5列
                }
                Integer sex = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    sex = new Double(row.getCell(5).getNumericCellValue()).intValue();
                    sysUser.setSex(sex);//第6列
                }
                String eMail = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    eMail = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? eMail : row.getCell(6).getStringCellValue();
                    sysUser.setEMail(eMail);//第7列
                }
                String mobilePhone = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    mobilePhone = String.valueOf(row.getCell(7).getNumericCellValue());
                    sysUser.setMobilePhone(mobilePhone);//第8列
                }
                String workNo = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    workNo = (row.getCell(8).getStringCellValue() == null || "".equals(row.getCell(8).getStringCellValue())) ? workNo : row.getCell(8).getStringCellValue();
                    sysUser.setWorkNo(workNo);//第9列
                }
                String faceId = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    faceId = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? faceId : row.getCell(9).getStringCellValue();
                    sysUser.setFaceId(faceId);//第10列
                }
                String fingerId = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    fingerId = (row.getCell(10).getStringCellValue() == null || "".equals(row.getCell(10).getStringCellValue())) ? fingerId : row.getCell(10).getStringCellValue();
                    sysUser.setFingerId(fingerId);//第11列
                }
                String voiceId = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    voiceId = (row.getCell(11).getStringCellValue() == null || "".equals(row.getCell(11).getStringCellValue())) ? voiceId : row.getCell(11).getStringCellValue();
                    sysUser.setVoiceId(voiceId);//第12列
                }
                Integer state = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    state = new Double(row.getCell(12).getNumericCellValue()).intValue();
                    sysUser.setState(state);//第13列
                }
                String userTitle = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    userTitle = (row.getCell(13).getStringCellValue() == null || "".equals(row.getCell(13).getStringCellValue())) ? userTitle : row.getCell(13).getStringCellValue();
                    sysUser.setUserTitle(userTitle);//第14列
                }
                Long creatorId = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    creatorId = new Double(row.getCell(15).getNumericCellValue()).longValue();
                    sysUser.setCreatorId(creatorId);//第16列
                }
                String imageUrl = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    imageUrl = (row.getCell(18).getStringCellValue() == null || "".equals(row.getCell(18).getStringCellValue())) ? imageUrl : row.getCell(18).getStringCellValue();
                    sysUser.setImageUrl(imageUrl);//第19列
                }
                Long roleId = null;
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(19).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    roleId = new Double(row.getCell(19).getNumericCellValue()).longValue();
                    sysUser.setRoleId(roleId);//第20列
                }
                Long orgId = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    orgId = new Double(row.getCell(20).getNumericCellValue()).longValue();
                    sysUser.setOrgId(orgId);//第21列
                }
                Integer userStatus = null;
                if (row.getCell(21) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    userStatus = new Double(row.getCell(21).getNumericCellValue()).intValue();
                    sysUser.setUserStatus(userStatus);//第22列
                }
                Date createTime = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(22).getDateCellValue() == null || "".equals(row.getCell(22).getDateCellValue())) ? createTime : row.getCell(22).getDateCellValue();
                    sysUser.setCreateTime(createTime);//第23列
                }
                Date updateTime = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    updateTime = (row.getCell(23).getDateCellValue() == null || "".equals(row.getCell(23).getDateCellValue())) ? updateTime : row.getCell(23).getDateCellValue();
                    sysUser.setUpdateTime(updateTime);//第24列
                }
                Date invalidTime = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    invalidTime = (row.getCell(24).getDateCellValue() == null || "".equals(row.getCell(24).getDateCellValue())) ? invalidTime : row.getCell(24).getDateCellValue();
                    sysUser.setInvalidTime(invalidTime);//第25列
                }
                Date lastLogin = null;
                if (row.getCell(25) != null && row.getCell(25).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    lastLogin = (row.getCell(25).getDateCellValue() == null || "".equals(row.getCell(25).getDateCellValue())) ? lastLogin : row.getCell(25).getDateCellValue();
                    sysUser.setLastLogin(lastLogin);//第26列
                }
                sysUserList.add(sysUser);
            }
            System.out.println("sysUserList是：" + sysUserList);

            //用户备份
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建用户备份
                SysUserBackUp sysUserBackUp = new SysUserBackUp();
//
                sysUserBackUp.setUserName(row.getCell(1).getStringCellValue());
                sysUserBackUp.setPassword(row.getCell(2).getStringCellValue());
                sysUserBackUp.setTrueName(row.getCell(3).getStringCellValue());
                sysUserBackUp.setUserType(new Double(row.getCell(4).getNumericCellValue()).intValue());
                sysUserBackUp.setSex(new Double(row.getCell(5).getNumericCellValue()).intValue());
                sysUserBackUp.setEMail(row.getCell(6).getStringCellValue());
                sysUserBackUp.setMobilePhone(String.valueOf(row.getCell(7).getNumericCellValue()));
                sysUserBackUp.setWorkNo(row.getCell(8).getStringCellValue());
                sysUserBackUp.setState(new Double(row.getCell(12).getNumericCellValue()).intValue());
                Integer userLevel = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(14).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    userLevel = new Double(row.getCell(14).getNumericCellValue()).intValue();
                    sysUserBackUp.setUserLevel(userLevel);//第15列
                }
                sysUserBackUp.setCreatorId(new Double(row.getCell(15).getNumericCellValue()).longValue());
                String secret = null;
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    secret = (row.getCell(16).getStringCellValue() == null || "".equals(row.getCell(16).getStringCellValue())) ? secret : row.getCell(16).getStringCellValue();
                    sysUserBackUp.setSecret(secret);//第17列
                }
                sysUserBackUp.setAppkey(row.getCell(17).getStringCellValue());
                sysUserBackUp.setImageUrl(row.getCell(18).getStringCellValue());
                sysUserBackUp.setRoleId(new Double(row.getCell(19).getNumericCellValue()).longValue());
                sysUserBackUp.setOrgId(new Double(row.getCell(20).getNumericCellValue()).longValue());
                sysUserBackUp.setUserStatus(new Double(row.getCell(21).getNumericCellValue()).intValue());
                sysUserBackUp.setCreateTime(row.getCell(22).getDateCellValue());
                sysUserBackUp.setUpdateTime(row.getCell(23).getDateCellValue());
                sysUserBackUp.setLastLogin(row.getCell(25).getDateCellValue());
//
                sysUserBackupList.add(sysUserBackUp);
            }
            System.out.println("sysUserBackupList是：" + sysUserBackupList);
            int sizeNum1 = sysUserList.size();
            int sizeNum2 = sysUserBackupList.size();
            int resultNum = 0;
            if (sizeNum1 == total - 1 && sizeNum2 == total - 1 ) {
                resultNum = templateToImportService.batchUpdateSysUser(sysUserList);
                int resultNum2 = templateToImportService.batchUpdateSysUserBackup(sysUserBackupList);
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "机器人区域数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelRobotRegion", method = RequestMethod.POST)
    public Result importExcelRobotRegion(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TRobotRegion> tRobotRegionList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //机器人区域层级
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建机器人区域层级表
                TRobotRegion tRobotRegion = new TRobotRegion();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(0).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(0).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(0) != null && row.getCell(0).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tRobotRegion.setRegionId(new Double(row.getCell(0).getNumericCellValue()).longValue());//第1列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                String regionName = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    regionName = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? regionName : row.getCell(1).getStringCellValue();
                    tRobotRegion.setRegionName(regionName);//第2列
                }
                Integer sort = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    sort = new Double(row.getCell(2).getNumericCellValue()).intValue();
                    tRobotRegion.setSort(sort);//第3列
                }
                Long upRegionId = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upRegionId = new Double(row.getCell(3).getNumericCellValue()).longValue();
                    tRobotRegion.setUpRegionId(upRegionId);//第4列
                }
                String UpRegionIds = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    UpRegionIds = (row.getCell(4).getStringCellValue() == null || "".equals(row.getCell(4).getStringCellValue())) ? UpRegionIds : row.getCell(4).getStringCellValue();
                    tRobotRegion.setUpRegionIds(UpRegionIds);//第5列
                }
                Integer regionType = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    regionType = new Double(row.getCell(5).getNumericCellValue()).intValue();
                    tRobotRegion.setRegionType(regionType);//第6列
                }
                String stationId = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    stationId = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? stationId : row.getCell(6).getStringCellValue();
                    tRobotRegion.setStationId(stationId);//第7列
                }
                String stationName = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    stationName = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? UpRegionIds : row.getCell(7).getStringCellValue();
                    tRobotRegion.setStationName(stationName);//第8列
                }
                Integer state = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    state = new Double(row.getCell(8).getNumericCellValue()).intValue();
                    tRobotRegion.setState(state);//第9列
                }
                Date createTime = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(9).getDateCellValue() == null || "".equals(row.getCell(9).getDateCellValue())) ? createTime : row.getCell(9).getDateCellValue();
                    tRobotRegion.setCreateTime(createTime);//第10列
                }

                tRobotRegionList.add(tRobotRegion);
            }
            System.out.println("tRobotRegionList是:" + tRobotRegionList);
            int resultNum = templateToImportService.batchUpdateTRobotRegion(tRobotRegionList);
            result.setData(resultNum);
        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "标准区域数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelStdRegion", method = RequestMethod.POST)
    public Result importExcelStdRegion(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TStdRegion> tStdRegionList = new ArrayList<>();
            List<SysRoleRegion> sysRoleRegionList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //标准区域
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建标准区域
                TStdRegion tStdRegion = new TStdRegion();

                String regionName = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    regionName = (row.getCell(1).getStringCellValue().trim() == null || "".equals(row.getCell(1).getStringCellValue())) ? regionName : row.getCell(1).getStringCellValue();
                    tStdRegion.setRegionName(regionName);//第2列
                }
                Integer sort = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    sort = new Double(row.getCell(2).getNumericCellValue()).intValue();
                    tStdRegion.setSort(sort);//第3列
                }
                Long upRegionId = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upRegionId = new Double(row.getCell(3).getNumericCellValue()).longValue();
                    tStdRegion.setUpRegionId(upRegionId);//第4列
                }
                String upRegionIds = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    upRegionIds = (row.getCell(4).getStringCellValue() == null || "".equals(row.getCell(4).getStringCellValue())) ? upRegionIds : row.getCell(4).getStringCellValue();
                    tStdRegion.setUpRegionIds(upRegionIds);//第5列
                }
                Integer regionCode = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    regionCode = new Double(row.getCell(5).getNumericCellValue()).intValue();
                    tStdRegion.setRegionCode(regionCode);//第6列
                }
                String stationId = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    stationId = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? stationId : row.getCell(6).getStringCellValue();
                    tStdRegion.setStationId(stationId);//第7列
                }
                String stationName = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    stationName = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? stationName : row.getCell(7).getStringCellValue();
                    tStdRegion.setStationName(stationName);//第8列
                }
                Integer state = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    state = new Double(row.getCell(8).getNumericCellValue()).intValue();
                    tStdRegion.setState(state);//第9列
                }
                Date createTime = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(9).getDateCellValue() == null || "".equals(row.getCell(9).getDateCellValue())) ? createTime : row.getCell(9).getDateCellValue();
                    tStdRegion.setCreateTime(createTime);//第10列
                }
                tStdRegionList.add(tStdRegion);
            }
            //获取标准区域表的最后一条regionId
            Long lastRegionId = templateToImportService.selectLastRegionId();
            //权限区域
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建权限区域
                SysRoleRegion sysRoleRegion = new SysRoleRegion();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(10).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(10).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    sysRoleRegion.setRoleId(new Double(row.getCell(10).getNumericCellValue()).longValue());//第11列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                sysRoleRegion.setRegionId(lastRegionId - 1 + i);
                String isChecked = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    isChecked = (row.getCell(11).getStringCellValue() == null || "".equals(row.getCell(11).getStringCellValue())) ? isChecked : row.getCell(11).getStringCellValue();
                    sysRoleRegion.setIsChecked(isChecked);//第12列
                }
                sysRoleRegionList.add(sysRoleRegion);
            }
            int sizeNum1 = sysRoleRegionList.size();
            int sizeNum2 = tStdRegionList.size();
            int resultNum = 0;
            if (sizeNum1 == total - 1 && sizeNum2 == total - 1) {
                resultNum = templateToImportService.batchUpdateTStdRegion(tStdRegionList);
                int resultNum2 = templateToImportService.batchUpdateSysRoleRegion(sysRoleRegionList);
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "模板导入测试", notes = "导入测试")
    @RequestMapping(value = "/importExcelTest", method = RequestMethod.POST)
    public Result importExcelTest(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<ExcelTemplate> excelTemplateList = new ArrayList<>();
            SysRoleRegion sysRoleRegion = new SysRoleRegion();

            excelTemplateList.add(new ExcelTemplate("角色ID", true, "Integer"));
            excelTemplateList.add(new ExcelTemplate("机器人ID", true, "Integer"));
            excelTemplateList.add(new ExcelTemplate("是否检查", false, "String"));

            System.out.println("excelTemplateList是：" + excelTemplateList);

            //遍历所有行
            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);

                for (int j = 0; j < excelTemplateList.size(); j++) {
                    //判断列是否必填
//                        if(itemAsLie.IsRequire && (row.getCell(j).getStringCellValue() == null || "".equals(row.getCell(j).getStringCellValue()))){
                    if (row.getCell(j) == null) {
                        System.out.println("第" + (i + 1) + "行第" + (j + 1) + "列的值为空，请检查");
                    } else {
                        String jieguo = row.getCell(j).getStringCellValue();
                        System.out.println("第" + (i + 1) + "行第" + (j + 1) + "列的值为：" + jieguo);
                    }

                }
                //遍历列
                for (ExcelTemplate itemAsLie : excelTemplateList) {
                    System.out.println("itemAsLie是：" + itemAsLie);
                    //判断列类型尝试类型转换
                    switch (itemAsLie.ColumnType) {
                        case "Long": {
                            System.out.println("Long");
                            break;
                        }
                        case "Integer": {
                            System.out.println("Integer类型");
                            break;
                        }
                        case "String": {
                            System.out.println("String类型");
                            break;
                        }
                    }
                    //判断列是否必填
                    if (itemAsLie.IsRequire && row.getCell(0) == null || "".equals(row.getCell(0))) {
                        System.out.println("该列是必填的，不能为空");
                    }
                }

            }

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "遥测量数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelTCfgTelemeter", method = RequestMethod.POST)
    public Result importExcelTCfgTelemeter(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TCfgTelemeter> tCfgTelemeterList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //遥测量
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建遥测量
                TCfgTelemeter tCfgTelemeter = new TCfgTelemeter();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(0).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(0).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(0) != null && row.getCell(0).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelemeter.setDeviceId(row.getCell(0).getStringCellValue());//第1列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                if (row.getCell(1) == null || row.getCell(1).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(1).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(1).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelemeter.setMeteId(row.getCell(1).getStringCellValue());//第2列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String meteName = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteName = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? meteName : row.getCell(2).getStringCellValue();
                    tCfgTelemeter.setMeteName(meteName);//第3列
                }
                Float upEffect = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upEffect = (float) row.getCell(3).getNumericCellValue();
                    tCfgTelemeter.setUpEffect(upEffect);//第4列
                }
                Float lowEffect = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowEffect = (float) row.getCell(4).getNumericCellValue();
                    tCfgTelemeter.setLowEffect(lowEffect);//第5列
                }
                Integer metePrecision = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    metePrecision = new Double(row.getCell(5).getNumericCellValue()).intValue();
                    tCfgTelemeter.setMetePrecision(metePrecision);//第6列
                }
                String unit = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    unit = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? unit : row.getCell(6).getStringCellValue();
                    tCfgTelemeter.setUnit(unit);//第7列
                }
                Integer meteIndex = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    meteIndex = new Double(row.getCell(7).getNumericCellValue()).intValue();
                    tCfgTelemeter.setMeteIndex(meteIndex);//第8列
                }
                Integer meteCid = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    meteCid = new Double(row.getCell(8).getNumericCellValue()).intValue();
                    tCfgTelemeter.setMeteCid(meteCid);//第9列
                }
                Float limitBand = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    limitBand = (float) row.getCell(9).getNumericCellValue();
                    tCfgTelemeter.setLimitBand(limitBand);//第10列
                }
                Float changeLimit = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    changeLimit = (float) row.getCell(10).getNumericCellValue();
                    tCfgTelemeter.setChangeLimit(changeLimit);//第11列
                }
                String validMid = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    validMid = (row.getCell(11).getStringCellValue() == null || "".equals(row.getCell(11).getStringCellValue())) ? validMid : row.getCell(11).getStringCellValue();
                    tCfgTelemeter.setValidMid(validMid);//第12列
                }
                String validString = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    validString = (row.getCell(12).getStringCellValue() == null || "".equals(row.getCell(12).getStringCellValue())) ? validString : row.getCell(12).getStringCellValue();
                    tCfgTelemeter.setValidString(validString);//第13列
                }
                Float invalidValue = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    invalidValue = (float) row.getCell(13).getNumericCellValue();
                    tCfgTelemeter.setInvalidValue(invalidValue);//第14列
                }
                Float lastValue = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(14).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lastValue = (float) row.getCell(14).getNumericCellValue();
                    tCfgTelemeter.setLastValue(lastValue);//第15列
                }
                Date lastTime = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    lastTime = (row.getCell(15).getDateCellValue() == null || "".equals(row.getCell(15).getDateCellValue())) ? lastTime : row.getCell(15).getDateCellValue();
                    tCfgTelemeter.setLastTime(lastTime);//第16列
                }
                if (row.getCell(16) == null || row.getCell(16).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(16).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(16).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelemeter.setMeteCode(row.getCell(16).getStringCellValue());//第17列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(17) == null || row.getCell(17).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(17).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(17).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelemeter.setDeviceType(row.getCell(17).getStringCellValue());//第18列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String description = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    description = (row.getCell(18).getStringCellValue() == null || "".equals(row.getCell(18).getStringCellValue())) ? description : row.getCell(18).getStringCellValue();
                    tCfgTelemeter.setDescription(description);//第19列
                }
                Float hilimit1 = null;
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(19).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    hilimit1 = (float) row.getCell(19).getNumericCellValue();
                    tCfgTelemeter.setHilimit1(hilimit1);//第20列
                }
                Float lolimit1 = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lolimit1 = (float) row.getCell(20).getNumericCellValue();
                    tCfgTelemeter.setLolimit1(lolimit1);//第21列
                }
                Float hilimit2 = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    hilimit2 = (float) row.getCell(21).getNumericCellValue();
                    tCfgTelemeter.setHilimit2(hilimit2);//第22列
                }
                Float lolimit2 = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(22).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lolimit2 = (float) row.getCell(22).getNumericCellValue();
                    tCfgTelemeter.setLolimit2(lolimit2);//第23列
                }
                Float hilimit3 = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    hilimit3 = (float) row.getCell(23).getNumericCellValue();
                    tCfgTelemeter.setHilimit3(hilimit3);//第24列
                }
                Float lolimit3 = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lolimit3 = (float) row.getCell(24).getNumericCellValue();
                    tCfgTelemeter.setLolimit3(lolimit3);//第25列
                }
                Float hilimit4 = null;
                if (row.getCell(25) != null && row.getCell(25).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(25).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    hilimit4 = (float) row.getCell(25).getNumericCellValue();
                    tCfgTelemeter.setHilimit4(hilimit4);//第26列
                }
                Float lolimit4 = null;
                if (row.getCell(26) != null && row.getCell(26).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AA列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(26).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lolimit4 = (float) row.getCell(26).getNumericCellValue();
                    tCfgTelemeter.setLolimit4(lolimit4);//第27列
                }
                Float stander = null;
                if (row.getCell(27) != null && row.getCell(27).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(27).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    stander = (float) row.getCell(27).getNumericCellValue();
                    tCfgTelemeter.setStander(stander);//第28列
                }
                Integer isshield = null;
                if (row.getCell(28) != null && row.getCell(28).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(28).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    isshield = new Double(row.getCell(28).getNumericCellValue()).intValue();
                    tCfgTelemeter.setIsshield(isshield);//第29列
                }
                Long storageperiod = null;
                if (row.getCell(29) != null && row.getCell(29).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "AB列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(29).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    storageperiod = new Double(row.getCell(29).getNumericCellValue()).longValue();
                    tCfgTelemeter.setStorageperiod(storageperiod);//第30列
                }
                String linkMeteId = null;
                if (row.getCell(30) != null && row.getCell(30).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AE列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    linkMeteId = (row.getCell(30).getStringCellValue() == null || "".equals(row.getCell(30).getStringCellValue())) ? linkMeteId : row.getCell(30).getStringCellValue();
                    tCfgTelemeter.setLinkMeteId(linkMeteId);//第31列
                }
                tCfgTelemeterList.add(tCfgTelemeter);

            }

            int resultNum = templateToImportService.batchUpdateTCfgTelemeter(tCfgTelemeterList);
            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "遥信量数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelTCfgTeleSignal", method = RequestMethod.POST)
    public Result importExcelTCfgTeleSignal(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TCfgTelesignal> tCfgTelesignalList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //遥测量
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建遥测量
                TCfgTelesignal tCfgTelesignal = new TCfgTelesignal();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(0).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(0).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(0) != null && row.getCell(0).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelesignal.setDeviceId(row.getCell(0).getStringCellValue());//第1列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(1) == null || row.getCell(1).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(1).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(1).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelesignal.setMeteId(row.getCell(1).getStringCellValue());//第2列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String meteName = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    meteName = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? meteName : row.getCell(2).getStringCellValue();
                    tCfgTelesignal.setMeteName(meteName);//第3列
                }
                Integer upEffect = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    upEffect = new Double(row.getCell(3).getNumericCellValue()).intValue();
                    tCfgTelesignal.setUpEffect(upEffect);//第4列
                }
                Integer lowEffect = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowEffect = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    tCfgTelesignal.setLowEffect(lowEffect);//第5列
                }
                Integer meteIndex = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    meteIndex = new Double(row.getCell(5).getNumericCellValue()).intValue();
                    tCfgTelesignal.setMeteIndex(meteIndex);//第6列
                }
                Integer meteCid = null;
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    meteCid = new Double(row.getCell(6).getNumericCellValue()).intValue();
                    tCfgTelesignal.setMeteCid(meteCid);//第7列
                }
                Integer signalKind = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    signalKind = new Double(row.getCell(7).getNumericCellValue()).intValue();
                    tCfgTelesignal.setSignalKind(signalKind);//第8列
                }
                Integer lastValue = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lastValue = new Double(row.getCell(8).getNumericCellValue()).intValue();
                    tCfgTelesignal.setLastValue(lastValue);//第9列
                }
                Date lastTime = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    lastTime = (row.getCell(9).getDateCellValue() == null || "".equals(row.getCell(9).getDateCellValue())) ? lastTime : row.getCell(9).getDateCellValue();
                    tCfgTelesignal.setLastTime(lastTime);//第10列
                }
                Integer explainType = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    explainType = new Double(row.getCell(10).getNumericCellValue()).intValue();
                    tCfgTelesignal.setExplainType(explainType);//第11列
                }
                Integer reportType = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    reportType = new Double(row.getCell(11).getNumericCellValue()).intValue();
                    tCfgTelesignal.setReportType(reportType);//第12列
                }
                Integer reportLevel = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    reportLevel = new Double(row.getCell(12).getNumericCellValue()).intValue();
                    tCfgTelesignal.setReportLevel(reportLevel);//第13列
                }
                Integer maskType = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    maskType = new Double(row.getCell(13).getNumericCellValue()).intValue();
                    tCfgTelesignal.setMaskType(maskType);//第14列
                }
                String maskMid = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    maskMid = (row.getCell(14).getStringCellValue() == null || "".equals(row.getCell(14).getStringCellValue())) ? maskMid : row.getCell(14).getStringCellValue();
                    tCfgTelesignal.setMaskMid(maskMid);//第15列
                }
                String maskString = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    maskString = (row.getCell(15).getStringCellValue() == null || "".equals(row.getCell(15).getStringCellValue())) ? maskString : row.getCell(15).getStringCellValue();
                    tCfgTelesignal.setMaskString(maskString);//第16列
                }
                Integer maskValue = null;
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    maskValue = new Double(row.getCell(16).getNumericCellValue()).intValue();
                    tCfgTelesignal.setMaskValue(maskValue);//第17列
                }
                Integer delayTime = null;
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(17).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    delayTime = new Double(row.getCell(17).getNumericCellValue()).intValue();
                    tCfgTelesignal.setDelayTime(delayTime);//第18列
                }
                if (row.getCell(18) == null || row.getCell(18).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(18).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(18).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelesignal.setMeteCode(row.getCell(18).getStringCellValue());//第19列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(19) == null || row.getCell(19).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(19).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(19).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.STRING)) {
                    tCfgTelesignal.setDeviceType(row.getCell(19).getStringCellValue());//第20列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String description = null;
                if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    description = (row.getCell(20).getStringCellValue() == null || "".equals(row.getCell(20).getStringCellValue())) ? description : row.getCell(20).getStringCellValue();
                    tCfgTelesignal.setDescription(description);//第21列
                }
                Integer isshield = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    isshield = new Double(row.getCell(21).getNumericCellValue()).intValue();
                    tCfgTelesignal.setIsshield(isshield);//第22列
                }
                Long storageperiod = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(22).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    storageperiod = new Double(row.getCell(22).getNumericCellValue()).longValue();
                    tCfgTelesignal.setStorageperiod(storageperiod);//第23列
                }
                String describer = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    describer = (row.getCell(23).getStringCellValue() == null || "".equals(row.getCell(23).getStringCellValue())) ? describer : row.getCell(23).getStringCellValue();
                    tCfgTelesignal.setDescriber(describer);//第24列
                }
                Integer alarmthresbhold = null;
                if (row.getCell(24) != null && row.getCell(24).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmthresbhold = new Double(row.getCell(24).getNumericCellValue()).intValue();
                    tCfgTelesignal.setAlarmthresbhold(alarmthresbhold);//第25列
                }
                Integer alarmlevel = null;
                if (row.getCell(25) != null && row.getCell(25).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "Z列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(25).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmlevel = new Double(row.getCell(25).getNumericCellValue()).intValue();
                    tCfgTelesignal.setAlarmlevel(alarmlevel);//第26列
                }
                String linkMeteId = null;
                if (row.getCell(26) != null && row.getCell(26).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "AA列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    linkMeteId = (row.getCell(26).getStringCellValue() == null || "".equals(row.getCell(26).getStringCellValue())) ? linkMeteId : row.getCell(26).getStringCellValue();
                    tCfgTelesignal.setLinkMeteId(linkMeteId);//第27列
                }
                tCfgTelesignalList.add(tCfgTelesignal);
            }

            int resultNum = templateToImportService.batchUpdateTCfgTelesignal(tCfgTelesignalList);
            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }

    @ApiOperation(value = "算法数据模板导入", notes = "导入")
    @RequestMapping(value = "/importExcelTAlgorithm", method = RequestMethod.POST)
    public Result importExcelTAlgorithm(@RequestParam("excelFile") String pathName) {
        Result result = new Result();
        try {
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(pathName));//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始

            List<TAlgorithmInfo> tAlgorithmInfoList = new ArrayList<>();
            List<TAlgorithmConf> tAlgorithmConfList = new ArrayList<>();

            StringBuffer errMsg = new StringBuffer();
            //算法
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);//获取第i+1行
                //创建算法
                TAlgorithmInfo tAlgorithmInfo = new TAlgorithmInfo();

                if (row.getCell(0) == null || row.getCell(0).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(0).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(0).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(0) != null && row.getCell(0).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tAlgorithmInfo.setAlgorithmId(new Double(row.getCell(0).getNumericCellValue()).longValue());//第17列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "A列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                String algorithmName = null;
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    algorithmName = (row.getCell(1).getStringCellValue() == null || "".equals(row.getCell(1).getStringCellValue())) ? algorithmName : row.getCell(1).getStringCellValue();
                    tAlgorithmInfo.setAlgorithmName(algorithmName);//第2列
                }
                String algorithmType = null;
                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    algorithmType = (row.getCell(2).getStringCellValue() == null || "".equals(row.getCell(2).getStringCellValue())) ? algorithmType : row.getCell(2).getStringCellValue();
                    tAlgorithmInfo.setAlgorithmType(algorithmType);//第3列
                }
                String describel = null;
                if (row.getCell(3) != null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "D列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    describel = (row.getCell(3).getStringCellValue() == null || "".equals(row.getCell(3).getStringCellValue())) ? describel : row.getCell(3).getStringCellValue();
                    tAlgorithmInfo.setDescribel(describel);//第4列
                }
                Integer algorithmCode = null;
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    algorithmCode = new Double(row.getCell(4).getNumericCellValue()).intValue();
                    tAlgorithmInfo.setAlgorithmCode(algorithmCode);//第5列
                }
                String analysType = null;
                if (row.getCell(5) != null && row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    analysType = (row.getCell(5).getStringCellValue() == null || "".equals(row.getCell(5).getStringCellValue())) ? analysType : row.getCell(5).getStringCellValue();
                    tAlgorithmInfo.setAnalysType(analysType);//第6列
                }
                tAlgorithmInfoList.add(tAlgorithmInfo);
            }

            //算法配置
            for (int i = 2; i <= total; i++){
                Row row = sheet.getRow(i);//获取第i+1行
                //创建算法配置
                TAlgorithmConf  tAlgorithmConf = new TAlgorithmConf();
                if (row.getCell(6) == null || row.getCell(6).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(6).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(6).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不能为空<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                if (row.getCell(6) != null && row.getCell(6).getCellTypeEnum().equals(CellType.STRING)) {
                    tAlgorithmConf.setInsId(row.getCell(6).getStringCellValue());//第7列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }
                tAlgorithmConf.setAlgorithmId(new Double(row.getCell(0).getNumericCellValue()).longValue());
                String insName = null;
                if (row.getCell(7) != null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    insName = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? insName : row.getCell(7).getStringCellValue();
                    tAlgorithmConf.setInsName(insName);//第8列
                }
                Long inspectiondevId = null;
                if (row.getCell(8) != null && row.getCell(8).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    inspectiondevId = new Double(row.getCell(8).getNumericCellValue()).longValue();
                    tAlgorithmConf.setInspectiondevId(inspectiondevId);//第9列
                }
                tAlgorithmConf.setAlgorithmName(row.getCell(1).getStringCellValue());
                Integer status = null;
                if (row.getCell(9) != null && row.getCell(9).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    status = new Double(row.getCell(9).getNumericCellValue()).intValue();
                    tAlgorithmConf.setStatus(status);//第10列
                }
                Integer ifDel = null;
                if (row.getCell(10) != null && row.getCell(10).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(10).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    ifDel = new Double(row.getCell(10).getNumericCellValue()).intValue();
                    tAlgorithmConf.setIfDel(ifDel);//第11列
                }
                Integer ifShow = null;
                if (row.getCell(11) != null && row.getCell(11).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    ifShow = new Double(row.getCell(11).getNumericCellValue()).intValue();
                    tAlgorithmConf.setIfShow(ifShow);//第12列
                }
                String picUrl = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是字符串类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    picUrl = (row.getCell(12).getStringCellValue() == null || "".equals(row.getCell(12).getStringCellValue())) ? picUrl : row.getCell(12).getStringCellValue();
                    tAlgorithmConf.setPicUrl(picUrl);//第13列
                }
                Integer applyModule = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else if (row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    applyModule = new Double(row.getCell(13).getNumericCellValue()).intValue();
                    tAlgorithmConf.setApplyModule(applyModule);//第14列
                }
                Date createTime = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    createTime = (row.getCell(14).getDateCellValue() == null || "".equals(row.getCell(14).getDateCellValue())) ? createTime : row.getCell(14).getDateCellValue();
                    tAlgorithmConf.setCreateTime(createTime);//第15列
                }
                Date updateTime = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)) {
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是日期类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                } else {
                    updateTime = (row.getCell(15).getDateCellValue() == null || "".equals(row.getCell(15).getDateCellValue())) ? updateTime : row.getCell(15).getDateCellValue();
                    tAlgorithmConf.setUpdateTime(updateTime);//第16列
                }
                tAlgorithmConfList.add(tAlgorithmConf);
            }

            int sizeNum1 = tAlgorithmInfoList.size();
            int sizeNum2 = tAlgorithmConfList.size();
            int resultNum = 0;
            if(sizeNum1 == total - 1 && sizeNum2 == total - 1){
                resultNum = templateToImportService.batchUpdateTAlgorithmInfo(tAlgorithmInfoList);
                int resultNum2 = templateToImportService.batchUpdateTAlgorithmConf(tAlgorithmConfList);
            } else {
                log.error("导入异常：数据格式有误");
            }

            result.setData(resultNum);//返回插入的条数

        } catch (Exception e) {
            result.setMessage(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("导入失败：" + e);
        }
        return result;
    }
}