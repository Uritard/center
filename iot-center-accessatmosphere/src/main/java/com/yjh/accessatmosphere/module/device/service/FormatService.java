package com.yjh.accessatmosphere.module.device.service;

import com.yjh.accessatmosphere.common.Constant;
import com.yjh.accessatmosphere.module.device.entity.Format;
import com.yjh.accessatmosphere.module.device.dao.FormatDao;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.accessatmosphere.commons.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class FormatService{

    @Autowired
    private FormatDao formatDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(Format format) {
        return this.formatDao.insert(format);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String testId) {
        return this.formatDao.deleteByPrimaryId(testId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(Format format) {
        return this.formatDao.update(format);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Format selectByPrimaryId(String testId) {
        return this.formatDao.selectByPrimaryId(testId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Format> select(String testId, String testType) {
        List<Format> formatList = formatDao.select(testId, testType);
        return formatList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<Format> selectByPage(Format format) {
        List<Format> formatList = formatDao.selectByPage(format);
        return formatList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<Format> list) {
        return this.formatDao.batchInsert(list);
    }



    @Logs(title = "获取微气象数据", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public Map<String,Object> saveInfo(){
        String data = Constant.weatherInfo;
        if("".equals(data)){
            return null;
        }
        DecimalFormat df = new DecimalFormat("#0.000000");
        String[] str = data.split(",");
        Map<String,Object> info = new HashMap<>();
        String windDirection = str[2];//风向 Dn=000D

        String windSpeed = str[5];//风速 Sm=000.0M
        windSpeed =windSpeed.replaceAll("Sm=","").replaceAll("M","");
        Double temp = Double.valueOf(windSpeed);
        info.put("windSpeed",df.format(temp).toString()+"m/s");

        String temperature = str[7].replaceAll("Ta=","").replaceAll("C","");//大气温度 Ta=021.2C
        temp = Double.valueOf(temperature);
        info.put("temperature",df.format(temp).toString()+"℃");

        String humidity = str[8].replaceAll("Ua=","").replaceAll("P","");//湿度 Ua=015.7P
        temp = Double.valueOf(humidity);
        info.put("humidity",df.format(temp).toString()+"%RH");

        String airPressure = str[9].replaceAll("Pa=","").replaceAll("H","");//气压 Pa=0.001022.4H;
        temp = Double.valueOf(airPressure);
        info.put("airPressure",df.format(temp).toString()+"hPa");

        String precipitation = str[10].replaceAll("Rc=","").replaceAll("M","");//降雨量 Rc=0000.0M
        temp = Double.valueOf(precipitation);
        info.put("precipitation",df.format(temp).toString()+"mm");
        //Constant.weatherInfo = info;

        return info;
    }

}

