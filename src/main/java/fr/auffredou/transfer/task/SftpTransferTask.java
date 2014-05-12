package fr.auffredou.transfer.task;

import com.jcraft.jsch.*;
import fr.auffredou.transfer.FileUtils;

import java.io.File;
import java.io.FileInputStream;


public class SftpTransferTask extends TransferTask {

    @Override
    public void execute() throws Exception {
        Session session;
        Channel channel;
        ChannelSftp channelSftp;
        JSch jsch = new JSch();
        session = jsch.getSession(username, host, port);
        session.setPassword(password);
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        channel = session.openChannel("sftp");
        channel.connect();
        channelSftp = (ChannelSftp) channel;
        channelSftp.cd(remoteBaseDirectory);

        StringBuilder sb = new StringBuilder();
        for (String subdir : FileUtils.directoryChain(localFile)) {
            sb.append("/").append(subdir);
            try {
                channelSftp.mkdir(remoteBaseDirectory + sb.toString());
            } catch (SftpException e) {
            }
        }

        File f = new File(localBaseDirectory,localFile);

        channelSftp.put(new FileInputStream(f), remoteFile);
        channelSftp.exit();
        channel.disconnect();
        session.disconnect();
    }


}