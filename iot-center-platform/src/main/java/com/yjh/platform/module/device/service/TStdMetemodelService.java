package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.ResultHandleUtils;
import com.yjh.platform.module.device.controller.TStdMetemodelController;
import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdMeteDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TStdMetemodelDao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.apache.catalina.startup.ExpandWar.deleteDir;
import static org.apache.catalina.startup.ExpandWar.expand;

/**
* @author tt
* @since 2020-08-07
*/
@Service
public class TStdMetemodelService {

    @Autowired
    private TStdMetemodelDao tStdMetemodelDao;

    @Autowired
    private TStdMetemodelDetailDao tStdMetemodelDetailDao;

    @Autowired
    private TStdDeviceDao tStdDeviceDao;

    @Autowired
    private TStdMeteDao tStdMeteDao;
    @Autowired
    RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TStdMetemodelService.class);

    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.add(tStdMeteModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.deleteByPrimaryId(modelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.update(tStdMeteModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdMeteModel selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.selectByPrimaryId(modelId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> select(Long modelId, String modelName, Integer deviceType, String remark) {
        return tStdMetemodelDao.select(modelId, modelName, deviceType, remark);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> selectByPage(TStdMeteModel tStdMeteModel) {
        return tStdMetemodelDao.selectByPage(tStdMeteModel);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModel> list) {
        return this.tStdMetemodelDao.batchAdd(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectMeteByDeviceType(Integer deviceType) {
        return tStdMetemodelDao.selectMeteByDeviceType(deviceType);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchAddModelMete(List<TStdMeteModelDetail> list) {
        Long modelId = list.get(0).getModelId();
        Long meteId=list.get(0).getMeteId();
        List<String> deviceList = tStdDeviceDao.selectByModelId(modelId);
        if (deviceList.size() > 0) {
            return 206;
        } else {
            tStdMetemodelDetailDao.deleteByPrimaryId(modelId,meteId);
            return this.tStdMetemodelDetailDao.batchAdd(list);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<String> list) {
        return this.tStdMetemodelDao.batchDelete(list);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<DeviceTypeTree> selectDeviceTypeModelTree(String deviceType) {
        List<MeteModel> list1=new ArrayList<>();
        if(deviceType==null || deviceType==""){
             list1 = tStdMetemodelDao.selectModel(null);
        }else { list1 = tStdMetemodelDao.selectModel(Integer.valueOf(deviceType));}
        List<DeviceTypeTree> list2 = tStdMetemodelDao.selectDevice(deviceType);
        for (int i = 0; i < list2.size(); i++) {
            List<MeteModel> list=new ArrayList<>();
            for (int j = 0; j < list1.size(); j++) {

                if (list2.get(i).getID().equals(list1.get(j).getDeviceType())){

                   list.add(list1.get(j));
                   list2.get(i).setChildren(list);
                }


            }

        }
        return list2;
    }

  @Transactional(rollbackFor = Exception.class)
  public Long addModel(ModelCreator modelCreator){
        TStdMeteModel m=new TStdMeteModel();
        m.setDeviceType(modelCreator.getDeviceType());
        m.setModelName(modelCreator.getModelName());
        m.setRemark(modelCreator.getRemark());
        tStdMetemodelDao.add(m);
        Long modelId=m.getModelId();
      for (Long id:modelCreator.getMeteIds()) {

          TStdMete mete=tStdMeteDao.selectByPrimaryId(id);
          TStdMeteModelDetail detail=new TStdMeteModelDetail();
          detail.setModelId(modelId);
          detail.setMeteId(mete.getStdMeteId());
          detail.setCustomId("101");
          detail.setMeteName(mete.getMeteName());
          detail.setMeteType(mete.getMeteType());
          detail.setMeteKind(mete.getMeteKind());//插入meteKind
          detail.setUnit(mete.getUnit());
          detail.setAnalyseType(mete.getAnalyseType());
          detail.setAlarmNote(mete.getAlarmNote());
          detail.setAlarmExplain(mete.getAlarmExplain());
          detail.setAlarmType(mete.getAlarmType());
          detail.setUpEffect(mete.getUpEffect());
          detail.setDownEffect(mete.getDownEffect());
          detail.setAlarmLevel(mete.getAlarmLevel());
          detail.setHighLimit1(mete.getHighLimit1());
          detail.setLowLimit1(mete.getLowLimit1());
          detail.setHighLimit2(mete.getHighLimit2());
          detail.setLowLimit2(mete.getLowLimit2());
          detail.setHighLimit3(mete.getHighLimit3());
          detail.setLowLimit3(mete.getLowLimit3());
          detail.setHighLimit4(mete.getHighLimit4());
          detail.setLowLimit4(mete.getLowLimit4());
          detail.setAlarmDelay(mete.getAlarmDelay());
          detail.setAlarmCnt(mete.getAlarmCnt());
          detail.setThresholdAbs(mete.getThresholdAbs());
          detail.setThresholdPer(mete.getThresholdPer());
          detail.setModulus(mete.getModulus());
          detail.setStateZero(mete.getStateZero());
          detail.setStateOne(mete.getStateOne());
          tStdMetemodelDetailDao.add(detail);

      }
        return m.getModelId();
  }

    @Transactional(rollbackFor = Exception.class)
    public ModelInfo selectModel(Long modelId){
        ModelInfo mInfo=new ModelInfo();
        TStdMeteModel model=tStdMetemodelDao.selectByPrimaryId(modelId);

       mInfo.setModel(model);


        List<MeteInfo> mete=tStdMetemodelDetailDao.selectMeteBlindModel(modelId);
        mInfo.setMeteInfo(mete);


        return  mInfo;

    }

    @Transactional(rollbackFor = Exception.class)
    public int updateModel(ModelCreator modelCreator){

        //修改模板名
        TStdMeteModel meteModel=tStdMetemodelDao.selectByPrimaryId(modelCreator.getModelId());
        meteModel.setModelName(modelCreator.getModelName());
        meteModel.setRemark(modelCreator.getRemark());
         tStdMetemodelDao.update(meteModel);


         //修改细节模板表
         List<MeteInfo> mete1=tStdMetemodelDetailDao.selectMeteBlindModel(modelCreator.getModelId());//当前模板绑定的测点信息


         List<Long> m1=new ArrayList<>();
         List<Long> m2=modelCreator.getMeteIds();
         List<Long> m3=new ArrayList<>();


         for(MeteInfo meteInfo1:mete1){
             m1.add(meteInfo1.getMeteId());
         }

        for(MeteInfo meteInfo1:mete1){
            m3.add(meteInfo1.getMeteId());
        }

        if(m2.isEmpty()){
            for(Long id:m3){
                tStdMetemodelDetailDao.deleteByMeteId(id);
            }
        }
         else if (m1.isEmpty()){
            for(Long id:m2){
                TStdMete mete=tStdMeteDao.selectByPrimaryId(id);
                TStdMeteModelDetail detail=new TStdMeteModelDetail();
                detail.setModelId(modelCreator.getModelId());
                detail.setMeteId(mete.getStdMeteId());
                detail.setCustomId("101");
                detail.setMeteName(mete.getMeteName());
                detail.setMeteType(mete.getMeteType());
                detail.setMeteKind(mete.getMeteKind());//插入meteKind
                detail.setUnit(mete.getUnit());
                detail.setAlarmNote(mete.getAlarmNote());
                detail.setAlarmExplain(mete.getAlarmExplain());
                detail.setAlarmType(mete.getAlarmType());
                detail.setUpEffect(mete.getUpEffect());
                detail.setDownEffect(mete.getDownEffect());
                detail.setAlarmLevel(mete.getAlarmLevel());
                detail.setHighLimit1(mete.getHighLimit1());
                detail.setLowLimit1(mete.getLowLimit1());
                detail.setHighLimit2(mete.getHighLimit2());
                detail.setLowLimit2(mete.getLowLimit2());
                detail.setHighLimit3(mete.getHighLimit3());
                detail.setLowLimit3(mete.getLowLimit3());
                detail.setHighLimit4(mete.getHighLimit4());
                detail.setLowLimit4(mete.getLowLimit4());
                detail.setAlarmDelay(mete.getAlarmDelay());
                detail.setAlarmCnt(mete.getAlarmCnt());
                detail.setThresholdAbs(mete.getThresholdAbs());
                detail.setThresholdPer(mete.getThresholdPer());
                detail.setModulus(mete.getModulus());
                detail.setStateZero(mete.getStateZero());
                detail.setStateOne(mete.getStateOne());

                tStdMetemodelDetailDao.add(detail);

            }
        }
        else {
            m1.retainAll(m2); //不变
            if(m2.removeAll(m3) && !m2.isEmpty() ){ //增加
                for(Long id:m2){
                    TStdMete mete=tStdMeteDao.selectByPrimaryId(id);
                    TStdMeteModelDetail detail=new TStdMeteModelDetail();
                    detail.setModelId(modelCreator.getModelId());
                    detail.setMeteId(mete.getStdMeteId());
                    detail.setCustomId("101");
                    detail.setMeteName(mete.getMeteName());
                    detail.setMeteType(mete.getMeteType());
                    detail.setMeteKind(mete.getMeteKind());//插入meteKind
                    detail.setUnit(mete.getUnit());
                    detail.setAlarmNote(mete.getAlarmNote());
                    detail.setAlarmExplain(mete.getAlarmExplain());
                    detail.setAlarmType(mete.getAlarmType());
                    detail.setUpEffect(mete.getUpEffect());
                    detail.setDownEffect(mete.getDownEffect());
                    detail.setAlarmLevel(mete.getAlarmLevel());
                    detail.setHighLimit1(mete.getHighLimit1());
                    detail.setLowLimit1(mete.getLowLimit1());
                    detail.setHighLimit2(mete.getHighLimit2());
                    detail.setLowLimit2(mete.getLowLimit2());
                    detail.setHighLimit3(mete.getHighLimit3());
                    detail.setLowLimit3(mete.getLowLimit3());
                    detail.setHighLimit4(mete.getHighLimit4());
                    detail.setLowLimit4(mete.getLowLimit4());
                    detail.setAlarmDelay(mete.getAlarmDelay());
                    detail.setAlarmCnt(mete.getAlarmCnt());
                    detail.setThresholdAbs(mete.getThresholdAbs());
                    detail.setThresholdPer(mete.getThresholdPer());
                    detail.setModulus(mete.getModulus());
                    detail.setStateZero(mete.getStateZero());
                    detail.setStateOne(mete.getStateOne());

                    tStdMetemodelDetailDao.add(detail);
                }
            }


            if(m3.removeAll(m1) && !m3.isEmpty()){  //删除
                for(Long id:m3){
                    tStdMetemodelDetailDao.deleteByMeteId(id);
                }
            }

        }







         return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public String createModel(){
        Map<String,Object> mapForCreatePath  = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String path = (String) mapForCreatePath.get("content");
        Map<String,Object> mapForReturnPath  = redisTemplate.opsForHash().entries("t_sys_param:meteModelPath");
        String returnPath = (String) mapForReturnPath.get("content");

//        String path = "D:/code/voice";
//        String returnPath = "D:/code/voice";

        String fileName = "meteModel.xls";
        List<String> name = this.tStdMetemodelDetailDao.selectColumnName();
       boolean isOk = createModel(name,fileName,path);
       if(isOk){
           return returnPath+"/"+fileName;
       }else {
         return "fail";
       }
    }
    private  boolean createModel(List<String> list, String modelName, String modelPath) {
        boolean newFile = false;
        //创建excel工作簿
        HSSFWorkbook workbook = new HSSFWorkbook();
        //创建工作表sheet
        HSSFSheet sheet = workbook.createSheet();
        //创建第一行
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell;
        //设置样式
            CellStyle style = workbook.createCellStyle();
            style.setFillForegroundColor(IndexedColors.AQUA.getIndex());

        cell = row.createCell(0);
        cell.setCellStyle(style);
        cell.setCellType(CellType.STRING);
        cell.setCellValue("带*为必填项");
        //插入第一行数据的表头
        String[] deviceType = this.tStdMetemodelDetailDao.selectForDict("device_type").toArray(new String[0]);
        String[] meteType = this.tStdMetemodelDetailDao.selectForDict("mete_type").toArray(new String[0]);
        String[] meteKind = this.tStdMetemodelDetailDao.selectForDict("mete_kind").toArray(new String[0]);
        String[] alarmLevel = this.tStdMetemodelDetailDao.selectForDict("alarm_level").toArray(new String[0]);
        //String[] alarmType = this.tStdMetemodelDetailDao.selectForDict("alarm_type").toArray(new String[0]);
        List<String> anaList = new ArrayList<>();
        anaList.add("analyse_type");
        anaList.add("defect_type");
        String[] analyseType = this.tStdMetemodelDetailDao.selectForDictByanalyseType(anaList).toArray(new String[0]);
        list.remove(list.get(5));//|| i==5 || i= 8 || i==14 || i==25 || i==28 || i==29
        list.remove(list.get(13));//14-1
        list.remove(list.get(23));//25-2
        list.remove(list.get(25));//28-3
        list.remove(list.get(25));//29-4
        list.remove(list.get(6));
            for (int i = 0; i < list.size(); i++) {
                if(i ==0 ){
                    continue;
                }
                cell = row.createCell(i);
                cell.setCellStyle(style);
                if(i == 1){
                    cell.setCellValue("*"+list.get(i));
                    continue;
                }
                if(i == 2){
                    cell.setCellValue(list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(deviceType);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
                if(i == 3){
                    cell.setCellValue(list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(meteType);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
                if(i == 4){
                    cell.setCellValue("*"+list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(meteKind);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
//                if(i == (8-1)){
//                    cell.setCellValue(list.get(i));
//                    // 设置第i列的2-5001行为下拉列表
//                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
//                    // 创建下拉列表数据
//                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(alarmType);
//                    // 绑定
//                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
//                    sheet.addValidationData(dataValidation3);
//                    continue;
//                }
                if(i == (9-1-1)){
                    cell.setCellValue(list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(analyseType);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
                if(i == (13-1-1)){
                    cell.setCellValue(list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(alarmLevel);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
               cell.setCellValue(list.get(i));
            }
        //创建excel文件
            File file = new File(modelPath + File.separator + modelName);
            try {
        //删除该文件夹下原来的模版文件
                deleteDir(new File(modelPath + File.separator+modelName));
        //判断对应的文件夹是否有，无则新建
                File myPath = new File(modelPath);
                if (!myPath.exists()) {
                    myPath.mkdir();
                }
        //创建新的模版文件
                newFile = file.createNewFile();
                //将excel写入
                FileOutputStream stream = FileUtils.openOutputStream(file);
                workbook.write(stream);
                stream.close();
        } catch (IOException e) {
                log.error(e.getMessage(), e);
        }
        return newFile;
    }

    private String excelDataImport(MultipartFile file) {
        Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:tempReflect");
        String path = (String) mapForPicModelPath.get("content");
        //path = "D:/code/qhTest";
        String fileName = "copy-meteModel.xlsx";
        // 将上传文件写入
        try {
            deleteDir(new File(path +"/"+ fileName));
            file.transferTo(new File(path +"/"+ fileName).getAbsoluteFile());
        }catch (NullPointerException | IOException e) {
            log.error(e.getMessage(), e);
        }
        return path +"/"+ fileName;
    }
    @Transactional(rollbackFor = Exception.class)
    public Result insertModel(MultipartFile file)   {
        FileInputStream in = null;
        HSSFWorkbook wb = null;
        Result result = new Result();
        List<Integer> insertRetList = new ArrayList<>();
        try {
            result.setCode(209);
            if(file == null){
                result.setCode(209,"文件错误，文件为null");
                return result;
            }
            String pathName = excelDataImport(file);
            //获取字典表的值pathName
            ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
            tStdMetemodelDetailDao.selectForDictNote(resultHandler);
            List<TStdMete> meteList = tStdMetemodelDetailDao.selectTSTDMeteAll();
            Map<String, String> nameMap = resultHandler.getMappedResults();
            in = new FileInputStream(pathName);
            wb = new HSSFWorkbook(in);//创建工作簿
            Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
            int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始
            List<TStdMete> tStdMeteList = new ArrayList<>();
            StringBuffer errMsg = new StringBuffer();
            String item = null;
            String isRepetition = "";
            if(sheet.getRow(1) == null){
                result.setCode(209,"文件无有效数据");
                return result;
            }
            for (int i = 1; i <= total; i++) {
                Row row = sheet.getRow(i);
                TStdMete tStdMete = new TStdMete();
                //第2列
                if (row.getCell(1) == null || row.getCell(1).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(1).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(1).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "第" + (2) + "列不能为空<br>");
                    result.setMessage(errMsg.toString());
                    return result;
                }
//                //第3列
//                if (row.getCell(2) == null || row.getCell(2).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(2).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(2).getStringCellValue()))) {
//                    errMsg.append("第" + (i + 1) + "行," + "第" + (3) + "列不能为空<br>");
//                    result.setMessage(errMsg.toString());
//                    return result;
//                }
                //第5列
                if (row.getCell(4) == null || row.getCell(4).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(4).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(4).getStringCellValue()))) {
                    errMsg.append("第" + (i + 1) + "行," + "第" + (5) + "列不能为空<br>");
                    result.setMessage(errMsg.toString());
                    return result;
                }
                Cell cell = row.getCell(1);
                if(cell != null) {
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (2) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setDeviceType(Integer.valueOf(nameMap.get(item)));
                }

                cell = row.getCell(2);
                if(cell != null) {
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (3) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setMeteType(nameMap.get(item).toString());
                }

                cell = row.getCell(3);
                if (cell != null) {
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (4) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    Integer meteKind = item.contains("遥信") ? 1 : (item.contains("遥测") ? 2 : (item.contains("遥控") ? 3 : (item.contains("遥调") ? 4 : null)));
                    tStdMete.setMeteKind(meteKind);
                }


                cell = row.getCell(4);
                if (cell != null) {
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (5) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setMeteName(item);
                }


//                cell = row.getCell(5);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (6) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setAlarmNote(item);
//                }


                cell = row.getCell(6-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (7) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setAlarmExplain(item);
                }


//                cell = row.getCell(7-1);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (8) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setAlarmType(nameMap.get(item).toString());
//                }


                cell = row.getCell(8-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (9) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setAnalyseType(Integer.valueOf(nameMap.get(item)));
                }


                cell = row.getCell(9-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (10) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setUnit(item);
                }

                cell = row.getCell(10-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (11) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setUpEffect(Float.valueOf(item));
                }


                cell = row.getCell(11-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (12) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setDownEffect(Float.valueOf(item));
                }


                cell = row.getCell(12-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (13) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setAlarmLevel(Integer.valueOf(nameMap.get(item)));
                }


                cell = row.getCell(13-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (14) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setAlarmLimit(Integer.valueOf(item));
                }


//                cell = row.getCell(14-1);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (15) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setAlarmDelay(Integer.valueOf(item));
//                }

                cell = row.getCell(15-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (16) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setStateZero(item);
                }

                cell = row.getCell(16-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (17) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setStateOne(item);
                }


                cell = row.getCell(17-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (18) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setHighLimit1(Float.valueOf(item));
                }


                cell = row.getCell(18-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (19) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setLowLimit1(Float.valueOf(item));
                }


                cell = row.getCell(19-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (20) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setHighLimit2(Float.valueOf(item));
                }


                cell = row.getCell(20-1-1-1);
                if (cell != null) {
                    //设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (21) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setLowLimit2(Float.valueOf(item));
                }


                cell = row.getCell(21-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (22) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setHighLimit3(Float.valueOf(item));
                }


                cell = row.getCell(22-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (23) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setLowLimit3(Float.valueOf(item));
                }


                cell = row.getCell(23-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (24) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setHighLimit4(Float.valueOf(item));
                }


                cell = row.getCell(24-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (25) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setLowLimit4(Float.valueOf(item));
                }


//                cell = row.getCell(25-1-1);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (26) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setAlarmCnt(Integer.valueOf(item));
//                }


                cell = row.getCell(26-1-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (27) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setThresholdAbs(BigDecimal.valueOf(Long.valueOf(item)));
                }


                cell = row.getCell(27-1-1-1-1);
                if (cell != null) {
//设置单元格类型
                    cell.setCellType(CellType.STRING);
                    item = cell.getStringCellValue();
                    if (checkString(item)) {
                        errMsg.append("第" + (i + 1) + "行," + "第" + (28) + "列含有特殊字符<br>");
                        result.setMessage(errMsg.toString());
                        return result;
                    }
                    tStdMete.setThresholdPer(BigDecimal.valueOf(Long.valueOf(item)));
                }


//                cell = row.getCell(28-1-1-1);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (29) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setModulus(Integer.valueOf(item));
//                }
//
//
//                cell = row.getCell(29-1-1-1);
//                if (cell != null) {
////设置单元格类型
//                    cell.setCellType(CellType.STRING);
//                    item = cell.getStringCellValue();
//                    if (checkString(item)) {
//                        errMsg.append("第" + (i + 1) + "行," + "第" + (30) + "列含有特殊字符<br>");
//                        result.setMessage(errMsg.toString());
//                        return result;
//                    }
//                    tStdMete.setRemark(item);
//                }
                if (isIn(meteList, tStdMete)) {
                    //isRepetition = isRepetition + "," + (i + 1);
                    continue;
                }
                tStdMeteList.add(tStdMete);
                if (tStdMeteList.size() >= 2000) {
                    int ret = tStdMeteDao.batchAdd(tStdMeteList);
                    insertRetList.add(ret);
                    tStdMeteList.clear();
                }
            }
            int isInsert = 0;
            if (tStdMeteList.size() > 0) {
                isInsert = tStdMeteDao.batchAdd(tStdMeteList);
                insertRetList.add(isInsert);
            }

            if (isInsert >= 0) {
                result.setCode(200);
                result.setData(insertRetList);
                result.setMessage("ok");
            }
            deleteDir(new File(pathName));
            return result;
        }catch (Exception e){
            result.setCode(209,"导入文件失败");
            log.error("导入文件失败"+e);
        }finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (wb != null) {
                    wb.close();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return result;
    }

    private boolean isIn(List<TStdMete> meteList,TStdMete tStdMete){
        for (TStdMete item:meteList) {
            if(item.getDeviceType().equals(tStdMete.getDeviceType())
            && item.getMeteName().equals(tStdMete.getMeteName())){
                return true;
            }
        }
        return false;
    }
    private boolean checkString(String str){
        Pattern pattern1 = Pattern.compile(".*[`~!@#$%^&*+=|{}':;',\\[\\]<>?~！@#￥%……&*——+|{}【】‘；：”“’。，、？\\\\]+.*");
        Matcher matcher1 = pattern1.matcher(str);
        if (matcher1.find()) {
            return true;
        }
        return false;
    }

    private boolean isNumber(String str){
        Pattern pattern1 = Pattern.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
        Matcher matcher1 = pattern1.matcher(str);
        if (matcher1.find()) {
            return true;
        }
        return false;
    }

}
