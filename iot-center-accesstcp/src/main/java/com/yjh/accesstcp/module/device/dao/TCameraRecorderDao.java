package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TCameraRecorder;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yanhao
 * @description 针对表【t_camera_recorder(录像服务器表)】的数据库操作Mapper
 * @createDate 2022-11-08
 * @Entity com.yjh.accesstcp.module.device.entity.TCameraRecorder
 */
@Repository
public interface TCameraRecorderDao {

    int deleteByPrimaryKey(Long id);

    int insert(TCameraRecorder record);

    int insertSelective(TCameraRecorder record);

    TCameraRecorder selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TCameraRecorder record);

    int updateByPrimaryKey(TCameraRecorder record);

    List<TCameraRecorder> selectAll();

}
