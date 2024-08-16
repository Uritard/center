package com.yjh.platform.module.device.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yjh.platform.module.device.dao.DictAreaMapper;
import com.yjh.platform.module.device.entity.DictArea;
import com.yjh.platform.module.device.entity.SipBDictArea;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author YIJIAHE
 * @description 针对表【dict_area(区县行政编码字典表)】的数据库操作Service实现
 * @createDate 2023-09-07 10:46:59
 */
@Service
public class DictAreaService extends ServiceImpl<DictAreaMapper, DictArea> {

    @Resource
    private DictAreaMapper dictAreaMapper;

    public List<DictArea> getDictAreaByParentId(String parentId) {
        LambdaQueryWrapper<DictArea> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DictArea::getParentId, parentId);
        return dictAreaMapper.selectList(lambdaQueryWrapper);
    }


    public SipBDictArea selectSipBDictArea(Long id, String code) {
        return dictAreaMapper.selectSipBDictArea(id, code);
    }

    public List<SipBDictArea> selectSipBDictAreaByType(Integer type) {
        return dictAreaMapper.selectSipBDictAreaByType(type);
    }

    public Map<String, String> getSipBConfig() {
        Map<String, String> re = new HashMap<>(8);
        List<SipBDictArea> GB = selectSipBDictAreaByType(666);
        if (!GB.isEmpty()) {
            SipBDictArea GBConfig = GB.get(0);
            String GBCode = GBConfig.getCode();//国标编码
            String GBACode = GBCode.substring(0, 6);//区编码
            DictArea city = dictAreaMapper.selectById(Long.valueOf(GBACode));
            String GBCCode = city.getParentId().toString();//市编码
            DictArea province = dictAreaMapper.selectById(Long.valueOf(GBCCode));
            String GBPCode = province.getParentId().toString();//省编码
            String GBHyCode = GBCode.substring(8, 10);//行业编码
            String GBTypeCode = GBCode.substring(10, 13);//类型编码

            re.put("GBCode", GBCode);
            re.put("GBPCode", GBPCode);
            re.put("GBCCode", GBCCode);
            re.put("GBACode", GBACode);
            re.put("GBHyCode", GBHyCode);
            re.put("GBTypeCode", GBTypeCode);
        }
        List<SipBDictArea> SipB = selectSipBDictAreaByType(777);
        if (!SipB.isEmpty()) {
            SipBDictArea sipBConfig = SipB.get(0);
            String SipBCode = sipBConfig.getCode();
            String SipBPCode = SipBCode.substring(0, 2);
            String SipBTypeCode = SipBCode.substring(10, 14);


            re.put("SipBCode", SipBCode);
            re.put("SipBPCode", SipBPCode);
            re.put("SipBTypeCode", SipBTypeCode);
        }

        return re;
    }

    public int saveConfig(SipBDictArea sipBDictArea) {
        List<SipBDictArea> old = selectSipBDictAreaByType(sipBDictArea.getType());
        if (old.isEmpty()) {
            return dictAreaMapper.insertSipBConfig(sipBDictArea);
        } else {
            return dictAreaMapper.updateSipBConfig(sipBDictArea);
        }
    }
}




