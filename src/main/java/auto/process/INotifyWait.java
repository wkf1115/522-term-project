package auto.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class INotifyWait implements Runnable{
    private static final Logger logger = LogManager.getLogger(INotifyWait.class);
    Map<String, Object> yaml;
    BlockingQueue<String> queue;

    public void setParams(Map<String, Object> yaml, BlockingQueue<String> queue) {
        this.yaml =  yaml;
        this.queue = queue;
    }
    @Override
    public void run() {
        String projectPath = (String) yaml.get("projectPath");
        String projectName = (String) yaml.get("projectName");

        //inotifywait -m -e modify /home/dock/wkf/program/fra-update/output/queue/
        String monitorCommand = "inotifywait -m -e modify" +  projectPath + projectName + "/output/queue";
        logger.info("exec " + monitorCommand);
        ProcessBuilder monitorPB = new ProcessBuilder("inotifywait", "-m", "-e", "modify", projectPath + projectName + "/output/queue");
        monitorPB.redirectErrorStream(true);
        Process mpb;
        try {
            mpb = monitorPB.start();
        } catch (IOException e) {
            logger.error("inotifywait can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader monitorBR = new BufferedReader(new InputStreamReader(mpb.getInputStream()));
        String line;

        try {
            while ((line = monitorBR.readLine()) != null) {
                //logger.info(line);
                if(line.contains("MODIFY")){
                    String[] parts = line.split(" ");
                    String lastPart = parts[parts.length - 1];
                    // /home/dock/wkf/program/fra-update/output/queue/fra-update/output/queue/filename
                    queue.put(lastPart);
                    logger.info("queue added 1 file");
                }
            }
        } catch (IOException e) {
            logger.error("BufferedReader.readLine() can not work properly");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            logger.error("BlockingQueue down");
            throw new RuntimeException(e);
        }
        try {
            mpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
