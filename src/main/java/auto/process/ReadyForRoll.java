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
    Map<String, Object> yaml;
    public void setParams(Map<String, Object> yaml) {
        this.yaml =  yaml;
    }
    @Override
    public void run() {
        String projectPath = (String) yaml.get("projectPath");
        String projectName = (String) yaml.get("projectName");
        String daikonInstrumentFile = (String) yaml.get("daikonInstrumentFile");

        //mkdir /home/dock/wkf/program/fra-update/daikon-output
        String mkdirCommand = "mkdir " +  projectPath + projectName + "/daikon-output";
        logger.info("exec : " + mkdirCommand);
        ProcessBuilder mkdirPB = new ProcessBuilder("mkdir", projectPath + projectName + "/daikon-output");
        mkdirPB.redirectErrorStream(true);
        Process mpb;
        try {
            mpb = mkdirPB.start();
        } catch (IOException e) {
            logger.error("mkdir() can not work properly");
            throw new RuntimeException(e);
        }

        try {
            mpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //rm -rf --no-preserve-root /home/dock/wkf/program/fra-update/output
        String rmdirCommand = "rm -rf --no-preserve-root " +  projectPath + projectName + "/output";
        logger.info("exec : " + rmdirCommand);
        ProcessBuilder rmdirPB = new ProcessBuilder("rm", "-rf", "--no-preserve-root" , projectPath + projectName + "/output");
        rmdirPB.redirectErrorStream(true);
        Process rpb;
        try {
            rpb = rmdirPB.start();
        } catch (IOException e) {
            logger.error("rm -rf can not work properly");
            throw new RuntimeException(e);
        }

        try {
            rpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        //rm /home/dock/wkf/program/fra-update/daikon-output/daikonInstrumentFile.decls / daikonInstrumentFile.dtrace
        String rmCommand = "rm " +  projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".decls";
        logger.info("exec : " + rmCommand);
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

        try {
            rmpb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //rm -rf --no-preserve-root res
        String rmresCommand = "rm -rf --no-preserve-root res";
        logger.info("exec : " + rmresCommand);
        ProcessBuilder rmresPB = new ProcessBuilder("rm", "-rf", "--no-preserve-root" , "res");
        rmresPB.redirectErrorStream(true);
        Process rmrespb;
        try {
            rmrespb = rmresPB.start();
        } catch (IOException e) {
            logger.error("rm -rf res can not work properly");
            throw new RuntimeException(e);
        }

        try {
            rmrespb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //mkdir res
        String mkresCommand = "mkdir res";
        logger.info("exec : " + mkresCommand);
        ProcessBuilder mkresPB = new ProcessBuilder("mkdir", "res");
        rmPB.redirectErrorStream(true);
        Process mkrespb;
        try {
            mkrespb = mkresPB.start();
        } catch (IOException e) {
            logger.error("mkdir res can not work properly");
            throw new RuntimeException(e);
        }

        try {
            mkrespb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
