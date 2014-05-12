package fr.auffredou.transfer.task;

import java.util.Date;
import java.util.concurrent.Callable;


public abstract class TransferTask implements Callable<Long> {

    protected String localBaseDirectory;
    protected String localFile;
    protected String remoteFile;
    protected String remoteBaseDirectory;
    protected String host;
    protected Integer port;
    protected Date startDate;
    protected Date endDate;
    protected String username;
    protected String password;

    public TransferTask host(String host, Integer port) {

        this.host = host;
        this.port = port;
        return this;
    }

    public TransferTask credentials(String username, String password) {
        this.username = username;
        this.password = password;
        return this;
    }

    public TransferTask local(String localBaseDirectory,String localFile) {
        this.localBaseDirectory = localBaseDirectory;
        this.localFile = localFile;
        return this;
    }

    public TransferTask remote(String remoteBaseDirectory, String remoteFile) {
        this.remoteBaseDirectory = remoteBaseDirectory;
        this.remoteFile = remoteFile;
        return this;
    }

    Exception error;

    protected void preExecute() {

    }

    public abstract void execute() throws Exception;

    protected void postExecute() {

    }

    @Override
    public Long call() throws Exception {
        startDate = new Date();
        execute();
        endDate = new Date();
        return endDate.getTime()-startDate.getTime();
    }

}
