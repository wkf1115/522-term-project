package auto.process;

import auto.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class MonitorAFL implements Runnable{
    private static final Logger logger = LogManager.getLogger(MonitorAFL.class);
    Map<String, Object> yaml;
    CountDownLatch latch;

    public void setParams(Map<String, Object> yaml, CountDownLatch latch) {
        this.yaml =  yaml;
        this.latch = latch;
    }
    @Override
    public void run() {
        String projectPath = (String) yaml.get("projectPath");
        String projectName = (String) yaml.get("projectName");
        //String monitorFile = yaml.get("monitorFile");

        //make afl -C /home/dock/wkf/program/fra-update/
        String instrumentCommand = "make afl -C " +  projectPath + projectName + "/";
        logger.info("exec :" + instrumentCommand);
        ProcessBuilder instrumentPB = new ProcessBuilder("make", "afl", "-C", projectPath + projectName + "/");
        instrumentPB.redirectErrorStream(true);
        Process ipb;
        try {
            ipb = instrumentPB.start();
        } catch (IOException e) {
            logger.error("instrument.start() can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader instrumentBR = new BufferedReader(new InputStreamReader(ipb.getInputStream()));
        try {
            ipb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String runCommand = "make aflfuzz -C " +  projectPath + projectName + "/";
        logger.info("exec " + runCommand);
        ProcessBuilder runPB = new ProcessBuilder("make", "aflfuzz", "-C", projectPath + projectName + "/");
        runPB.redirectErrorStream(true);
        Process rpb;

        try {
            rpb = runPB.start();
        } catch (IOException e) {
            logger.error("instrument.start() can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader runBR = new BufferedReader(new InputStreamReader(rpb.getInputStream()));
        String line;
        try {
            while (!(line = runBR.readLine()).contains("All right - fork server is up.")) {
                logger.info("starting AFL....");
            }
            latch.countDown();
            logger.info("============================================");
            logger.info("AFL is running!");
            logger.info("============================================");
            while ((line = runBR.readLine()) != null) {
                if (line.contains("cycle")) logger.info(line);
            }
        } catch (IOException e) {
            logger.error("BufferedReader.readLine() can not work properly");
            throw new RuntimeException(e);
        } catch (NullPointerException e){
            logger.error("There is something wrong with AFL, please check!");
        }
        try {
            rpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
