package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TDefectInfoDao;
import com.yjh.platform.module.task.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
* @author tt
* @since 2020-10-15
*/
@Service
public class TDefectInfoService {

    @Autowired
    private TDefectInfoDao tDefectInfoDao;
    private DateTimeUtil dateTimeUtil;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TDefectInfo tDefectInfo) {
        return this.tDefectInfoDao.insert(tDefectInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long defectId) {
        return this.tDefectInfoDao.deleteByPrimaryId(defectId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TDefectInfo tDefectInfo) {
        return this.tDefectInfoDao.update(tDefectInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TDefectInfo selectByPrimaryId(Long defectId) {
        return this.tDefectInfoDao.selectByPrimaryId(defectId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDefectInfo> select(Long defectId, Integer defectLevel, Date defectTime, Integer defectType, String defectName, String defectContent, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Integer isDefect, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, Integer ifDefectDisable, Integer alarmSource, Integer defectSubtype, String deviceCode, String imagePath, String videoPath, String value, String outRange, String linkMessage) {
        List<TDefectInfo> tDefectInfoList = tDefectInfoDao.select(defectId, defectLevel, defectTime, defectType, defectName, defectContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isDefect, dealType, dealInfo, dealPersonId, dealTime, ifDefectDisable, alarmSource, defectSubtype, deviceCode, imagePath, videoPath, value, outRange, linkMessage);
        return tDefectInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDefectInfo> selectByPage(TDefectInfo tDefectInfo) {
        List<TDefectInfo> tDefectInfoList = tDefectInfoDao.selectByPage(tDefectInfo);
        return tDefectInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TDefectInfo> list) {
        return this.tDefectInfoDao.batchInsert(list);
    }
    @Logs(title = "查询所有缺陷", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDefectInfoDetail> selectDefectByPage(Integer confMode, Integer defectType, Date startTime, Date endTime, String deviceName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("confMode", confMode);
        map.put("defectType", defectType);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        return tDefectInfoDao.selectAllDefect(map);
    }
    @Logs(title = "统计近一月的所有缺陷个数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> countDefectOnMonth() {
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        List<WarnStatistical> list = tDefectInfoDao.countDefectOnMonth(startTime,endTime);
        List<WarnStatistical> list1 = new ArrayList<>();

        for (int i = 0;i < monthDates.size();i++){
            int flag = 0;
            for (int j = 0;j < list.size();j++){
                if (monthDates.get(i).substring(0, 10).equals(list.get(j).getTimeNode())){
                    flag++;
                }
            }
            if (flag == 0){
                WarnStatistical ws = new WarnStatistical();
                ws.setCount(0);
                ws.setTimeNode(monthDates.get(i).substring(0, 10));
                list1.add(ws);
            }
        }
        list.addAll(list1);
        Collections.sort(list, new Comparator<WarnStatistical>() {
            @Override
            public int compare(WarnStatistical o1, WarnStatistical o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = simpleDateFormat.parse(o1.getTimeNode());
                    date2 = simpleDateFormat.parse(o2.getTimeNode());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int flag = date1.compareTo(date2);
                if (flag == -1) {
                    flag = -1;
                } else if (flag == 1) {
                    flag = 1;
                }
                return flag;
            }
        });
        return list;
    }
    @Logs(title = "根据缺陷处理状态统计缺陷个数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countDefectConfMode() {
        Map<String, Integer> map = tDefectInfoDao.countDefectConfMode();
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue =  (Number)map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setTJContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Logs(title = "查看缺陷处理情况", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDefectInfoDetail> selectDefectProcess(Long defectId) {
        return tDefectInfoDao.selectDefectProcess(defectId);
    }
    @Logs(title = "进行缺陷处理", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int defectProcess(Long defectId,Integer dealType,String dealInfo) {

        TDefectInfo tDefectInfo = new TDefectInfo();
        tDefectInfo.setDefectId(defectId);
        tDefectInfo.setDealInfo(dealInfo);
        tDefectInfo.setDealType(dealType);
        int jieGuo = tDefectInfoDao.update(tDefectInfo);

        return jieGuo;
    }
}

