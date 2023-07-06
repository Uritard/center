/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.config.service;

import cn.hutool.core.io.FileUtil;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.util.Date;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/7/4
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class FileProcess implements Closeable {
    private Date expiryDate;
    private boolean needBack;
    private String folderPath;
    private String backPath;
    private String parentSub;

    private int level = Deflater.BEST_SPEED;
    private long truncateSize = 0L;

    private volatile boolean started = false;
    private ZipOutputStream zos;
    private WritableByteChannel zosByteChannel;

    public FileProcess(Date expiryDate, boolean needBack, String folderPath, String backPath, String parentSub) {
        this.expiryDate = expiryDate;
        this.needBack = needBack;
        this.folderPath = folderPath;
        this.backPath = backPath;
        this.parentSub = parentSub;
    }

    public FileProcess(Date expiryDate, boolean needBack, String folderPath, String backPath, String parentSub, int level) {
        this.expiryDate = expiryDate;
        this.needBack = needBack;
        this.folderPath = folderPath;
        this.backPath = backPath;
        this.parentSub = parentSub;
        this.level = level;
    }

    public void nextProcess(String folderPath, String parentSub) {
        this.folderPath = folderPath;
        this.parentSub = parentSub;
        this.started = false;
        this.truncateSize = 0L;
    }

    public void nextProcess(boolean needBack, String folderPath, String parentSub) {
        this.needBack = needBack;
        this.folderPath = folderPath;
        this.parentSub = parentSub;
        this.started = false;
        this.truncateSize = 0L;
    }

    public void nextProcess(Date expiryDate, boolean needBack, String folderPath, String parentSub) {
        this.expiryDate = expiryDate;
        this.needBack = needBack;
        this.folderPath = folderPath;
        this.parentSub = parentSub;
        this.started = false;
        this.truncateSize = 0L;
    }

    public static void main(String[] args) {
        Date expiryDate = DateTimeUtil.parse("2022-10-15 00:00:00");
        String folderPath = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置";
        String backPath = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\back_picture.zip";
        // String parentSub = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture";
        String parentSub = "";
        FileProcess process = new FileProcess(expiryDate, true, folderPath, backPath, parentSub);
        // process.cleanup();
        //
        // process.nextProcess(true, "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-files", backPath, parentSub);
        // process.cleanup();

        process.recovery();

        process.close();

    }

    public void cleanup() {
        if (started) {
            throw new BusinessException(ResultCodeEnum.CODE10009, "该清理任务已经执行！");
        }
        started = true;

        long startTime = System.currentTimeMillis();
        log.info("开始执行清理任务：needBack: {}\n\t\t folderPath: {}\n\t\t backPath: {}", needBack, folderPath, backPath);
        try {

            if (needBack && zos == null) {
                FileUtil.mkParentDirs(backPath);
                zos = new ZipOutputStream(new FileOutputStream(backPath));
                zos.setLevel(level);
                zosByteChannel = Channels.newChannel(zos);
            }

            String parentDir = parentDir(folderPath, parentSub);
            log.info("zip parentDir: {}", parentDir);
            compressAndDelete(new File(folderPath), parentDir);
        } catch (IOException e) {
            log.error("清理文件失败", e);
        }
        String spaceTime = CommonUtils.percentFormat((System.currentTimeMillis() - startTime) / 1000F, "#.##") + "s";
        log.info("Files added to the zip successfully! truncateSpace: {} {}, {} -> {}", truncateSpace(), spaceTime, folderPath, backPath);
    }

    public void recovery() {
        long startTime = System.currentTimeMillis();
        log.info("开始恢复备份文件：\n\t\t folderPath: {}\n\t\t backPath: {}", folderPath, backPath);
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backPath))) {

            byte[] buffer = new byte[4 * 1024];
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                String filePath = FilenameUtils.concat(folderPath, entry.getName());
                File outFile = new File(filePath);
                if (entry.isDirectory()) {
                    FileUtil.mkdir(outFile);
                } else {
                    FileUtil.mkParentDirs(outFile);
                    try (FileChannel fileChannel = new FileOutputStream(outFile).getChannel()) {
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fileChannel.write(ByteBuffer.wrap(buffer, 0, bytesRead));
                        }
                    }
                    /*try (OutputStream out = new FileOutputStream(outFile)){
                        IOUtils.copyLarge(zis, out);
                    }*/
                }
                outFile.setLastModified(entry.getTime());
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error("恢复备份失败", e);
        }
        String spaceTime = CommonUtils.percentFormat((System.currentTimeMillis() - startTime) / 1000F, "#.##") + "s";
        log.info("Zip File decompress successfully! timeSpace: {}, {} -> {}", spaceTime, backPath, folderPath);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(zos);
        IOUtils.closeQuietly(zosByteChannel);
    }

    private static String parentDir(String sourcePath, String parentSub) {
        // 处理截断父目录，判断截断父目录是否存在，并规范路径格式，以“/”或“\”分隔符结尾
        String parentDir = "";
        if (StringUtils.isNotEmpty(parentSub)) {
            String parentPath = FilenameUtils.normalizeNoEndSeparator(sourcePath, true);
            String psub = FilenameUtils.normalizeNoEndSeparator(parentSub, true);
            parentDir = StringUtils.substringAfter(parentPath, psub) + "/";
        }

        return parentDir;
    }

    private boolean compressAndDelete(File sourceFile, String parentDir) {
        // 判断当前路径是否被排除
        if (filterDate(sourceFile)) {
            return false;
        }

        // 处理单个文件逻辑
        if (sourceFile.isFile()) {
            return zipAndDelFile(sourceFile, parentDir);
        }

        File[] listFiles = sourceFile.listFiles();
        // 处理空文件夹逻辑
        if (ArrayUtils.isEmpty(listFiles)) {
            return zipAndDelEmptyDir(sourceFile, parentDir);
        }

        // 循环处理子文件和子文件夹
        assert listFiles != null;
        boolean deleteAll = true;
        for (File file : listFiles) {
            // 传入父文件夹的目录结构，并在最后加一斜杠,
            // 否则最后压缩包中无法保留原始文件结构，所有文件都压缩到包根目录下
            if (!compressAndDelete(file, parentDir + sourceFile.getName() + "/")) {
                deleteAll = false;
            }
        }
        if (deleteAll) {
            return FileUtil.del(sourceFile);
        }
        return false;
    }

    /**
     * 压缩并删除空目录
     */
    private boolean zipAndDelEmptyDir(File sourceFile, String parentDir) {
        try {
            if (zos != null) {
                // 保留空文件夹，只增加路径，不写入内容
                // 最后一定要以 / 结尾才是文件夹，\ 不行
                ZipEntry entry = new ZipEntry(parentDir + sourceFile.getName() + "/");
                // 设置文件最后修改时间
                entry.setTime(sourceFile.lastModified());
                zos.putNextEntry(entry);
                zos.closeEntry();
            }

            return Files.deleteIfExists(sourceFile.toPath());
        } catch (IOException e) {
            log.error("压缩/删除目录失败：{}", sourceFile.getAbsolutePath(), e);
            return false;
        }
    }

    /**
     * 压缩并删除单个文件
     */
    private boolean zipAndDelFile(File sourceFile, String parentDir) {
        // 如果压缩对象存在，则表示需要压缩，否则不压缩
        boolean flag = true;
        if (zos != null) {
            // 向 zip 输出流中添加一个 zip 实体，构造器中 name 为 zip 实体的文件的路径加名字
            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                ZipEntry entry = new ZipEntry(parentDir + sourceFile.getName());
                entry.setTime(sourceFile.lastModified());
                zos.putNextEntry(entry);
                // copy文件到zip输出流中
                FileChannel fileChannel = fis.getChannel();
                fileChannel.transferTo(0, sourceFile.length() - 1, zosByteChannel);

                // 压缩文件写入完成
                zos.closeEntry();
            } catch (IOException e) {
                log.error("压缩文件失败：{}", sourceFile.getAbsolutePath(), e);
                flag = false;
            }
        }

        // 删除被压缩文件，如果需要压缩却没有被压缩成功，则不删除
        try {
            long fileLen = sourceFile.length();
            if (flag && !Files.deleteIfExists(sourceFile.toPath())) {
                log.error("删除文件失败：{}", sourceFile.getAbsolutePath());
                flag = false;
            } else {
                truncateSize += fileLen;
            }
        } catch (IOException e) {
            log.error("删除文件失败：{}", sourceFile.getAbsolutePath(), e);
            flag = false;
        }
        return flag;
    }

    private boolean filterDate(File file) {
        if (file.isDirectory()) {
            return false;
        }
        long lastDate = file.lastModified();

        return lastDate > expiryDate.getTime();
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long truncateSize() {
        return truncateSize;
    }

    public String truncateSpace() {
        String[] units = new String[] {"B", "K", "M", "G", "T", "P"};
        double size = truncateSize;
        int i = 0;
        while (size > 1024) {
            size = size / 1024;
            if (++i == 5) {
                break;
            }
        }
        return CommonUtils.percentFormat((float)size, "#.##") + units[i];
    }
}
