package com.yjh.gateway.module.gateway.dao;

import com.yjh.gateway.module.gateway.entity.Route;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by tt on 2019/6/4.
 */
@Repository
public interface RouteDao {
    List<Route> selectAll();
}
