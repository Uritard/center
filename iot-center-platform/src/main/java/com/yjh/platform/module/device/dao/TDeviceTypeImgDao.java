package com.yjh.platform.module.device.dao;

import java.util.List;
import java.util.Map;

import com.yjh.platform.module.device.entity.TDeviceTypeImg;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author lqh
 * @since 2021-03-11
 */
@Repository
public interface TDeviceTypeImgDao {

    int add(TDeviceTypeImg tDeviceTypeImg);
    int deleteByPrimaryId(@Param(value = "typeId") String typeId);
    int update(TDeviceTypeImg tDeviceTypeImg);
    TDeviceTypeImg selectByPrimaryId(@Param(value = "typeId") String typeId);
    List<TDeviceTypeImg> select(@Param(value = "typeId") String typeId,
                                @Param(value = "picAbspath") String picAbspath,
                                @Param(value = "picRealpath") String picRealpath,
                                @Param(value = "remake") String remake);
    List<TDeviceTypeImg> selectByPage(@Param(value = "typeId") String typeId,
                                    @Param(value = "picAbspath") String picAbspath,
                                    @Param(value = "picRealpath") String picRealpath,
                                    @Param(value = "remake") String remake);

    int batchAdd(List<TDeviceTypeImg> list);
    int batchDelete(List<String> list);
    int deleteAll();
    List<Map<String,String>>selectDeviceTypeAndImg();
}
