package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.SysOrg;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;

/**
* @author tt
* @since 2020-08-06
*/
@Service
public class TAlgorithmInfoService{

    @Autowired
    private TAlgorithmInfoDao tAlgorithmInfoDao;

    private final static Map<String, TAlgorithmInfo> ALGORITHM_INFO_MAP = new HashMap<>(64);

    @PostConstruct
    public void init(){
        algorithmDefectInfoRefresh ();
    }

    @Transactional(rollbackFor = Exception.class)
    public int insert(TAlgorithmInfo tAlgorithmInfo) {
        int i = this.tAlgorithmInfoDao.insert(tAlgorithmInfo);
        algorithmDefectInfoRefresh ();
        return i;
    }
    public boolean judgeAnalyseType(String analyseType) {
        boolean flag = false;
        List<TAlgorithmInfo> tAlgorithmInfoList = this.tAlgorithmInfoDao.select(null, null,null,null,null, analyseType, null,null);
        if (tAlgorithmInfoList.size()>0) {
            flag=true;
        }
        return flag;
    }
    public boolean judgeDefectType(Integer defectType) {
        boolean flag = false;
        List<TAlgorithmInfo> tAlgorithmInfoList = this.tAlgorithmInfoDao.select(null, null,null,null,null, null, null,defectType);
        if (tAlgorithmInfoList.size()>0) {
            flag=true;
        }
        return flag;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long algorithmId) {
        {//算法已配置了预置位
            List<Long> list = tAlgorithmInfoDao.selectHaveDevice(algorithmId);
            if(list!= null && list.size()>0){
                return -1;
            }
        }
        return this.tAlgorithmInfoDao.deleteByPrimaryId(algorithmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TAlgorithmInfo tAlgorithmInfo) {
        int i = this.tAlgorithmInfoDao.update(tAlgorithmInfo);
        algorithmDefectInfoRefresh ();
        return i;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<String> selectAllAnalyseType() {
        return tAlgorithmInfoDao.selectAllAnalyseType();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<Integer> selectAllDefectType() {
        return tAlgorithmInfoDao.selectAllDefectType();
    }
    @Transactional(rollbackFor = Exception.class)
    public String selectAnalyseById(Long algorithmId) {
        return tAlgorithmInfoDao.selectAnalyseById(algorithmId);
    }
    @Transactional(rollbackFor = Exception.class)
    public Integer selectDefectById(Long algorithmId) {
        return tAlgorithmInfoDao.selectDefectById(algorithmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TAlgorithmInfo selectByPrimaryId(Long algorithmId) {
        return this.tAlgorithmInfoDao.selectByPrimaryId(algorithmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> select(Long algorithmId, String algorithmName, String aliasName, String describel, String algorithmCode, String analyseType,Integer isAi,Integer defectType) {
        if("-1".equals(analyseType)){
            analyseType = null;
        }
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.select(algorithmId, algorithmName, aliasName, describel, algorithmCode, analyseType,isAi,defectType);
        return tAlgorithmInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TAlgorithmInfo> selectByPage(TAlgorithmInfo tAlgorithmInfo) {
        List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.selectByPage(tAlgorithmInfo);
        return tAlgorithmInfoList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TAlgorithmInfo> list) {
        for(TAlgorithmInfo item:list){
            List<Long> listHave = tAlgorithmInfoDao.selectHaveDevice(item.getAlgorithmId());
            if(listHave!= null && listHave.size()>0){
                return -1;
            }
        }
        int i = this.tAlgorithmInfoDao.batchInsert(list);
        algorithmDefectInfoRefresh ();
        return i;
    }

    public void algorithmDefectInfoRefresh() {
        List<TAlgorithmInfo> tAlgorithmInfoList = this.tAlgorithmInfoDao.select(null, null, null, null, null, "398", null, null);
        if (tAlgorithmInfoList.size() > 0) {
            ALGORITHM_INFO_MAP.put("abnormal", new TAlgorithmInfo().setAlgorithmName("图像有差异").setAliasName("abnormal").setDefectLevel(130));
            tAlgorithmInfoList.forEach(info -> {
                ALGORITHM_INFO_MAP.put(info.getAliasName(), info);
            });
        }
    }

    public String getDefectName(String aliasName) {
        return getDefectName(aliasName, "不支持异常类型(" + aliasName +")");
    }

    public String getDefectName(String aliasName, String def) {
        TAlgorithmInfo info = ALGORITHM_INFO_MAP.get(aliasName);
        String name = def;
        if (info != null && StringUtils.isNotEmpty(info.getAlgorithmName())) {
            name = info.getAlgorithmName();
        }
        return name;
    }

    public TAlgorithmInfo getDefectInfo(String aliasName) {
        return ALGORITHM_INFO_MAP.get(aliasName);
    }
}

