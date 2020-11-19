package com.yjh.accessrobot.common.utils;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import java.io.*;
import java.util.Properties;
import java.util.Vector;

@Slf4j
public class SFTPUtil {

    private ChannelSftp sftp;
    private Session session;
    @Value("${ftp.local.charset}")
    private String localFileCode;//本地文件编码
    @Value("${ftp.key.path}")
    private String filePath;//SFTP文件在服务器的地址
    @Value("${ftp.host}")
    private String host;//SFTP 服务器地址IP地址
    @Value("${ftp.port}")
    private int port;//端口
    @Value("${ftp.username}")
    private String username;//SFTP 登录用户名
    @Value("${ftp.password}")
    private String password;//SFTP 登录密码
    @Value("${ftp.server.charset}")
    private String ftpFileCode;//FTP文件编码
    @Value("${ftp.key.pw}")
    private String keyPw;//FTP隐式/显式方法
    private static SFTPUtil instance = null;
    /**
     * 构造基于密码认证的sftp对象
     */
    public SFTPUtil(String username, String password, String host, int port) {
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }
    public SFTPUtil() {
    }

    /**
     * 连接sftp服务器
     */
    public void login() {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);

            if (password != null) {
                session.setPassword(password);
            }

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            log.info("sftp session connected...");

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftp = (ChannelSftp) channel;
            log.info("connected to " + host);
        } catch (JSchException e) {
            log.error("sftp login error", e);
        }
    }

    /**
     * 关闭连接 server
     */
    public void logout() {
        if (sftp != null) {
            if (sftp.isConnected()) {
                sftp.disconnect();
            }
        }
        if (session != null) {
            if (session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * 将输入流的数据上传到sftp作为文件。文件完整路径=basePath+directory
     *
     * @param basePath     服务器的基础路径
     * @param directory    上传到该目录
     * @param sftpFileName sftp端文件名
     * @param input        输入流
     */
    public void upload(String basePath, String directory, String sftpFileName, InputStream input) throws SftpException {
        createAndCdDir(basePath);
        createAndCdDir(directory);
        sftp.put(input, sftpFileName);  //上传文件
    }

    /**
     * 判断目录是否存在
     */
    private void createAndCdDir(String directory) throws SftpException {
        if (isDirExist(directory)) {
            sftp.cd(directory);
        } else {
            // 建立目录并进入
            try {
                createDir2(directory, sftp);
            } catch (Exception e) {
                log.error("createDir error", e);
            }
        }
    }

    /**
     * 判断目录是否存在
     */
    private boolean isDirExist(String directory) {
        boolean isDirExistFlag = false;
        try {
            SftpATTRS sftpATTRS = sftp.lstat(directory);
            isDirExistFlag = true;
            return sftpATTRS.isDir();
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().equals("no such file")) {
                isDirExistFlag = false;
            }
        }
        return isDirExistFlag;
    }

    /**
     * 创建一个文件目录
     */
    private void createDir(String createpath, ChannelSftp sftp) throws Exception {
        try {
            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                return;
            }
            String pathArry[] = createpath.split("/");
            StringBuilder filePath = new StringBuilder("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                filePath.append(path).append("/");
                if (isDirExist(filePath.toString())) {
                    sftp.cd(filePath.toString());
                } else {
                    // 建立目录
                    sftp.mkdir(filePath.toString());
                    // 进入并设置为当前目录
                    sftp.cd(filePath.toString());
                }
            }
            this.sftp.cd(createpath);
        } catch (SftpException e) {
            log.error("创建路径错误：" + createpath);
        }
    }

    /**
     * 创建一个文件目录
     */
    private void createDir2(String createpath, ChannelSftp sftp) throws Exception {
        try {
            if (isDirExist(createpath)) {
                this.sftp.cd(createpath);
                return;
            }
            String pathArry[] = createpath.split("/");
            for (String path : pathArry) {
                if (path.equals("")) {
                    continue;
                }
                if (isDirExist(path)) {
                    sftp.cd(path);
                } else {
                    // 建立目录
                    sftp.mkdir(path);
                    // 进入并设置为当前目录
                    sftp.cd(path);
                }
            }
        } catch (SftpException e) {
            log.error("创建路径错误：" + createpath);
        }
    }

    /**
     * 下载文件。
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件
     * @param saveFile     存在本地的路径
     */
    public File download(String directory, String downloadFile, String saveFile) throws SftpException, FileNotFoundException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        File file = new File(saveFile);
        sftp.get(downloadFile, new FileOutputStream(file));
        return file;
    }

    /**
     * 下载文件
     *
     * @param directory    下载目录
     * @param downloadFile 下载的文件名
     * @return 字节数组
     */
    public byte[] download(String directory, String downloadFile) throws SftpException, IOException {
        if (directory != null && !"".equals(directory)) {
            sftp.cd(directory);
        }
        InputStream is = sftp.get(downloadFile);

        return IOUtils.toByteArray(is);
    }

    /**
     * 删除文件
     *
     * @param directory  要删除文件所在目录
     * @param deleteFile 要删除的文件
     */
    public void delete(String directory, String deleteFile) throws SftpException {
        sftp.cd(directory);
        sftp.rm(deleteFile);
    }

    /**
     * 列出目录下的文件
     *
     * @param directory 要列出的目录
     */
    public Vector<?> listFiles(String directory) throws SftpException {
        return sftp.ls(directory);
    }

    //上传文件测试  
    public static void main(String[] args) throws SftpException, IOException {
        SFTPUtil sftp = new SFTPUtil("用户名", "密码", "ip地址", 22);
        sftp.login();
        File file = new File("D:\\图片\\t0124dd095ceb042322.jpg");
        InputStream is = new FileInputStream(file);

        sftp.upload("基础路径", "文件路径", "test_sftp.jpg", is);
        sftp.logout();
    }
}