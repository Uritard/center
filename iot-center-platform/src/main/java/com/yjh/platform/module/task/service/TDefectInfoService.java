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
    public List<TutHistoryStatistical> countDefectOnMonth() {
        List<String> monthDates = dateTimeUtil.getDayDateList(31);
        System.out.println("时间段是:"+monthDates);
        String firstTime1 = monthDates.get(0);
        String firstTime2 = monthDates.get(1);
        String firstTime3 = monthDates.get(2);
        String firstTime4 = monthDates.get(3);
        String firstTime5 = monthDates.get(4);
        String firstTime6 = monthDates.get(5);
        String firstTime7 = monthDates.get(6);
        String firstTime8 = monthDates.get(7);
        String firstTime9 = monthDates.get(8);
        String firstTime10 = monthDates.get(9);
        String firstTime11 = monthDates.get(10);
        String firstTime12 = monthDates.get(11);
        String firstTime13 = monthDates.get(12);
        String firstTime14 = monthDates.get(13);
        String firstTime15 = monthDates.get(14);
        String firstTime16 = monthDates.get(15);
        String firstTime17 = monthDates.get(16);
        String firstTime18 = monthDates.get(17);
        String firstTime19 = monthDates.get(18);
        String firstTime20 = monthDates.get(19);
        String firstTime21 = monthDates.get(20);
        String firstTime22 = monthDates.get(21);
        String firstTime23 = monthDates.get(22);
        String firstTime24 = monthDates.get(23);
        String firstTime25 = monthDates.get(24);
        String firstTime26 = monthDates.get(25);
        String firstTime27 = monthDates.get(26);
        String firstTime28 = monthDates.get(27);
        String firstTime29 = monthDates.get(28);
        String firstTime30 = monthDates.get(29);
        String firstTime31 = monthDates.get(30);

        Map<String,Integer> map = tDefectInfoDao.countDefectOnMonth(firstTime1,firstTime2,firstTime3,firstTime4,firstTime5,
                firstTime6,firstTime7,firstTime8,firstTime9,firstTime10,
                firstTime11,firstTime12,firstTime13,firstTime14,firstTime15,
                firstTime16,firstTime17,firstTime18,firstTime19,firstTime20,
                firstTime21,firstTime22,firstTime23,firstTime24,firstTime25,
                firstTime26,firstTime27,firstTime28,firstTime29,firstTime30,
                firstTime31);
        List<TutHistoryStatistical> tutHistoryStatisticalList = new ArrayList<>();

        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String timeNode = key.substring(0, 10);
            Number mapValue = (Number) map.get(key);
            TutHistoryStatistical tutHistoryStatistical = new TutHistoryStatistical();
            tutHistoryStatistical.setTimeNode(timeNode);
            tutHistoryStatistical.setCount(mapValue);
            tutHistoryStatisticalList.add(tutHistoryStatistical);
        }
        Collections.sort(tutHistoryStatisticalList, new Comparator<TutHistoryStatistical>() {
            @Override
            public int compare(TutHistoryStatistical o1, TutHistoryStatistical o2) {
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
        System.out.println("tutHistoryStatisticalList是:"+tutHistoryStatisticalList);
        return tutHistoryStatisticalList;
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

