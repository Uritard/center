/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.manager.module.gateway.dao;

import com.yjh.manager.module.gateway.entity.Route;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by tt on 2019/6/4.
 */
@Repository
public interface RouteDao {
    List<Route> selectAll();
}
