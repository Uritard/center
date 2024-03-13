package com.yjh.platform.module.device.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TMeterDao;
import com.yjh.platform.module.device.dao.TMeterLogDao;
import com.yjh.platform.module.device.dao.TStdRegionDao;
import com.yjh.platform.module.device.entity.TMeter;
import com.yjh.platform.module.device.entity.TMeterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/10/21
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class TMeterService {
    @Autowired
    @Qualifier("serviceRestTemplate")
    private RestTemplate serviceRestTemplate;

    @Autowired
    private TMeterDao tMeterDao;
    @Resource
    private TMeterLogDao tMeterLogDao;
    @Autowired
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Autowired
    private TStdRegionDao tStdRegionDao;

    public Result select(Long upRegionId, String meterName,int pageNum, int pageSize) {
        Result result = new Result();
        List<Long> list = tStdRegionDao.selectDownId(upRegionId);
        if (CollectionUtils.isNotEmpty(list)) {
            list.add(upRegionId);
        }
        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<TMeter> tMeterList = tMeterDao.selectByUpRegionId(list,meterName);
        tMeterList.forEach(t -> {
            //获取该电表最近的一次数据 (昨天的)耗电量 =  采集时间的第一次采集数据 - 采集时间前一天的第一次采集数据
            TMeter log = tMeterLogDao.selectPowerDifferenceValue(t.getId());
            setPower(t, log, true);
        });
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("count", page.getTotal());
        resultMap.put("list", tMeterList);
        result.setData(resultMap);
        result.setCode(ResultCodeEnum.NORMAL.getCode());
        return result;
    }

    public void setPower(TMeter tMeter, TMeter log, Boolean flag) {
        int mc = StringUtils.isNotBlank(tMeter.getMagnificationCoefficient()) ? Integer.parseInt(tMeter.getMagnificationCoefficient()) : 1;
        //正向有功 采集值
        tMeter.setTotalPositivePower(getMeterRealNum(tMeter.getTotalPositivePower(), 1, flag, 1));
        //正向有功 耗电量
        tMeter.setTotalPositivePowerLast(getMeterRealNum(Objects.nonNull(log) ? log.getTotalPositivePowerDifferenceValue() : "", mc, flag, 1));
        //正向无功 采集值
        tMeter.setTotalPositiveReactivePower(getMeterRealNum(tMeter.getTotalPositiveReactivePower(), 1, flag, 2));
        //正向无功 耗电量
        tMeter.setTotalPositiveReactivePowerLast(getMeterRealNum(Objects.nonNull(log) ? log.getTotalPositiveReactivePowerDifferenceValue() : "", mc, flag, 2));
        //反向无功 采集值
        tMeter.setTotalNegativePositivePower(getMeterRealNum(tMeter.getTotalNegativePositivePower(), 1, flag, 2));
        //反向无功 耗电量
        tMeter.setTotalNegativePositivePowerLast(getMeterRealNum(Objects.nonNull(log) ? log.getTotalNegativePositivePowerDifferenceValue() : "", mc, flag, 2));
    }
    /**
     *
     * @param value 值  空值转为 0
     * @param mc 倍率
     * @param flag ture 加单位  false不加单位
     * @param type 单位类型 1 kwh  2 kvarh
     * @return
     */
    public String getMeterRealNum(String value, int mc, Boolean flag, int type) {
        Double num = StringUtils.isNotBlank(value) ? Double.parseDouble(value) : 0;
        num = num * mc;
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        String res;
        if (flag) {
            String unit = type == 1 ? "kwh" : "kvarh";
            res = decimalFormat.format(num) + unit;
        } else {
            res = decimalFormat.format(num);
        }
        return res;
    }

    public int deleteByPrimaryKey(Long id) {
        int result = tMeterDao.deleteByPrimaryKey(id);
        threadPoolTaskExecutor.execute(() -> {
            Result result1 = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/delete?id={0}", Result.class, id);
            log.info("删除电表 result:{}", result1);
        });
        return result;
    }

    public int insert(TMeter record) {
        int result = tMeterDao.insert(record);
        threadPoolTaskExecutor.execute(() -> {
            Result result1 = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/add?id={0}", Result.class, record.getId());
            log.info("新增电表 result:{}", result1);
        });
        return result;
    }

    public int update(TMeter tMeter) {
        int result = tMeterDao.update(tMeter);
        threadPoolTaskExecutor.execute(() -> {
            Result result1 = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/add?id={0}", Result.class, tMeter.getId());
            log.info("新增电表 result:{}", result1);
        });
        return result;
    }

    public Result collectData(Long id) {
        Result result = serviceRestTemplate.getForObject(Constant.SEND_METER_URL + "/collect?id={0}", Result.class, id);
        log.info("采集电量 result:{}", result);
        return result;
    }

    public List<TMeterVo> list(TMeter tMeter) {
        List<TMeter> list = tMeterLogDao.list(tMeter);
        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        list.forEach(t -> setPower(t, t, false));
        return list.stream().map(e -> {
            TMeterVo tMeterVo = new TMeterVo();
            BeanUtils.copyProperties(e, tMeterVo);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tMeterVo.setCreateTime(sdf.format(e.getCreateTime()));
            return tMeterVo;
        }).collect(Collectors.toList());
    }
}
