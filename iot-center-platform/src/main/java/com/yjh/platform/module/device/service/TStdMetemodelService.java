package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.dao.TStdDeviceDao;
import com.yjh.platform.module.device.dao.TStdMeteDao;
import com.yjh.platform.module.device.dao.TStdMetemodelDetailDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.device.dao.TStdMetemodelDao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.add(tStdMeteModel);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.deleteByPrimaryId(modelId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TStdMeteModel tStdMeteModel) {
        return this.tStdMetemodelDao.update(tStdMeteModel);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TStdMeteModel selectByPrimaryId(Long modelId) {
        return this.tStdMetemodelDao.selectByPrimaryId(modelId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> select(Long modelId, String modelName, Integer deviceType, String remark) {
        return tStdMetemodelDao.select(modelId, modelName, deviceType, remark);
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModel> selectByPage(TStdMeteModel tStdMeteModel) {
        return tStdMetemodelDao.selectByPage(tStdMeteModel);
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TStdMeteModel> list) {
        return this.tStdMetemodelDao.batchAdd(list);
    }

    @Logs(title = "根据模版的设备类型查询对应的初始测点信息", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TStdMeteModelDetail> selectMeteByDeviceType(Integer deviceType) {
        return tStdMetemodelDao.selectMeteByDeviceType(deviceType);
    }

    @Logs(title = "批量插入模版测点", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAddModelMete(List<TStdMeteModelDetail> list) {
        Long modelId = list.get(0).getModelId();
        List<String> deviceList = tStdDeviceDao.selectByModelId(modelId);
        if (deviceList.size() > 0) {
            return 206;
        } else {
            tStdMetemodelDetailDao.deleteByPrimaryId(modelId);
            return this.tStdMetemodelDetailDao.batchAdd(list);
        }
    }

    @Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(List<String> list) {
        return this.tStdMetemodelDao.batchDelete(list);
    }


    @Logs(title = "查询设备类型-模板树", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<DeviceTypeTree> selectDeviceTypeModelTree() {
        List<MeteModel> list1 = tStdMetemodelDao.selectModel();
        List<DeviceTypeTree> list2 = tStdMetemodelDao.selectDevice();
        for (int i = 0; i < list2.size(); i++) {
            List<MeteModel> list=new ArrayList<>();
            for (int j = 0; j < list1.size(); j++) {

                if (list2.get(i).getDeviceType()==list1.get(j).getDeviceType()){

                   list.add(list1.get(j));
                   list2.get(i).setChildren(list);
                }


            }

        }
        return list2;
    }

  @Logs(title = "新建模板联合新增",code = "module")
  @Transactional(rollbackFor = Exception.class)
  public int addModel(ModelCreator modelCreator){
        TStdMeteModel m=new TStdMeteModel();
        m.setDeviceType(modelCreator.getDeviceType());
        m.setModelName(modelCreator.getModel_name());
        tStdMetemodelDao.add(m);
        Long modelId=m.getModelId();
      for (Long id:modelCreator.getMeteIds()) {

          TStdMete mete=tStdMeteDao.selectByPrimaryId(id);
          TStdMeteModelDetail detail=new TStdMeteModelDetail();
          detail.setModelId(modelId);
          detail.setMeteId(mete.getStdMeteId());
          detail.setMeteCode(mete.getMeteCode());
          detail.setMeteName(mete.getMeteName());
          detail.setMeteType(mete.getMeteType());
          detail.setUnit(mete.getUnit());
          detail.setAlarmNote(mete.getAlarmNote());
          detail.setAlarmExplain(mete.getAlarmExplain());
          detail.setAlarmType(mete.getAlarmType());
          detail.setUpEffect(mete.getUpEffect());
          detail.setLowEffect(mete.getLowEffect());
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
        return 1;
  }

    @Logs(title = "查询当前模板信息",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public ModelInfo selectModel(Long modelId){
        ModelInfo mInfo=new ModelInfo();
        TStdMeteModel model=tStdMetemodelDao.selectByPrimaryId(modelId);
        mInfo.setModel(model);
        List<MeteInfo> mete=tStdMetemodelDetailDao.selectMeteBlindModel(modelId);
        mInfo.setMeteInfo(mete);

        return  mInfo;

    }

    @Logs(title = "修改当前模板信息",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int updateModel(ModelCreator modelCreator){

        //修改模板名
        TStdMeteModel meteModel=tStdMetemodelDao.selectByPrimaryId(modelCreator.getModelId());
        meteModel.setModelName(modelCreator.getModel_name());
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
        }else {
            m1.retainAll(m2); //不变
            if(m2.removeAll(m3) && !m2.isEmpty() ){ //增加
                for(Long id:m2){
                    TStdMete mete=tStdMeteDao.selectByPrimaryId(id);
                    TStdMeteModelDetail detail=new TStdMeteModelDetail();
                    detail.setModelId(modelCreator.getModelId());
                    detail.setMeteId(mete.getStdMeteId());
                    detail.setMeteCode(mete.getMeteCode());
                    detail.setMeteName(mete.getMeteName());
                    detail.setMeteType(mete.getMeteType());
                    detail.setUnit(mete.getUnit());
                    detail.setAlarmNote(mete.getAlarmNote());
                    detail.setAlarmExplain(mete.getAlarmExplain());
                    detail.setAlarmType(mete.getAlarmType());
                    detail.setUpEffect(mete.getUpEffect());
                    detail.setLowEffect(mete.getLowEffect());
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

}
