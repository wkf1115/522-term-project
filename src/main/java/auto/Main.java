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
        Map<String, String> yaml =  YamlConverter.convert();
        String projectPath = yaml.get("projectPath");
        String projectName = yaml.get("projectName");
        String monitorFile = yaml.get("monitorFile");

        //init
        ReadyForRoll readyForRoll = new ReadyForRoll();
        readyForRoll.setParams(yaml);
        Thread initThread = new Thread(readyForRoll);
        initThread.start();

        try {
            initThread.join();
        } catch (InterruptedException e) {
            logger.error("can not wait for init");
        }
        //start afl
        CountDownLatch latch = new CountDownLatch(1);

        MonitorAFL monitorAFL = new MonitorAFL();
        monitorAFL.setParams(yaml, latch);
        Thread aflThread = new Thread(monitorAFL);
        aflThread.start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }

        logger.info("starting queue monitor ...");

        BlockingQueue<String> monitorQueue = new LinkedBlockingQueue<>(1000);

        INotifyWait iNotifyWait = new INotifyWait();
        iNotifyWait.setParams(yaml, monitorQueue);
        Thread notifyWaitThread = new Thread(iNotifyWait);
        notifyWaitThread.start();

        logger.info("starting daikon monitor ...");

        MonitorDaikon monitorDaikon = new MonitorDaikon();
        monitorDaikon.setParams(yaml, monitorQueue);
        Thread monitorDaikonThread = new Thread(monitorDaikon);
        monitorDaikonThread.start();

    }
}