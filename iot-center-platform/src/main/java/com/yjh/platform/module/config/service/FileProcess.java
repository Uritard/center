/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.config.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import com.yjh.platform.common.Constant;
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
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    /**
     * 过期日期，删除此日期之前的文件
     */
    private Date expiryDate;
    /**
     * 是否需要备份
     */
    private boolean needBack;
    /**
     * 清理文件夹列表
     */
    private String[] folderPaths;
    /**
     * 备份文件路径
     */
    private String backPath;
    /**
     * 公共父目录，文件压缩后的路径即为原始路径减去父目录后的路径
     */
    private String parentSub;
    /**
     * 排除目录，在此列表中的目录/文件不被清理，支持通配符
     */
    private String[] excludes;
    /**
     * 压缩级别，默认1
     */
    private int level;
    private long truncateSize = 0L;

    private volatile boolean started = false;
    private ZipOutputStream zos;
    private WritableByteChannel zosByteChannel;

    public FileProcess(Date expiryDate, boolean needBack, String folderPath, String backPath, String parentSub) {
        this(expiryDate, needBack, new String[] {folderPath}, backPath, parentSub, Deflater.BEST_SPEED);
    }

    public FileProcess(Date expiryDate, boolean needBack, String[] folderPath, String backPath, String parentSub) {
        this(expiryDate, needBack, folderPath, backPath, parentSub, Deflater.BEST_SPEED);
    }

    public FileProcess(Date expiryDate, boolean needBack, String[] folderPath, String backPath, String parentSub, int level) {
        this.expiryDate = expiryDate;
        this.needBack = needBack;
        this.folderPaths = folderPath;
        this.backPath = backPath;
        this.parentSub = parentSub;
        this.level = level;
    }

    /**
     * 执行下一步，文件夹的压缩和删除可以分步执行，但每次必须先执行 nextProcess 步骤才可以添加新的文件夹进行删除/压缩
     */
    public void nextProcess(String folderPath, String parentSub) {
        this.nextProcess(null, needBack, new String[] {folderPath}, parentSub);
    }

    public void nextProcess(String[] folderPaths, String parentSub) {
        this.nextProcess(null, needBack, folderPaths, parentSub);
    }

    public void nextProcess(boolean needBack, String folderPath, String parentSub) {
        this.nextProcess(null, needBack, new String[] {folderPath}, parentSub);
    }

    public void nextProcess(Date expiryDate, String folderPath, String parentSub) {
        this.nextProcess(expiryDate, needBack, new String[] {folderPath}, parentSub);
    }

    public void nextProcess(Date expiryDate, boolean needBack, String folderPath, String parentSub) {
        this.nextProcess(expiryDate, needBack, new String[] {folderPath}, parentSub);
    }

    /**
     * 执行下一步，文件夹的压缩和删除可以分步执行，但每次必须先执行 nextProcess 步骤才可以添加新的文件夹进行删除/压缩
     * 支持重新指定过期时间、是否需备份，清理目录和父目录
     */
    public void nextProcess(Date expiryDate, boolean needBack, String[] folderPaths, String parentSub) {
        if (expiryDate != null) {
            this.expiryDate = expiryDate;
        }
        this.needBack = needBack;
        this.folderPaths = folderPaths;
        this.parentSub = parentSub;
        this.started = false;
        this.truncateSize = 0L;
    }

    public static void main(String[] args) {
        Date expiryDate = DateTimeUtil.parse("2022-10-18 00:00:00");
        String folderPath = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture";
        String backPath = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\back_all3.zip";
        // String parentSub = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture";
        String parentSub = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置";

        String[] folders = new String[] {"D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-files\\temporaryFiles",
            "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture\\ftpImg"};

        FileProcess process = new FileProcess(expiryDate, true, folders, backPath, parentSub);
        process.setExcludes(new String[] {
            "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture\\ftpImg\\Map\\**",
            "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-picture\\resultImg\\**"});
        process.cleanup();
        //
        // process.nextProcess(true, "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-files", backPath, parentSub);
        // process.cleanup();

        // process.nextProcess(folderPath, parentSub);
        // process.recovery();
        //

        /*for (String folder : folders) {
            String parentDir = process.parentDir(folder, parentSub);
            log.info("zip parentDir: {}", parentDir);
        }*/

        /*File sourceFile = new File(folderPath);
        File[] listFiles = sourceFile.listFiles();
        for (File f : listFiles) {
            System.out.println(f.getAbsolutePath());
        }*/

/*
        String path = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置\\iot-files";
        // process.compressAndDelete(path, "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置");
        parentSub = "D:\\Project\\资料\\巡视系统\\softPackage\\packages-yjh\\环境配置";
        // process.nextProcess(true, path, parentSub);
        // process.cleanup(true);

        process.nextProcess(parentSub, parentSub);
        process.recovery();
*/

        process.close();
    }

    /**
     * 执行清理任务
     */
    public boolean cleanup() {
        return cleanup(false);
    }

    /**
     * 执行清理任务，若传true则表示使用 Files.walkFileTree 来遍历并删除文件
     */
    public boolean cleanup(boolean nio) {
        if (started) {
            throw new BusinessException(ResultCodeEnum.CODE10009, "该清理任务已经执行！");
        }
        started = true;
        boolean flag = true;
        long startTime = System.currentTimeMillis();
        folderPaths = Arrays.stream(folderPaths).map(m -> StringUtils.stripEnd(m, "/")).toArray(String[]::new);
        log.info("开始执行清理任务：needBack: {}\n\t\t folderPath: {}\n\t\t backPath: {}", needBack, folderPaths, backPath);
        try {

            if (needBack && zos == null) {
                FileUtil.mkParentDirs(backPath);
                zos = new ZipOutputStream(new FileOutputStream(backPath));
                zos.setLevel(level);
                zosByteChannel = Channels.newChannel(zos);
            }

            for (String folder : folderPaths) {
                if (nio) {
                    compressAndDelete(folder, parentSub);
                } else {
                    String parentDir = parentDir(folder, parentSub);
                    log.info("zip parentDir: {}", parentDir);
                    compressAndDelete(new File(folder), parentDir);
                }
            }
        } catch (Exception e) {
            flag = false;
            log.error("清理文件失败", e);
        }

        String spaceTime = CommonUtils.percentFormat((System.currentTimeMillis() - startTime) / 1000F, "#.##") + "s";
        log.info("Files added to the zip successfully! flag: {}, truncateSpace: {} {}, {} -> {}", flag, truncateSpace(), spaceTime,
            folderPaths, backPath);

        excludes = null;
        return flag;
    }

    /**
     * 回复文件
     */
    public void recovery() {
        long startTime = System.currentTimeMillis();
        log.info("开始恢复备份文件：\n\t\t folderPath: {}\n\t\t backPath: {}", folderPaths, backPath);
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backPath))) {

            byte[] buffer = new byte[4 * 1024];
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(folderPaths[0], entry.getName());
                if (entry.isDirectory()) {
                    FileUtil.mkdir(outFile);
                } else {
                    FileUtil.mkParentDirs(outFile);
                    if (Constant.logUpLv3()) {
                        log.info("recovery path:{}", outFile.getAbsolutePath());
                    }
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
        } catch (Exception e) {
            log.error("恢复备份失败", e);
        }
        String spaceTime = CommonUtils.percentFormat((System.currentTimeMillis() - startTime) / 1000F, "#.##") + "s";
        log.info("Zip File decompress successfully! timeSpace: {}, {} -> {}", spaceTime, backPath, folderPaths);
    }

    @Override
    public void close() {
        IOUtils.closeQuietly(zos);
        IOUtils.closeQuietly(zosByteChannel);
    }

    /**
     * 处理父目录路径
     */
    private String parentDir(String sourcePath, String parentSub) {
        // 处理截断父目录，判断截断父目录是否存在，并规范路径格式，以“/”或“\”分隔符结尾
        String parentDir = "";
        if (StringUtils.isNotEmpty(parentSub)) {
            String parentPath = FilenameUtils.normalizeNoEndSeparator(FilenameUtils.concat(sourcePath, "../"), true);
            String psub = FilenameUtils.normalizeNoEndSeparator(parentSub, true);
            parentDir = StringUtils.substringAfter(parentPath, psub) + "/";
        }

        return parentDir;
    }

    /**
     * 执行具体的删除及压缩处理，并组装压缩路径
     */
    private boolean compressAndDelete(File sourceFile, String parentDir) {
        // 判断当前路径是否被排除
        if (filterDate(sourceFile)) {
            if (Constant.logUpLv2()) {
                log.info("路径排除： {}", sourceFile.getAbsolutePath());
            }
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
            if (!compressAndDelete(file, FilenameUtils.concat(parentDir, sourceFile.getName()) + "/")) {
                deleteAll = false;
            }
        }
        if (deleteAll && !ArrayUtils.contains(folderPaths, sourceFile.getAbsolutePath())) {
            return FileUtil.del(sourceFile);
        }
        return false;
    }

    /**
     * 使用 Files.walkFileTree 来遍历并删除文件，理论上是 nio 包，会更快一些
     *
     * @param sourceFilePath 处理目录
     * @param parentDir      父目录
     */
    private void compressAndDelete(String sourceFilePath, String parentDir) {
        if (StringUtils.isEmpty(parentDir)) {
            parentDir = FileUtil.getParent(sourceFilePath, 1);
        }

        Path pdir = Paths.get(parentDir).normalize();
        try {
            Files.walkFileTree(Paths.get(sourceFilePath), new SimpleFileVisitor<Path>() {
                //进入文件夹之前
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    try (Stream<Path> pathStream = Files.list(dir)) {
                        if (!pathStream.findAny().isPresent()) {
                            String path = FilenameUtils.normalizeNoEndSeparator(pdir.relativize(dir).toString(), true);
                            if (zos != null) {
                                // 保留空文件夹，只增加路径，不写入内容
                                // 最后一定要以 / 结尾才是文件夹，\ 不行
                                ZipEntry entry = new ZipEntry(path + "/");
                                // 设置文件最后修改时间
                                entry.setLastModifiedTime(Files.getLastModifiedTime(dir));
                                zos.putNextEntry(entry);
                                zos.closeEntry();
                            }
                        }
                    } catch (Exception e) {
                        log.error("创建空文件路径失败：{}", dir, e);
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                //遍历文件
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (filterDate(file)) {
                        return super.visitFile(file, attrs);
                    }

                    pathFileVisit(file, pdir);

                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    //进入文件夹之后
                    try (Stream<Path> pathStream = Files.list(dir)) {
                        if (!pathStream.findAny().isPresent() && !ArrayUtils.contains(folderPaths, dir.toString())) {
                            Files.delete(dir);
                        }
                    } catch (IOException e) {
                        log.error("删除空文件夹失败：{}", dir, e);
                    }
                    return super.postVisitDirectory(dir, exc);
                }
            });
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Files.walkFileTree 中对单个文件进行处理
     */
    private void pathFileVisit(Path file, Path pdir) {
        boolean flag = true;
        long fileLen = 0;
        if (zos != null) {
            // 向 zip 输出流中添加一个 zip 实体，构造器中 name 为 zip 实体的文件的路径加名字
            try (FileChannel fileChannel = FileChannel.open(file)) {
                fileLen = Files.size(file);
                String path = FilenameUtils.normalizeNoEndSeparator(pdir.relativize(file).toString(), true);
                ZipEntry entry = new ZipEntry(path);
                entry.setLastModifiedTime(Files.getLastModifiedTime(file));
                zos.putNextEntry(entry);
                // copy文件到zip输出流中
                fileChannel.transferTo(0, fileLen - 1, zosByteChannel);

                // 压缩文件写入完成
                zos.closeEntry();
            } catch (Exception e) {
                log.error("压缩文件失败：{}", file, e);
                flag = false;
            }
        }

        // 删除被压缩文件，如果需要压缩却没有被压缩成功，则不删除
        try {
            if (fileLen == 0) {
                fileLen = Files.size(file);
            }
            if (flag && !Files.deleteIfExists(file)) {
                log.error("删除文件失败：{}", file);
            } else {
                if (Constant.logUpLv3()) {
                    log.info("delete file： {}", file);
                }
                truncateSize += fileLen;
            }
        } catch (Exception e) {
            log.error("删除文件失败：{}", file, e);
        }
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
        } catch (Exception e) {
            log.error("创建压缩目录失败：{}", sourceFile.getAbsolutePath(), e);
        }

        try {
            if (!ArrayUtils.contains(folderPaths, sourceFile.getAbsolutePath()) && !Files.deleteIfExists(sourceFile.toPath())) {
                log.error("删除空目录失败：{}", sourceFile.getAbsolutePath());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("删除空目录失败：{}", sourceFile.getAbsolutePath(), e);
        }
        return false;
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
            } catch (Exception e) {
                log.error("压缩文件失败：{}", sourceFile.getAbsolutePath(), e);
                flag = false;
            }
        }

        // 删除被压缩文件，如果需要压缩却没有被压缩成功，则不删除
        try {
            long fileLen = sourceFile.length();
            // 小于 10B 的文件默认为损坏文件，当做压缩成功处理，删除
            boolean deleteOrEmpty = (fileLen <= 10 || flag);
            if (deleteOrEmpty && !Files.deleteIfExists(sourceFile.toPath())) {
                log.error("删除文件失败：{}", sourceFile.getAbsolutePath());
                flag = false;
            } else {
                if (Constant.logUpLv3()) {
                    log.info("delete file： {}", sourceFile.getAbsolutePath());
                }
                truncateSize += fileLen;
                flag = true;
            }
        } catch (Exception e) {
            log.error("删除文件失败：{}", sourceFile.getAbsolutePath(), e);
            flag = false;
        }
        return flag;
    }

    /**
     * 排除逻辑的预处理
     */
    private void excludePrepare(String[] excs) {

        if (ArrayUtils.isEmpty(excs)) {
            return;
        }

        // 处理排除路径列表，确认路径是否存在，并格式化路径，若是目录则以“/”或“\”分隔符结尾
        List<String> excludeList = new ArrayList<>();

        for (String exclude : excs) {
            if (StringUtils.isBlank(exclude)) {
                continue;
            }

            if (StringUtils.containsAny(exclude, "?", "*")) {
                excludeList.add(FilenameUtils.separatorsToUnix(exclude));
            } else {
                File exFile = new File(exclude);
                if (exFile.exists()) {
                    // 若是目录则以“/”或“\”分隔符结尾
                    //                String exPath = exFile.getAbsolutePath() + (exFile.isFile() ? "" : File.separator);
                    String exPath = exFile.getAbsolutePath();
                    excludeList.add(exPath);
                }
            }
        }

        // 排除文件/文件夹，文件夹以 / 结尾, 使用 ThreadLocal 存储，少传一个参数
        if (!excludeList.isEmpty()) {
            this.excludes = excludeList.toArray(new String[0]);
        }
    }

    /**
     * 文件过滤，过滤指定时间之前的文件及排除路径之外的路径
     */
    private boolean filterDate(File file) {

        if (ArrayUtils.isNotEmpty(excludes)) {
            // 例外文件/目录判断
            for (String exPath : excludes) {
                // 支持通配符 *? 的判断
                if (FilenameUtils.wildcardMatch(file.getAbsolutePath(), exPath)) {
                    return true;
                }
            }
        }

        if (file.isDirectory()) {
            return false;
        }
        long lastDate = file.lastModified();

        return lastDate > expiryDate.getTime();
    }

    /**
     * 文件过滤，过滤指定时间之前的文件及排除路径之外的路径
     * 适用于 Files.walkFileTree
     */
    private boolean filterDate(Path file) {

        if (ArrayUtils.isNotEmpty(excludes)) {
            // 例外文件/目录判断
            for (String exPath : excludes) {
                // 支持通配符 *? 的判断
                if (FilenameUtils.wildcardMatch(FilenameUtils.separatorsToUnix(file.toAbsolutePath().toString()), exPath)) {
                    return true;
                }
            }
        }

        boolean flag = true;
        try {
            long lastDate = Files.getLastModifiedTime(file).toMillis();
            flag = lastDate > expiryDate.getTime();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return flag;
    }

    /**
     * 设置排除路径
     */
    public FileProcess setExcludes(String[] excludes) {
        excludePrepare(excludes);
        return this;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 清理文件总大小
     */
    public long truncateSize() {
        return truncateSize;
    }

    /**
     * 清理文件总大小，带单位
     */
    public String truncateSpace() {
        return CommonUtils.fileSpace(truncateSize);
    }
}
