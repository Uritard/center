package com.yjh.gateway.module.dao;

import com.yjh.gateway.module.entity.Route;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by tt on 2019/6/3.
 */
@Repository
public interface RouteDao {
    List<Route> selectAll();
}
