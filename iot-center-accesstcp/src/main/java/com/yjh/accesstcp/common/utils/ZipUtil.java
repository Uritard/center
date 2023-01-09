package com.yjh.accesstcp.common.utils;

import com.yjh.accesstcp.commons.result.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    private static Logger logger = LoggerFactory.getLogger(ZipUtil.class);
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


    /**
     * @param zipPathDir  压缩包路径 ，如 /home/data/zip-folder/
     * @param zipFileName 压缩包名称 ，如 测试文件.zip
     * @param fileList    要压缩的文件列表（绝对路径），如 /home/person/test/测试.doc，/home/person/haha/测试.doc
     * @return
     */
    public static void compressFiles(String zipPathDir, String zipFileName, List<String> fileList) {
        Boolean isAllNotExists = true;
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(new File(zipPathDir + zipFileName)))) {
            File zipFile = new File(zipPathDir);
            if (!zipFile.exists()) {
                zipFile.mkdirs();
            }
            for (String filePath : fileList) {
                File file = new File(filePath);

                if (file.exists()) {
                    isAllNotExists = false;
                    int index = file.getName().lastIndexOf('.');
                    ZipEntry zipEntry = new ZipEntry(file.getName().substring(0, index) + file.getName().substring(index));
                    zos.putNextEntry(zipEntry);
                    byte[] buffer = new byte[2048];
                    compressSingleFile(file, zos, buffer);
                }
            }
            if (isAllNotExists){
                File emptyZipFile = new File(zipPathDir+zipFileName);
                if (emptyZipFile.exists()){
                    emptyZipFile.delete();
                }
                emptyZipFile.createNewFile();
            }
            zos.flush();
        } catch (Exception e) {
            logger.error("压缩文建出错：",e);
        }
    }

    //压缩单个文件
    public static void compressSingleFile(File file, ZipOutputStream zos, byte[] buffer) {
        int len;
        try (FileInputStream fis = new FileInputStream(file)) {
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
                zos.flush();
            }
            zos.closeEntry();
        } catch (IOException e) {
            logger.error("压缩单个文件异常：",e);
        }
    }


}
