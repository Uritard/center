/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.UPatrolDataResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTaskAttr;
import org.apache.ibatis.cursor.Cursor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/10/17
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface CruiseUpPatrolDao {

    Cursor<UPatrolTaskAttr> selectPatrolCursor();

    Cursor<UPatrolResult> selectResultCursor();

    Cursor<UPatrolDataResult> selectPatrolDetailCursor();
}
