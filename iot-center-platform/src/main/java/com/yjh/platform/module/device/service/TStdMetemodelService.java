package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdMeteDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TStdMetemodelDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
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

    @Logs(title = "修改当前模板信息",code = "module",content = "根据页面传入的参数修改数据")
    @Transactional(rollbackFor = Exception.class)
    public String createModel(){
        Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:temporaryReflect");
        String path = (String) mapForPicModelPath.get("content");
        String fileName = "设备测点模板.xlsx";
        List<String> name = this.tStdMetemodelDetailDao.selectColumnName();
       boolean isOk = createModel(name,fileName,path);
       if(isOk){
           return path+fileName;
       }else {
         return "fail";
       }
    }
    public  boolean createModel(List<String> list, String modelName, String modelPath) {
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
                    cell.setCellValue(list.get(i)+"*");
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
                    cell.setCellValue(list.get(i)+"*");
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
                    cell.setCellValue(list.get(i)+"*");
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
                deleteDir(new File(modelPath + File.separator));
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

}
