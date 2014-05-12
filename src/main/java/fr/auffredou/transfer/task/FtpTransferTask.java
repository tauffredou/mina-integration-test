package fr.auffredou.transfer.task;

import fr.auffredou.transfer.FileUtils;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class FtpTransferTask extends TransferTask {

    @Override
    public void execute() throws IOException {
        FTPClient client = new FTPClient();
        FileInputStream fis = null;
        try {
            client.connect(host, port);
            client.login(username, password);

            fis = new FileInputStream(new File(localBaseDirectory, localFile));

            if(!client.changeWorkingDirectory(remoteBaseDirectory)){
                throw new IOException(remoteBaseDirectory);
            }
            StringBuilder sb = new StringBuilder(remoteBaseDirectory);
            for (String subdir : FileUtils.directoryChain(remoteFile)) {
                sb.append("/").append(subdir);
                client.makeDirectory(sb.toString());
            }
            client.storeFile(remoteFile, fis);
            client.logout();
        } catch (IOException e) {
            throw e;
        } finally {
            if (fis != null) {
                fis.close();
            }
            client.disconnect();
        }
    }


}