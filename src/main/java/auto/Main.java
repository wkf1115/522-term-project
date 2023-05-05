package auto;

import auto.process.INotifyWait;
import auto.process.MonitorAFL;
import auto.process.MonitorDaikon;
import auto.process.ReadyForRoll;
import auto.util.YamlConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        //get config
        Map<String, Object> yaml =  YamlConverter.convert();
        String projectPath = (String) yaml.get("projectPath");
        String projectName = (String) yaml.get("projectName");
        String monitorFile = (String) yaml.get("monitorFile");

        //init
        ReadyForRoll readyForRoll = new ReadyForRoll();
        readyForRoll.setParams(yaml);
        Thread initThread = new Thread(readyForRoll);
        initThread.setName("init-thread");
        initThread.start();

        try {
            initThread.join();
            logger.info("============================================");
            logger.info("program is ready now");
            logger.info("============================================");
        } catch (InterruptedException e) {
            logger.error("can not wait for init");
        }
        //start afl
        CountDownLatch latch = new CountDownLatch(1);

        MonitorAFL monitorAFL = new MonitorAFL();
        monitorAFL.setParams(yaml, latch);
        Thread aflThread = new Thread(monitorAFL);
        aflThread.setName("afl-thread");
        aflThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        logger.info("============================================");
        logger.info("starting queue monitor ...");
        logger.info("============================================");

        BlockingQueue<String> monitorQueue = new LinkedBlockingQueue<>(1000);

        INotifyWait iNotifyWait = new INotifyWait();
        iNotifyWait.setParams(yaml, monitorQueue);
        Thread notifyWaitThread = new Thread(iNotifyWait);
        notifyWaitThread.setName("monitor-queue");
        notifyWaitThread.start();

        logger.info("============================================");
        logger.info("starting daikon monitor ...");
        logger.info("============================================");

        MonitorDaikon monitorDaikon = new MonitorDaikon();
        monitorDaikon.setParams(yaml, monitorQueue);
        Thread monitorDaikonThread = new Thread(monitorDaikon);
        monitorDaikonThread.setName("daikon-thread");
        monitorDaikonThread.start();

    }
}