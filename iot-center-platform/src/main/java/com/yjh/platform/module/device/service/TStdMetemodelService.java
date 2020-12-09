package com.yjh.platform.module.device.service;

import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.ResultHandleUtils;
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

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.apache.catalina.startup.ExpandWar.deleteDir;

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

    @Logs(title = "插入", code = "module",content = "根据页面传入的参数新增数据")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.add(tStdMeteModel);
    }

    @Logs(title = "删除", code = "module",content = "根据页面传入的参数删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.deleteByPrimaryId(modelId);
    }

    @Logs(title = "更新", code = "module",content = "根据页面传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.update(tStdMeteModel);
    }

    @Logs(title = "主键查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public TStdMeteModel selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.selectByPrimaryId(modelId);
    }

    @Logs(title = "查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> select(Long modelId, String modelName, Integer deviceType, String remark) {
        return tStdMetemodelDao.select(modelId, modelName, deviceType, remark);
    }

    @Logs(title = "分页查询", code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> selectByPage(TStdMeteModel tStdMeteModel) {
        return tStdMetemodelDao.selectByPage(tStdMeteModel);
    }

    @Logs(title = "批量插入", code = "module",content = "根据页面传入的参数批量插入数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModel> list) {
        return this.tStdMetemodelDao.batchAdd(list);
    }

    @Logs(title = "根据模版的设备类型查询对应的初始测点信息", code = "module",content = "根据页面传入的参数根据模版的设备类型查询对应的初始测点信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectMeteByDeviceType(Integer deviceType) {
        return tStdMetemodelDao.selectMeteByDeviceType(deviceType);
    }

    @Logs(title = "批量插入模版测点", code = "module",content = "根据页面传入的参数批量插入数据")
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

    @Logs(title = "批量删除", code = "module",content = "根据页面传入的参数批量删除数据")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<String> list) {
        return this.tStdMetemodelDao.batchDelete(list);
    }


    @Logs(title = "查询设备类型-模板树", code = "module",content = "根据页面传入的参数查询设备类型-模板树")
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

  @Logs(title = "新建模板联合新增",code = "module",content = "根据页面传入的参数新增数据")
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

          tStdMetemodelDetailDao.add(detail);

      }
        return m.getModelId();
  }

    @Logs(title = "查询当前模板信息",code = "module",content = "根据页面传入的参数查询数据")
    @Transactional(rollbackFor = Exception.class)
    public ModelInfo selectModel(Long modelId){
        ModelInfo mInfo=new ModelInfo();
        TStdMeteModel model=tStdMetemodelDao.selectByPrimaryId(modelId);

       mInfo.setModel(model);


        List<MeteInfo> mete=tStdMetemodelDetailDao.selectMeteBlindModel(modelId);
        mInfo.setMeteInfo(mete);


        return  mInfo;

    }

    @Logs(title = "修改当前模板信息",code = "module",content = "根据页面传入的参数修改数据")
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

    @Logs(title = "创建模板信息",code = "module",content = "创建模板信息")
    @Transactional(rollbackFor = Exception.class)
    public String createModel(){
        Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:temporaryReflect");
        String path = (String) mapForPicModelPath.get("content");
        //String path = "D:/code/qhTest";
        String fileName = "meteModel.xlsx";
        List<String> name = this.tStdMetemodelDetailDao.selectColumnName();
       boolean isOk = createModel(name,fileName,path);
       if(isOk){
           return path+"/"+fileName;
       }else {
         return "fail";
       }
    }
    private   boolean createModel(List<String> list, String modelName, String modelPath) {
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
        String[] alarmType = this.tStdMetemodelDetailDao.selectForDict("alarm_type").toArray(new String[0]);
            for (int i = 0; i < list.size(); i++) {
                if(i ==0 ){
                    continue;
                }
                cell = row.createCell(i);
                cell.setCellStyle(style);
                if(i == 1){
                    cell.setCellValue(list.get(i)+"*");
                }
                if(i == 2){
                    cell.setCellValue(list.get(i)+"*");
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
                    cell.setCellValue(list.get(i)+"*");
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(meteKind);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
                if(i == 8){
                    cell.setCellValue(list.get(i));
                    // 设置第i列的2-5001行为下拉列表
                    CellRangeAddressList regions3 = new CellRangeAddressList(1, 5000, i-1, i-1);
                    // 创建下拉列表数据
                    DVConstraint constraint3 = DVConstraint.createExplicitListConstraint(alarmType);
                    // 绑定
                    HSSFDataValidation dataValidation3 = new HSSFDataValidation(regions3, constraint3);
                    sheet.addValidationData(dataValidation3);
                    continue;
                }
                if(i == 12){
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
            e.printStackTrace();
        }
        return newFile;
    }

    private String excelDataImport(MultipartFile file) {
//        Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:temporaryReflect");
//        String path = (String) mapForPicModelPath.get("content");
        String path = "D:/code/qhTest/66666";
        String fileName = "copy-meteModel.xlsx";
        // 将上传文件写入
        try {
            deleteDir(new File(path +"/"+ fileName));
            file.transferTo(new File(path +"/"+ fileName));
        } catch (IOException e) {
            e.getMessage();
        }
        return path +"/"+ fileName;
    }
    @Logs(title = "测点模板信息导入",code = "module",content = "测点模板信息导入")
    @Transactional(rollbackFor = Exception.class)
    public Result insertModel(MultipartFile file) throws Exception  {
        Result result = new Result();
        String  pathName = excelDataImport(file);
        //获取自带你表的值pathName
        ResultHandleUtils<String, String> resultHandler = new ResultHandleUtils<>();
        tStdMetemodelDetailDao.selectForDictNote(resultHandler);
        List<TStdMete> meteList = tStdMetemodelDetailDao.selectTSTDMeteAll();
        Map<String, String> nameMap = resultHandler.getMappedResults();
        HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(pathName));//创建工作簿
        Sheet sheet = wb.getSheetAt(0);//读取第一个工作表
        int total = sheet.getLastRowNum();//获取最后一行num,即总行数，从0开始
        List<TStdMete> tStdMeteList = new ArrayList<>();
        StringBuffer errMsg = new StringBuffer();
        String item = null;
        String isRepetition = "";
        for (int i = 1; i <= total; i++) {
            Row row = sheet.getRow(i);
            TStdMete tStdMete = new TStdMete();
            //第2列
            if(row.getCell(1) == null || row.getCell(1).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(1).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(1).getStringCellValue()))){
                errMsg.append("第" + (i + 1) + "行," + "第" + (2) + "列不能为空<br>");
                result.setMessage(errMsg.toString());
                return result;
            }
            //第3列
            if(row.getCell(2) == null || row.getCell(2).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(2).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(2).getStringCellValue()))){
                errMsg.append("第" + (i + 1) + "行," + "第" + (3) + "列不能为空<br>");
                result.setMessage(errMsg.toString());
                return result;
            }
            //第5列
            if(row.getCell(4) == null || row.getCell(4).getCellTypeEnum().equals(CellType.BLANK) || (row.getCell(4).getCellTypeEnum().equals(CellType.STRING) && "".equals(row.getCell(4).getStringCellValue()))){
                errMsg.append("第" + (i + 1) + "行," + "第" + (5) + "列不能为空<br>");
                result.setMessage(errMsg.toString());
                return result;
            }
            Cell cell = row.getCell(1);
            //设置单元格类型
            cell.setCellType(CellType.STRING);
            item = cell.getStringCellValue();
            tStdMete.setDeviceType(Integer.valueOf(nameMap.get(item)));

            cell = row.getCell(2);
            //设置单元格类型
            cell.setCellType(CellType.STRING);
            item = cell.getStringCellValue();
            tStdMete.setMeteType(nameMap.get(item).toString());

            cell = row.getCell(3);
            if(cell != null){
                //设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                Integer meteKind = item.contains("遥信")?1:(item.contains("遥测")?2:(item.contains("遥控")?3:(item.contains("遥调")?4:null)));
                tStdMete.setMeteKind(meteKind);
            }


            cell = row.getCell(4);
            if(cell != null){
                //设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setMeteName(item);
            }


            cell = row.getCell(5);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmNote(item);
            }


            cell = row.getCell(6);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmExplain(item);
            }


            cell = row.getCell(7);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmType(nameMap.get(item).toString());
            }


            cell = row.getCell(8);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setUnit(item);
            }


            cell = row.getCell(9);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setUpEffect(Float.valueOf(item));
            }


            cell = row.getCell(10);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setDownEffect(Float.valueOf(item));
            }


            cell = row.getCell(11);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmLevel(Integer.valueOf(nameMap.get(item)));
            }


            cell = row.getCell(12);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmLimit(Integer.valueOf(item));
            }


            cell = row.getCell(13);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmDelay(Integer.valueOf(item));
            }


            cell = row.getCell(14);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setHighLimit1(Float.valueOf(item));
            }


            cell = row.getCell(15);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setLowLimit1(Float.valueOf(item));
            }


            cell = row.getCell(16);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setHighLimit2(Float.valueOf(item));
            }


            cell = row.getCell(17);
            if(cell != null){
                //设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setLowLimit2(Float.valueOf(item));
            }


            cell = row.getCell(18);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setHighLimit3(Float.valueOf(item));
            }


            cell = row.getCell(19);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setLowLimit3(Float.valueOf(item));
            }


            cell = row.getCell(20);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setHighLimit4(Float.valueOf(item));
            }


            cell = row.getCell(21);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setLowLimit4(Float.valueOf(item));
            }


            cell = row.getCell(22);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setAlarmCnt(Integer.valueOf(item));
            }


            cell = row.getCell(23);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setThresholdAbs(BigDecimal.valueOf(Long.valueOf(item)));
            }


            cell = row.getCell(24);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setThresholdPer(BigDecimal.valueOf(Long.valueOf(item)));
            }



            cell = row.getCell(25);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setModulus(Integer.valueOf(item));
            }


            cell = row.getCell(26);
            if(cell != null){
//设置单元格类型
                cell.setCellType(CellType.STRING);
                item = cell.getStringCellValue();
                tStdMete.setRemark(item);
            }
            if(isIn(meteList,tStdMete)){
                isRepetition = isRepetition+","+(i+1);
                continue;
            }
            tStdMeteList.add(tStdMete);
        }
        if(tStdMeteList.size() > 0){
            tStdMeteDao.batchAdd(tStdMeteList);
        }

        if("".equals(isRepetition)){
            result.setData("ok");
        }else {
            isRepetition = "第"+isRepetition.replaceFirst(",","")+"行与数据库数据重复，其他数据已导入";
            result.setData(isRepetition);
        }
        deleteDir(new File(pathName));
        return result;

    }

    private boolean isIn(List<TStdMete> meteList,TStdMete tStdMete){
        for (TStdMete item:meteList) {
            if(item.getDeviceType().equals(tStdMete.getDeviceType())
            && item.getMeteType().equals(tStdMete.getMeteType())
            && item.getMeteName().equals(tStdMete.getMeteName())){
                return true;
            }
        }
        return false;
    }
}
