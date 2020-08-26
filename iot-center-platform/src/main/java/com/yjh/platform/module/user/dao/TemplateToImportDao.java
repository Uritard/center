package com.yjh.platform.module.user.dao;

import com.yjh.platform.module.device.entity.TStdMeteModel;
import com.yjh.platform.module.device.entity.TStdMeteModelDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author YC
 * @date 2020/8/26 - 10:51
 */
@Repository
public interface TemplateToImportDao {
    //测点模板导入
    int batchUpdateTStdMeteModel(@Param(value = "tStdMeteModelList") List<TStdMeteModel> tStdMeteModelList);
    //测点模板细详细导入
    int batchUpdateTStdMeteModelDetail(@Param(value = "tStdMeteModelDetailList")List<TStdMeteModelDetail> tStdMeteModelDetailList);
    //获取模板表的最后一条modelid
    long selectLastModelId();
}
