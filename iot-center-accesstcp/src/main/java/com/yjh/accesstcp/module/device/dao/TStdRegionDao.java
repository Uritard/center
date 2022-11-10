package com.yjh.accesstcp.module.device.dao;

import com.yjh.accesstcp.module.device.entity.TStdRegion;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author tt
 * @since 2020-07-27
 */
@Repository
public interface TStdRegionDao {
    List<TStdRegion> selectAll();

}
