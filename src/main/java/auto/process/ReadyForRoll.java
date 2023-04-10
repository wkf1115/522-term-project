package auto.process;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReadyForRoll implements Runnable{
    private static final Logger logger = LogManager.getLogger(ReadyForRoll.class);
    Map<String, String> yaml;
    public void setParams(Map<String, String> yaml) {
        this.yaml =  yaml;
    }
    @Override
    public void run() {
        String projectPath = yaml.get("projectPath");
        String projectName = yaml.get("projectName");
        String daikonInstrumentFile = yaml.get("daikonInstrumentFile");

        //mkdir /home/dock/wkf/program/fra-update/daikon-output
        String mkdirCommand = "mkdir " +  projectPath + projectName;
        ProcessBuilder mkdirPB = new ProcessBuilder("mkdir", projectPath + projectName);
        mkdirPB.redirectErrorStream(true);
        Process mpb;
        try {
            mpb = mkdirPB.start();
        } catch (IOException e) {
            logger.error("instrument.start() can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader mkdirBR = new BufferedReader(new InputStreamReader(mpb.getInputStream()));
        String line;

        try {
            while ((line = mkdirBR.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("BufferedReader.readLine() can not work properly");
            throw new RuntimeException(e);
        }

        try {
            mpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //rm /home/dock/wkf/program/fra-update/daikon-output/daikonInstrumentFile.decls / .dtrace
        String rmCommand = "rm " +  projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".decls";
        ProcessBuilder rmPB = new ProcessBuilder("rm", projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".decls",
                                                        projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".dtrace");
        rmPB.redirectErrorStream(true);
        Process rmpb;
        try {
            rmpb = rmPB.start();
        } catch (IOException e) {
            logger.error("remove can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader rmBR = new BufferedReader(new InputStreamReader(rmpb.getInputStream()));

        try {
            while ((line = rmBR.readLine()) != null) {
                logger.info(line);
            }
        } catch (IOException e) {
            logger.error("BufferedReader.readLine() can not work properly");
            throw new RuntimeException(e);
        }

        try {
            rmpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
