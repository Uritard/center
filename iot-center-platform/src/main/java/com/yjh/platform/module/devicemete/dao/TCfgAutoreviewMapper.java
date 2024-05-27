package com.yjh.platform.module.devicemete.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yjh.platform.module.devicemete.entity.TCfgAutoreview;

import java.util.List;

/**
 * <p>
 * 自动审核配置表 Mapper 接口
 * </p>
 *
 * @author Chenfei
 * @since 2024-05-20
 */
public interface TCfgAutoreviewMapper extends BaseMapper<TCfgAutoreview> {

    List<TCfgAutoreview> selectByPage(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType, String defectType,
        String alarmType, String deviceType, String deviceName, String meteName, String labelAttri);

    List<TCfgAutoreview> selectByPageMini(IPage<TCfgAutoreview> page, String autoreviewName, String autoreviewType, int autoDetailType,
        String typeId, String typeName);

    List<TCfgAutoreview> detailList(String autoreviewType, int autoDetailType, String typeId, String typeName);
}
