package com.yjh.platform.module.device.service;

import com.yjh.platform.module.device.entity.TDeviceTypeImg;
import com.yjh.platform.module.device.dao.TDeviceTypeImgDao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import com.yjh.platform.module.user.dao.TDictBusinessDao;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TDictBusiness;
import com.yjh.platform.module.user.entity.TSysParam;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2021-03-11
*/
@Service
public class TDeviceTypeImgService{

    @Autowired
    private TDeviceTypeImgDao tDeviceTypeImgDao;
    @Autowired
    private TDictBusinessDao tDictBusinessDao;
    @Autowired
    private TSysParamDao tSysParamDao;

    //@Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int add(TDeviceTypeImg tDeviceTypeImg) {
        return this.tDeviceTypeImgDao.add(tDeviceTypeImg);
    }

    //@Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String typeId) {
        return this.tDeviceTypeImgDao.deleteByPrimaryId(typeId);
    }

    //@Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDeviceTypeImg tDeviceTypeImg) {
        return this.tDeviceTypeImgDao.update(tDeviceTypeImg);
    }

    //@Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TDeviceTypeImg selectByPrimaryId(String typeId) {
        return this.tDeviceTypeImgDao.selectByPrimaryId(typeId);
    }

    //@Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceTypeImg> select(String typeId, String picAbsPath, String picRealPath, String remake) {
        List<TDeviceTypeImg> tDeviceTypeImgList = tDeviceTypeImgDao.select(typeId, picAbsPath, picRealPath, remake);
        return tDeviceTypeImgList;
    }

    //@Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDeviceTypeImg> selectByPage(String typeId, String picAbsPath, String picRealPath, String remake) {
        List<TDeviceTypeImg> tDeviceTypeImgList = tDeviceTypeImgDao.selectByPage(typeId, picAbsPath, picRealPath, remake);
        return tDeviceTypeImgList;
    }

    //@Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchAdd(List<TDeviceTypeImg> list) {
        return this.tDeviceTypeImgDao.batchAdd(list);
    }

    //@Logs(title = "批量删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchDelete(String typeId) {
    List<String> list1= Arrays.asList(typeId.split(","));
    return this.tDeviceTypeImgDao.batchDelete(list1);
    }

    //将图片文件写入数据库
    @Transactional(rollbackFor = Exception.class)
    public int findPic(){
        List<String> colName = new ArrayList<>();
        List<TDeviceTypeImg> inList = new ArrayList<>();
        colName.add("device_type");
        List<TDictBusiness> list = tDictBusinessDao.selectQuery(colName);
        String realPath = tSysParamDao.selectByParamType("deviceTypeImgReaPath").getContent();
        String absPath  = tSysParamDao.selectByParamType("deviceTypeImgAbsPath").getContent();
//        String realPath = "D:/code/qhTest/deviceTypeImage";
//        String absPath  = "D:/code/qhTest/deviceTypeImage";
        for(TDictBusiness tDictBusiness : list){
            TDeviceTypeImg tDeviceTypeImg = new TDeviceTypeImg();
            tDeviceTypeImg.setTypeId(tDictBusiness.getDictCode());
            File file = new File(absPath+"/"+tDictBusiness.getDictCode());
            File[] picList = file.listFiles();
            if(picList == null || picList.length <1){
                continue;
            }
            tDeviceTypeImg.setPicAbsPath(absPath+"/"+tDictBusiness.getDictCode()+"/"+picList[0].getName());
            tDeviceTypeImg.setPicRealPath(realPath+"/"+tDictBusiness.getDictCode()+"/"+picList[0].getName());
            inList.add(tDeviceTypeImg);
        }
        if(inList != null && inList.size()>0){
            this.tDeviceTypeImgDao.deleteAll();
            return this.batchAdd(inList);
        }
        return -1;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>>selectDeviceTypeAndImg(){
        return this.tDeviceTypeImgDao.selectDeviceTypeAndImg();
    }

}

