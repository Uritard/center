package com.yjh.platform.common.utils;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @Author: lqh
 * @Date: 2022/05/25
 */
@Slf4j
public class FileUtil {

    private static List<String> nameList = new ArrayList<>();

    static{
        nameList.add("ZIP");
        nameList.add("XLS");
        nameList.add("XLSX");
        nameList.add("XLSB");
        nameList.add("XLSM");
        nameList.add("XLST");
        nameList.add("XML");
    }


    /**
     * 压缩图片 判断缩略图是否存在  不存在压缩 存在直接返回
     * @param filePath 原图路径
     * @return 缩略图路径
     */
    public static String picCompression(String filePath) {
        try {
            String thumbnailFilePath = "";
            if (StringUtils.isNotEmpty(filePath) && isImageFile(filePath)) {
                File file = new File(filePath);
                if (!file.isDirectory() && file.exists()) {
                    String path = StringUtils.substringBeforeLast(filePath, ".");
                    String suffixName = StringUtils.substringAfterLast(filePath, ".");
                    thumbnailFilePath = path + "-thumbnail" + "." + suffixName;
                    File suffixFile = new File(thumbnailFilePath);
                    if (!suffixFile.exists()) {
                        Thumbnails.of(filePath)
                                .size(960, 540)
                                .outputQuality(0.25f)
                                .asFiles(Rename.SUFFIX_HYPHEN_THUMBNAIL);
                    }
                }
            }
            return thumbnailFilePath;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 判断图片大小是否为20kb
     * @param filePath 文件路径
     * @return true 大于20kb
     */
    public static boolean hasEffective(String filePath) {
        File file = new File(filePath);
        long length = file.length();
        return length > 20L;
    }

    /**
     * 判断是不是图片格式
     * @param filePath 文件路径
     * @return
     */
    public static boolean isImageFile(String filePath) {
        String extension = getFileExtension(filePath);
        if (extension != null) {
            // 判断常见图片格式的后缀
            return "jpg".equalsIgnoreCase(extension) ||
                    "jpeg".equalsIgnoreCase(extension) ||
                    "png".equalsIgnoreCase(extension) ||
                    "gif".equalsIgnoreCase(extension) ||
                    "bmp".equalsIgnoreCase(extension);
        }
        return false;
    }

    public static String getFileExtension(String filePath) {
        int dotIndex = filePath.lastIndexOf('.');
        if (dotIndex >= 0 && dotIndex < filePath.length() - 1) {
            return filePath.substring(dotIndex + 1).toLowerCase();
        }
        return null;
    }

    public static boolean checkFileName(String fileName){
        String name = StringUtils.substringAfterLast(fileName,".").toUpperCase();
        if (nameList.contains(name)){
            return true;
        }
        log.error("上传文件不在规定文件范围内: {} - {}", fileName, name);
        return false;
    }

    public static boolean checkFileName(String fileName, String[] type){
        String name = StringUtils.substringAfterLast(fileName,".");
        if (Arrays.binarySearch(type, name) >= 0){
            return true;
        }
        return false;
    }

    /**
     * 文件复制
     *
     * @param sourcePath 文件源路径
     * @param descPath 文件目的路径
     * @return
     */
    public static boolean copyFileUsingStream(String sourcePath, String descPath) {
        File source = new File(sourcePath);
        File dest = new File(descPath);

        try {
            FileUtils.copyFile(source, dest);
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * @param fileList 压缩多个文件夹名称 全路径
     * @param zipFileName 压缩文件名称
     * @param basePath 压缩文件的根目录
     * @param path 压缩到那个路径下
     *                    此方法保留空白文件
     */
    public static void zip(List<String> fileList, String zipFileName,String basePath,String path)throws Exception{
        if(fileList ==null || fileList.size() ==0){
            return;
        }
        File inputFile = null;
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(path+File.separator+zipFileName));
        for (String str: fileList) {
            inputFile = new File(str);
            if (!inputFile.exists()){
                log.info("文件：{} 不存在",str);
                continue;
            }

            Boolean flag = true;
            if(!str.contains("/")){
                flag = false;
            }
            if (!inputFile.isDirectory()){
                zip(out, inputFile, basePath+"/"+inputFile.getName(),flag);
            }
        }
        out.flush();
        out.close();
    }
    public static void zip(ZipOutputStream out, File f, String base,Boolean flag) throws Exception {
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
//			out.putNextEntry(new org.apache.tools.zip.ZipEntry(base + "/"));
//			base = base.length() == 0 ? "" : base + "/";
            if(base.length() == 0){
                base = "";
            }else{
                out.putNextEntry(new ZipEntry(base + "/"));
                base = base + "/";
            }
            if(null!=fl && flag){
                for (int i = 0; i < fl.length; i++) {
                    zip(out, fl[i], base + fl[i].getName(),true);
                }
            }
        } else {
            out.putNextEntry(new ZipEntry(base));
            FileInputStream in = new FileInputStream(f);
            int b;
            log.info(base);
//			while ((b = in.read()) != -1) {
//				out.write(b);
//			}
            byte[] buf = new byte[1024*10];//每秒100k
            while((b = in.read(buf))!=-1){//循环读取
                out.write(buf, 0, b);
            }
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * 将MultipartFile文件保存在指定的文件中
     *
     * @param file     MultipartFile文件
     * @param filePath 指定文件
     */
    public static void saveFile(MultipartFile file, String filePath) throws IOException {
        File fileTemp = new File(filePath);
        if (!fileTemp.exists()) {
            if (!fileTemp.getParentFile().exists()) {
                fileTemp.getParentFile().mkdirs();
            }
            fileTemp.createNewFile();
            file.transferTo(fileTemp);
        }
    }

    /**
     * 创建文件夹，如果存在直接返回此文件夹<br>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dirPath 文件夹路径，使用POSIX格式，无论哪个平台
     * @return 创建的目录
     */
    public static File mkdir(String dirPath) {
        if (dirPath == null) {
            return null;
        }
        final File dir = new File(dirPath);
        return mkdir(dir);
    }

    /**
     * 创建文件夹，会递归自动创建其不存在的父文件夹，如果存在直接返回此文件夹<br>
     * 此方法不对File对象类型做判断，如果File不存在，无法判断其类型
     *
     * @param dir 目录
     * @return 创建的目录
     */
    public static File mkdir(File dir) {
        if (dir == null) {
            return null;
        }
        if (!dir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            dir.mkdirs();
        }
        return dir;
    }

}
