/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/8/9
 * @since [产品/模块版本] （可选）
 */
public class ImageFile {
    String path;

    public ImageFile(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "ImageFile{" + "path='" + path + '\'' + '}';
    }
}
