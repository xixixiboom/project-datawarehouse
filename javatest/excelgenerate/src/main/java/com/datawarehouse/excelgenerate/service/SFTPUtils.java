package com.datawarehouse.excelgenerate.service;
import com.datawarehouse.excelgenerate.utils.FileHandle;
import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
//import org.apache.commons.net.ftp.*;

import java.io.*;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.io.IOException;

@Service
public class SFTPUtils {
    Session session = null;
    Channel channel = null;

    private static final Logger logger = LoggerFactory.getLogger(SFTPUtils.class);

    public ChannelSftp getChannelSftp(String sftpHost,String sftpUsername,String sftpPassword,int sftpPort) throws JSchException {
        // ssh服务器的IP、用户名、密码和端口
//        String sftpHost = "101.200.149.*";
/*        String sftpHost = uploadFileConfig.getIp();
        String sftpUsername = uploadFileConfig.getUserName();
        String sftpPassword = uploadFileConfig.getPassWord();
        int sftpPort = uploadFileConfig.getPort();*/
        JSch jsch = new JSch(); // 创建JSch对象
        session = jsch.getSession(sftpUsername, sftpHost, sftpPort);// 获取sesson对象
        session.setPassword(sftpPassword);// 设置sftp访问密码
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);// 为session重新设置参数
        session.setTimeout(30000);// 设置超时
        session.connect();// 建立连接
        channel = session.openChannel("sftp"); // 打开sftp通道
        channel.connect();// 建立sftp通道连接
        logger.info("服务器已连接,连接服务器ip："+sftpHost+"\t用户名："+sftpUsername+"\t端口："+sftpPort);
        return (ChannelSftp) channel;
    }


    public void doUpload(String sftpHost,String sftpUsername,String sftpPassword,int sftpPort,LinkedHashMap<String, String> localAndRemoteDir) {
         /*JSch支持三种文件传输模式：
         OVERWRITE 完全覆盖模式，这是JSch的 默认文件传输模式，即如果目标文件已经存在，传输的文件将完全覆盖目标文件，产生新的文件。
         RESUME 恢复模式，如果文件已经传输一部分，这时由于网络或其他任何原因导致文件传输中断，如果下一次传输相同的文件，则会从上一次中断的地方续传。
         APPEND 追加模式，如果目标文件已存在，传输的文件将在目标文件后追加。*/
        if(localAndRemoteDir.isEmpty()||localAndRemoteDir==null){
            logger.error("上传列表为空");
            System.exit(0);
        }
//        LinkedHashMap<String, String> localAndRemoteDir = uploadFileConfig.getLocalAndRemoteDir();
        Iterator<String> iterator = localAndRemoteDir.keySet().iterator();
        try {
            SFTPUtils sftpChannel = new SFTPUtils();
            ChannelSftp channel = sftpChannel.getChannelSftp(sftpHost,sftpUsername,sftpPassword,sftpPort);
            while (iterator.hasNext()) {
                String localDir = iterator.next();
                String remoteDir = localAndRemoteDir.get(localDir);
                localDir=localDir.substring(1,localDir.length()-1);
                File[] files = FileHandle.returnFileArray(localDir, "获取本地文件");
//                File[] files = new File(localDir).listFiles();
                for (File localFile : files) {
                    String dst = remoteDir+"/"+localFile.getName();
                    channel.put(new FileInputStream(localFile),dst, ChannelSftp.OVERWRITE);  // 这里使用OVERWRITE模式
                    // 默认使用OVERWRITE模式
/*                    byte[] buff = new byte[1024 * 256]; // 设定每次传输的数据块大小为256KB
                    int read;
                    if (out != null) {
                        InputStream is = new FileInputStream(localFile);
                        do {
                            read = is.read(buff, 0, buff.length);
                            if (read > 0) {
                                out.write(buff, 0, read);
                            }
                            out.flush();
                        } while (read >= 0);
                    }*/
                }
                logger.info("目录"+localDir+"中"+files.length+"个文件已传输至目录"+remoteDir);
            }
            channel.quit();
            sftpChannel.closeChannel();
        } catch (FileNotFoundException e) {
            logger.error("文件不存在");
            e.printStackTrace();
        }catch(SftpException e){
            logger.error("文件上传异常");
            e.printStackTrace();
        }catch(IOException e){
            logger.error("IO异常");
            e.printStackTrace();
        }catch (JSchException e){
            logger.error("登录异常");
            e.printStackTrace();
        }
    }

    public void closeChannel() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
        logger.info("文件全部传输完成");
    }

/*    public void downloadFile(String remotePathFile, String localPathFile) throws SftpException, IOException {
        try (FileOutputStream os = new FileOutputStream(new File(localPathFile))) {
            if (channel == null)
                throw new IOException("sftp server not login");
            channel.get(remotePathFile, os);
        }
    }*/
}

