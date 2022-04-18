package com.yjh.platform.common.utils.smUtil.report;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author hsk on 2020/4/14
 */
@Slf4j
public class DownloadUtil {
    // 字符编码格式
    private static String charsetCode = "utf-8";

    /**
     * 文件的内容类型
     */
    private static String getFileContentType(String name) {
        String result = "";
        String fileType = name.toLowerCase();
        if (fileType.endsWith(".png")) {
            result = "image/png";
        } else if (fileType.endsWith(".gif")) {
            result = "image/gif";
        } else if (fileType.endsWith(".jpg") || fileType.endsWith(".jpeg")) {
            result = "image/jpeg";
        } else if (fileType.endsWith(".svg")) {
            result = "image/svg+xml";
        } else if (fileType.endsWith(".doc")) {
            result = "application/msword";
        } else if (fileType.endsWith(".xls")) {
            result = "application/x-excel";
        } else if (fileType.endsWith(".zip")) {
            result = "application/zip";
        } else if (fileType.endsWith(".pdf")) {
            result = "application/pdf";
        } else {
            result = "application/octet-stream";
        }
        return result;
    }

    /**
     * 下载文件
     *
     * @param path     文件的位置
     * @param fileName 自定义下载文件的名称
     * @param resp     http响应
     * @param req      http请求
     */
    public static void downloadFile(String path, String fileName, HttpServletResponse resp, HttpServletRequest req) {

        try {
            File file = new File(path);
            //中文乱码解决
            /*String type = req.getHeader("User-Agent").toLowerCase();
            if (type.indexOf("firefox") > 0 || type.indexOf("chrome") > 0) {
                //谷歌或火狐
                fileName = new String(fileName.getBytes(charsetCode), "iso8859-1");
            } else {
                //IE
                fileName = URLEncoder.encode(fileName, charsetCode);
            }*/
            setDownloadResponse(resp, fileName);
            resp.setContentLength((int) file.length());
            outStream(new FileInputStream(file), resp.getOutputStream());
        } catch (Exception e) {
            log.error("执行downloadFile发生了异常：{}", e.getMessage());
        }
    }

    /**
     * 基础字节数组输出
     */
    private static void outStream(InputStream is, OutputStream os) {
        try {
            byte[] buffer = new byte[10240];
            int length = -1;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
                os.flush();
            }
        } catch (Exception e) {
            log.error("执行outStream 发生了异常发生了异常：{}", e.getMessage());
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 设置下载响应头
     *
     * @param response     请求响应
     * @param downloadName 文件名
     */
    private static void setDownloadResponse(HttpServletResponse response, String downloadName) {
        try {
            downloadName = URLEncoder.encode(downloadName, charsetCode);
            response.setHeader("content-disposition", "attachment;filename=" + downloadName);
            response.setContentType(getFileContentType(downloadName) + "; charset=" + charsetCode);
        } catch (IOException e) {
            e.getMessage();
        }
    }

    /**
     * 将多个文件压缩到指定输出流中
     *
     * @param files    需要压缩的文件列表
     * @param fileName 文件名
     * @param response 响应
     * @throws IOException 异常
     */
    public static void compressZip(List<File> files, String fileName, HttpServletResponse response) throws IOException {
        ZipOutputStream zipOutStream = null;
        FileInputStream fileinputStream = null;
        setDownloadResponse(response, fileName);
        OutputStream outputStream = response.getOutputStream();
        try {
            //-- 包装成ZIP格式输出流
            zipOutStream = new ZipOutputStream(new BufferedOutputStream(outputStream));
            // -- 设置压缩方法
            zipOutStream.setMethod(ZipOutputStream.DEFLATED);
            //-- 将多文件循环写入压缩包
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                fileinputStream = new FileInputStream(file);
                byte[] data = new byte[(int) file.length()];
                fileinputStream.read(data);
                zipOutStream.putNextEntry(new ZipEntry(file.getName()));
                zipOutStream.write(data);
                zipOutStream.closeEntry();
            }
        } catch (IOException e) {
            log.error("compressZip error {}", e.getMessage());
        } finally {
            try {
                if (Objects.nonNull(zipOutStream)) {
                    zipOutStream.flush();
                    zipOutStream.close();
                }
                if (Objects.nonNull(outputStream)) {
                    outputStream.close();
                }
                if (Objects.nonNull(fileinputStream)) {
                    fileinputStream.close();
                }
            } catch (IOException e) {
                log.error("close stream error {}", e.getMessage());
            }
        }
    }
}
