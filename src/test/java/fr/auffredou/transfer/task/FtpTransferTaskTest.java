package fr.auffredou.transfer.task;

import fr.auffredou.transfer.FtpDummyUserManager;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;

public class FtpTransferTaskTest {

    private static final Logger LOG = LoggerFactory.getLogger(FtpTransferTaskTest.class);

    private static FtpServerFactory serverFactory;
    private static ListenerFactory factory;
    private static FtpServer server;
    private final static FtpDummyUserManager userManager = new FtpDummyUserManager();
    private static int PORT = 2121;

    @BeforeClass
    public static void setup() throws IOException {
        serverFactory = new FtpServerFactory();
        factory = new ListenerFactory();
        factory.setPort(PORT);
        serverFactory.addListener("default", factory.createListener());
        serverFactory.setUserManager(userManager);

        startServerDetectingListeningPort(42);

    }

    @AfterClass
    public static void cleanUp() {
        server.stop();
    }

    private static void startServerDetectingListeningPort(int triesLeft) {
        try {
            server = serverFactory.createServer();
            server.start();
            LOG.info("FTP server port : {}", PORT);
        } catch (FtpException e) {
            if (triesLeft > 0) {
                factory.setPort(PORT++);
                startServerDetectingListeningPort(triesLeft - 1);
            }
        }
    }

    @Test
    public void testFtpTranfert() throws Exception {
        // Given

        ExecutorService execute = Executors.newSingleThreadExecutor();
        TransferTask ftpTask = new FtpTransferTask()
                .host("localhost", PORT)
                .credentials("foo", "foo")
                .local("src/test/data/", "test.txt")
                .remote("/", "some/path/text.txt");
        Future<Long> future = execute.submit(ftpTask);

        // When
        System.out.println("Execution time : " + future.get());

        // Then
        File resultFile = new File(userManager.getUserByName("foo").getHomeDirectory() + ftpTask.remoteBaseDirectory, ftpTask.remoteFile);
        File expectedFile = new File(ftpTask.localBaseDirectory, ftpTask.localFile);
        LOG.info("Transfered file location : {}",resultFile.getAbsolutePath());
        execute.shutdown();

        assertEquals(expectedFile.length(), resultFile.length());

    }

    @Test(expected = ExecutionException.class)
    public void testInvalidFtpTranfert() throws Exception {
        // Given

        ExecutorService execute = Executors.newSingleThreadExecutor();
        TransferTask ftpTask = new FtpTransferTask()
                .host("localhost", PORT)
                .credentials("foo", "foo")
                .local("src/test/data/", "test.txt")
                .remote("/bad/dir", "some/other/path/text.txt");
        Future<Long> future = execute.submit(ftpTask);

        // When
        System.out.println("Execution time : " + future.get());
        execute.shutdown();


    }
}
