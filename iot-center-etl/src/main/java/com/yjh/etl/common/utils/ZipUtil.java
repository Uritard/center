package com.yjh.etl.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private Logger logger = LoggerFactory.getLogger(ZipUtil.class);
    static final int BUFFER = 8192;

    private File zipFile;

    public ZipUtil(String pathName) {
        zipFile = new File(pathName);
    }

    public void compress(String... pathName) throws Exception {
        try (FileOutputStream fileOutputStream = new FileOutputStream(zipFile);
             CheckedOutputStream cos = new CheckedOutputStream(fileOutputStream, new CRC32());
             ZipOutputStream out = new ZipOutputStream(cos)) {
            String basedir = "";
            for (int i = 0; i < pathName.length; i++) {
                compress(new File(pathName[i]), out, basedir);
            }
        }
    }

    private void compress(File file, ZipOutputStream out, String basedir) throws Exception {
        /* 判断是目录还是文件 */
        if (file.isDirectory()) {
            System.out.println("压缩：" + basedir + file.getName());
            this.compressDirectory(file, out, basedir);
        } else {
            System.out.println("压缩：" + basedir + file.getName());
            this.compressFile(file, out, basedir);
        }
    }

    /**
     * 压缩一个目录
     */
    private void compressDirectory(File dir, ZipOutputStream out, String basedir) throws Exception {
        if (!dir.exists())
            return;

        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            /* 递归 */
            compress(files[i], out, basedir + dir.getName() + "/");
        }
    }

    /**
     * 压缩一个文件
     */
    private void compressFile(File file, ZipOutputStream out, String basedir) throws Exception {
        if (!file.exists()) {
            return;
        }
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(
                    new FileInputStream(file));
            ZipEntry entry = new ZipEntry(basedir + file.getName());
            out.putNextEntry(entry);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = bis.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
        } catch (Exception e) {
            logger.error("压缩文件失败", e);
        } finally {
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (Exception e) {
                logger.error("压缩文件失败", e);
            }
        }
    }
}
