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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public Result select(Long upRegionId, int pageNum, int pageSize) {
        Result result = new Result();
        List<Long> list = tStdRegionDao.selectDownId(upRegionId);
        if (CollectionUtils.isNotEmpty(list)) {
            list.add(upRegionId);
        }
        Page page = PageHelper.startPage(pageNum, pageSize, true, null, true);
        List<TMeter> tMeterList = tMeterDao.selectByUpRegionId(list);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("count", page.getTotal());
        resultMap.put("list", tMeterList);
        result.setData(resultMap);
        result.setCode(ResultCodeEnum.NORMAL.getCode());
        return result;
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
        return list.stream().map(e -> {
            TMeterVo tMeterVo = new TMeterVo();
            BeanUtils.copyProperties(e, tMeterVo);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tMeterVo.setCreateTime(sdf.format(e.getCreateTime()));
            return tMeterVo;
        }).collect(Collectors.toList());
    }
}
