package com.yjh.platform.module.device.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.device.entity.DictArea;
import com.yjh.platform.module.device.entity.SipBDictArea;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author YIJIAHE
 * @description 针对表【dict_area(区县行政编码字典表)】的数据库操作Mapper
 * @createDate 2023-09-07 10:46:59
 * @Entity generator.domain.DictArea
 */
public interface DictAreaMapper extends BaseMapper<DictArea> {

    SipBDictArea selectSipBDictArea(
            @Param(value = "id") Long id,
            @Param(value = "code") String code
    );

    List<SipBDictArea> selectSipBDictAreaByType(
            @Param(value = "type") Integer type
    );

    int insertSipBConfig(SipBDictArea sipBDictArea);
    int updateSipBConfig(SipBDictArea sipBDictArea);
}




