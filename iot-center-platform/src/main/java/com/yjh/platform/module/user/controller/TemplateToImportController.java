package com.yjh.platform.module.user.controller;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.controller.TStdMetemodelController;
import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
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
                    log.error("模板有误:"+errMsg);
                    break;
                }
                if (row.getCell(1) != null && row.getCell(1).getCellTypeEnum().equals(CellType.STRING)) {
                    tStdMeteModel.setModelName(row.getCell(1).getStringCellValue());//第2列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "B列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                }
                if (row.getCell(2) == null || row.getCell(2).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(2).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(2).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不能为空<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                }

                if (row.getCell(4) == null || row.getCell(4).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(4).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(4).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不能为空<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                }
                if (row.getCell(4) != null && row.getCell(4).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tStdMeteModel.setDeviceType
                            (new Double(row.getCell(4).getNumericCellValue()).intValue());//第5列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "E列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                }

                String remark = null;
                if(row.getCell(24) !=null && row.getCell(24).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "Y列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    remark = (row.getCell(24).getStringCellValue() == null || "".equals(row.getCell(24).getStringCellValue())) ? remark : row.getCell(24).getStringCellValue();
                    tStdMeteModel.setRemark(remark);//第25列
                }
                tStdMeteModelList.add(tStdMeteModel);
            }

            int sizeNum = tStdMeteModelList.size();

            long lastModelId = templateToImportService.selectLastModelId();    //获取模板表的最后一条modelid

            //测点模板详细
            for (int i = 2; i <= total; i++) {
                Row row = sheet.getRow(i);
                //创建系统测点模板详细表
                TStdMeteModelDetail tStdMeteModelDetail = new TStdMeteModelDetail();

                tStdMeteModelDetail.setModelId(lastModelId-1+i);

                if (row.getCell(2) != null && row.getCell(2).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    tStdMeteModelDetail.setMeteId
                            (new Double(row.getCell(2).getNumericCellValue()).longValue());//第3列
                } else {
                    errMsg.append("第" + (i + 1) + "行," + "C列,点号不是数值类型<br>");
                    log.error("模板有误:" + errMsg);
                    break;
                }

                Integer customType = null;
                if(row.getCell(3) !=null && row.getCell(3).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    customType =  new Double(row.getCell(3).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setCustomType(customType);//第4列
                } else {
                    tStdMeteModelDetail.setCustomType(0);
                }

                String  meteCode = null;
                if(row.getCell(5) !=null && row.getCell(5).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "F列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    meteCode = (row.getCell(5).getStringCellValue() == null || "".equals(row.getCell(5).getStringCellValue())) ? meteCode : row.getCell(5).getStringCellValue();
                    tStdMeteModelDetail.setMeteCode(meteCode);//第6列
                }

                String meteName = null;
                if(row.getCell(6) !=null && row.getCell(6).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "G列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    meteName = (row.getCell(6).getStringCellValue() == null || "".equals(row.getCell(6).getStringCellValue())) ? meteName : row.getCell(6).getStringCellValue();
                    tStdMeteModelDetail.setMeteName(meteName);//第7列
                }

                String meteType = null;
                if(row.getCell(7) !=null && row.getCell(7).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "H列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    meteType = (row.getCell(7).getStringCellValue() == null || "".equals(row.getCell(7).getStringCellValue())) ? meteType : row.getCell(7).getStringCellValue();
                    tStdMeteModelDetail.setMeteType(meteType);//第8列
                }

                String unit = null;
                if(row.getCell(8) !=null && row.getCell(8).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "I列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    unit = (row.getCell(8).getStringCellValue() == null || "".equals(row.getCell(8).getStringCellValue())) ? unit : row.getCell(8).getStringCellValue();
                    tStdMeteModelDetail.setUnit(unit);//第9列
                }

                String alarmNote = null;
                if(row.getCell(9) !=null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "J列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    alarmNote = (row.getCell(9).getStringCellValue() == null || "".equals(row.getCell(9).getStringCellValue())) ? alarmNote : row.getCell(9).getStringCellValue();
                    tStdMeteModelDetail.setAlarmNote(alarmNote);//第10列
                }

                String alarmExplain = null;
                if(row.getCell(9) !=null && row.getCell(9).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "K列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    alarmExplain = (row.getCell(10).getStringCellValue() == null || "".equals(row.getCell(10).getStringCellValue())) ? alarmExplain : row.getCell(10).getStringCellValue();
                    tStdMeteModelDetail.setAlarmExplain(alarmExplain);//第11列
                }

                String alarmType = null;
                if(row.getCell(11) !=null && row.getCell(11).getCellTypeEnum().equals(CellType.NUMERIC)){
                    errMsg.append("第" + (i + 1) + "行," + "L列,点号不是字符串类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else {
                    alarmType = (row.getCell(11).getStringCellValue() == null || "".equals(row.getCell(11).getStringCellValue())) ? alarmType : row.getCell(11).getStringCellValue();
                    tStdMeteModelDetail.setAlarmType(alarmType);//第12列
                }

                Float upEffect = null;
                if (row.getCell(12) != null && row.getCell(12).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "M列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if(row.getCell(12).getCellTypeEnum().equals(CellType.NUMERIC)){
                    upEffect = (float)row.getCell(12).getNumericCellValue();
                    tStdMeteModelDetail.setUpEffect(upEffect);//第13列
                }

                Float lowEffect = null;
                if (row.getCell(13) != null && row.getCell(13).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "N列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if(row.getCell(13).getCellTypeEnum().equals(CellType.NUMERIC)){
                    lowEffect = (float)row.getCell(13).getNumericCellValue();
                    tStdMeteModelDetail.setLowEffect(lowEffect);//第14列
                }

                Integer alarmLevel = null;
                if (row.getCell(14) != null && row.getCell(14).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "O列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(14).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmLevel = new Double(row.getCell(14).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmLevel(alarmLevel);//第15列
                }

                Float highLimit1 = null;
                if (row.getCell(15) != null && row.getCell(15).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "P列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(15).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit1 =  (float) row.getCell(15).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit1(highLimit1);//第16列
                }

                Float lowLimit1 = null;
                if (row.getCell(16) != null && row.getCell(16).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "Q列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(16).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit1 =  (float) row.getCell(16).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit1(lowLimit1);//第17列
                }

                Float highLimit2 = null;
                if (row.getCell(17) != null && row.getCell(17).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "R列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(17).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    highLimit2 = (float) row.getCell(17).getNumericCellValue();
                    tStdMeteModelDetail.setHighLimit2(highLimit2);//第18列
                }

                Float lowLimit2 = null;
                if (row.getCell(18) != null && row.getCell(18).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "S列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(18).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    lowLimit2 =  (float) row.getCell(18).getNumericCellValue();
                    tStdMeteModelDetail.setLowLimit2(lowLimit2);//第19列
                }

                Integer alarmDelay = null;
                if (row.getCell(19) != null && row.getCell(19).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "T列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                }else if (row.getCell(19).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmDelay =  new Double(row.getCell(19).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmDelay(alarmDelay);//第20列
                }

                Integer alarmCnt = null;if (row.getCell(20) != null && row.getCell(20).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "U列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(20).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    alarmCnt = new Double(row.getCell(20).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setAlarmCnt(alarmCnt);//第21列
                }

                BigDecimal thresholdAbs = null;
                if (row.getCell(21) != null && row.getCell(21).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "V列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(21).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdAbs =  BigDecimal.valueOf(row.getCell(21).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdAbs(thresholdAbs);//第22列
                }

                BigDecimal thresholdPer = null;
                if (row.getCell(22) != null && row.getCell(22).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "W列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(22).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    thresholdPer =  BigDecimal.valueOf(row.getCell(22).getNumericCellValue());
                    tStdMeteModelDetail.setThresholdPer(thresholdPer);//第23列
                }

                Integer modulus = null;
                if (row.getCell(23) != null && row.getCell(23).getCellTypeEnum().equals(CellType.STRING)){
                    errMsg.append("第" + (i + 1) + "行," + "X列,点号不是数值类型<br>");
                    log.error("模板有误:"+errMsg);
                    break;
                } else if (row.getCell(23).getCellTypeEnum().equals(CellType.NUMERIC)) {
                    modulus =  new Double(row.getCell(23).getNumericCellValue()).intValue();
                    tStdMeteModelDetail.setModulus(modulus);//第24列
                }
                tStdMeteModelDetailList.add(tStdMeteModelDetail);
            }

            int sizeNum2 = tStdMeteModelDetailList.size();

            int resultNum = 0;
            if (sizeNum == total-1 && sizeNum2 == total-1){
                resultNum = templateToImportService.batchUpdateTStdMeteModel(tStdMeteModelList);
                int resultNum2 = templateToImportService.batchUpdateTStdMeteModelDetail(tStdMeteModelDetailList);
            }else{
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
