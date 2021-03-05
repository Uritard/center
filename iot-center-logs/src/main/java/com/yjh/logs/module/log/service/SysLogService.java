package com.yjh.logs.module.log.service;

import com.yjh.logs.common.utils.NumToStringUtil;
import com.yjh.logs.module.log.dao.SysLogDao;
import com.yjh.logs.module.log.entity.LongAnalyseDetail;
import com.yjh.logs.module.log.entity.SysLog;
import com.yjh.logs.module.log.entity.SysLogDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @author tt
* @since 2021-01-14
*/
@Service
public class SysLogService {

    @Autowired
    private SysLogDao sysLogDao;

    private Logger log = LoggerFactory.getLogger(SysLogService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(SysLog sysLog) {

        if(Objects.isNull(sysLog)){
            log.error("SysLogService.addOperateLog >>> error: parameter is null.");
            return 1;
        }
        return sysLogDao.insert(sysLog);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long logId) {
        return this.sysLogDao.deleteByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(SysLog sysLog) {
        return this.sysLogDao.update(sysLog);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysLog selectByPrimaryId(Long logId) {
        return this.sysLogDao.selectByPrimaryId(logId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLog> select(Long logId, String logType, String ip, String title, Integer state, String content, Long userId, String userName, String requestOrigin, String requestPath, String requestMethod, Date createTime) {
        List<SysLog> sysLogList = sysLogDao.select(logId, logType, ip, title, state, content, userId, userName, requestOrigin, requestPath, requestMethod, createTime);
        return sysLogList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<SysLogDetail> selectByPage(String userName, String title, Date startTime, Date endTime,String logType) {
        List<SysLogDetail> sysOperateLogList = sysLogDao.selectByPage(userName,title,startTime,endTime,logType);
        for (SysLogDetail item:sysOperateLogList) {
            String type = item.getLogType();
            if(type != null && !"".equals(type)){
                type = NumToStringUtil.findType(type);
                if(type != null){
                    item.setLogType(type);
                }
            }
            String state = item.getState();
            if(state != null && !"".equals(state)){
                if("1".equals(state)){
                    state = "成功";
                }
                if("2".equals(state)){
                    state = "失败";
                }
                if("3".equals(state)){
                    state = "失败";
                }
                item.setState(state);
            }
        }
        return sysOperateLogList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<SysLog> list) {
        return this.sysLogDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,String>> logAnalyze(){
        List<LongAnalyseDetail> listForAna = this.sysLogDao.logAnalyze();
        List<Map<String,String>> listForRe = new ArrayList<>();
        for(int i = 0; i < 14;i++){
            Map<String,String> map = new HashMap<>();
            String count = "0";
            for(LongAnalyseDetail item:listForAna){
                if(String.valueOf(i).equals(item.getLogType())){
                    count = item.getCount();
                }
            }
            map.put("logType",NumToStringUtil.findType(String.valueOf(i)));
            map.put("count",count);
            listForRe.add(map);
        }
        //List<Map<String,Integer>> listForAna = this.sysLogDao.logAnalyze(list);
        return listForRe;
    }
}

