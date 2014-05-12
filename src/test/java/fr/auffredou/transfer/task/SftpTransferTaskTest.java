package fr.auffredou.transfer.task;


import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class SftpTransferTaskTest {

    private static final Logger LOG = LoggerFactory.getLogger(SftpTransferTaskTest.class);
    private static SshServer sshd;
    private static int PORT;

    @BeforeClass
    public static void startServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();

        sshd.setPasswordAuthenticator(new PasswordAuthenticator() {
            public boolean authenticate(String username, String password, ServerSession session) {
                return username != null && username.equals(password);
            }
        });
        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider(System.getProperty("java.io.tmpdir") + UUID.randomUUID()));

        CommandFactory myCommandFactory = new CommandFactory() {
            public Command createCommand(String command) {
                return null;
            }
        };
        sshd.setCommandFactory(new ScpCommandFactory(myCommandFactory));
        List<NamedFactory<Command>> namedFactoryList = new ArrayList<>();
        namedFactoryList.add(new SftpSubsystem.Factory());

        sshd.setSubsystemFactories(namedFactoryList);
        sshd.start();
        PORT = sshd.getPort();

    }

    @Test
    public void testTransfer() throws Exception {
        ExecutorService execute = Executors.newSingleThreadExecutor();
        TransferTask sftpTask = new SftpTransferTask()
                .host("localhost", PORT)
                .credentials("foo", "foo")
                .local("src/test/data/", "test.txt")
                //unjailed user
                .remote(System.getProperty("java.io.tmpdir"), "some/path/test-sftp.txt");
        Future<Long> future = execute.submit(sftpTask);

        // When
        System.out.println("Execution time : " + future.get());

        // Then
        File resultFile = new File(sftpTask.remoteBaseDirectory, sftpTask.remoteFile);
        File expectedFile = new File(sftpTask.localBaseDirectory, sftpTask.localFile);
        LOG.info("Transfered file location : {}", resultFile.getAbsolutePath());
        execute.shutdown();

        assertEquals(expectedFile.length(), resultFile.length());

    }

    @AfterClass
    public static void stopServer() throws InterruptedException {
        if (sshd != null) {
            sshd.stop(true);
        }
    }

}
