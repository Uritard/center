/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessvideo.hik;

import com.sun.jna.Structure;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/11
 * @since [产品/模块版本] （可选）
 */
public class StructureFieldOrder extends Structure {
    @Override
    protected List<String> getFieldOrder() {
        Field[] fields = this.getClass().getFields();
        List<String> flist = new ArrayList<>();

        for(int i = 0; i < fields.length; ++i) {
            int idx = fields[i].getModifiers();
            if (!Modifier.isStatic(idx) && Modifier.isPublic(idx)) {
                flist.add(fields[i].getName());
            }
        }

        return flist;
    }
}
