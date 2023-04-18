package auto.process;

import auto.data.Invariance;
import auto.util.CompareInvarince;
import auto.util.GenerateInvarince;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

public class MonitorDaikon implements Runnable{
    private static final Logger logger = LogManager.getLogger(MonitorDaikon.class);
    ConcurrentLinkedDeque<Invariance> oldInvariances;
    ConcurrentLinkedDeque<Invariance> newInvariances;
    Map<String, String> yaml;
    BlockingQueue<String> queue;
    public void setParams(Map<String, String> yaml, BlockingQueue<String> queue) {
        this.yaml =  yaml;
        this.queue = queue;
    }
    @Override
    public void run() {
        String projectPath = yaml.get("projectPath");
        String projectName = yaml.get("projectName");
        String daikonInstrumentFile = yaml.get("daikonInstrumentFile");

        oldInvariances = new ConcurrentLinkedDeque<>();

        int times = 0;

        //start instrument
        // gcc -gdwarf-2 -no-pie fra-update.c -o fra-update-daikon
        String instrumentCommand = "gcc -gdwarf-2 -no-pie " +  projectName + ".c -o " + daikonInstrumentFile;
        logger.info("exec " + instrumentCommand);
        ProcessBuilder instrumentPB = new ProcessBuilder("gcc", "-gdwarf-2", "-no-pie", projectPath + projectName + "/" + projectName + ".c", "-o", projectPath + projectName + "/" + daikonInstrumentFile);
        instrumentPB.redirectErrorStream(true);
        Process ipb;
        try {
            ipb = instrumentPB.start();
        } catch (IOException e) {
            logger.error("instrument.start() can not work properly");
            throw new RuntimeException(e);
        }

        BufferedReader instrumentBR = new BufferedReader(new InputStreamReader(ipb.getInputStream()));
        String line;

        try {
            ipb.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("Start invariant generation now");

        while (true) {
            StringBuffer sbInput = new StringBuffer();
            String fileName = queue.poll();
            if (fileName != null) {

                File file = new File(projectPath + projectName + "/output/queue/" + fileName);

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                    String fileLine;
                    while ((fileLine = br.readLine()) != null) {
                        sbInput.append(fileLine);
                    }
                    br.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                //start trace
                //kvasir-dtrace --dtrace-append --dtrace-file=daikon-output/fra-update-daikon.dtrace ./fra-update-daikon
                String traceCommand = "kvasir-dtrace --dtrace-append --dtrace-file=" + projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".dtrace" + projectPath + projectName + "/" + daikonInstrumentFile;
                logger.info("exec " + traceCommand);
                ProcessBuilder tracePB = new ProcessBuilder("kvasir-dtrace", "--dtrace-append", "--dtrace-file=" + projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".dtrace", "--decls-file=" + projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".decls", projectPath + projectName + "/" + daikonInstrumentFile);
                tracePB.redirectErrorStream(true);
                Process tpb;
                try {
                    tpb = tracePB.start();
                } catch (IOException e) {
                    logger.error("trace.start() can not work properly");
                    throw new RuntimeException(e);
                }

                logger.error("now dealing with input : " + sbInput);
                OutputStream inputpb = tpb.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(inputpb);
                BufferedWriter bw = new BufferedWriter(osw);

                try {
                    bw.write(sbInput.toString());
                    bw.newLine();
                    bw.flush();
                    tpb.waitFor();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                logger.info("============================================");
                logger.info("Finish trace : " + fileName);
                logger.info("============================================");

                //java -Xmx3600m -cp $DAIKONDIR/daikon.jar daikon.Daikon daikon-output/fra1.dtrace daikon-output/fra-update-daikon.decls
                String analysisCommand = "java -Xmx3600m -cp $DAIKONDIR/daikon.jar daikon.Daikon daikon-output/" + daikonInstrumentFile + ".dtrace " + projectPath + projectName + "/daikon-output/" + daikonInstrumentFile + ".decls";
                logger.info("exec " + analysisCommand);
                ProcessBuilder analysisPB = new ProcessBuilder("java", "-Xmx3600m", "-cp", "/home/daikonparent/daikon-5.8.16/daikon.jar", "daikon.Daikon", projectPath + projectName + "/" + "daikon-output/" + daikonInstrumentFile + ".dtrace", projectPath + projectName + "/" + "daikon-output/" + daikonInstrumentFile + ".decls");
                analysisPB.redirectErrorStream(true);
                Process apb;
                try {
                    apb = analysisPB.start();
                } catch (IOException e) {
                    logger.error("daikon analysis can not work properly");
                    throw new RuntimeException(e);
                }

                BufferedReader analysisBR = new BufferedReader(new InputStreamReader(apb.getInputStream()));
                StringBuffer sb = new StringBuffer();

                try {
                    while ((line = analysisBR.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (IOException e) {
                    logger.error("BufferedReader.readLine() can not work properly");
                    throw new RuntimeException(e);
                }
                try {
                    apb.waitFor();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                newInvariances = GenerateInvarince.generate(sb.toString());

                if (!CompareInvarince.compare(oldInvariances, newInvariances)) {
                    //save input
                    try {
                        FileWriter fw = new FileWriter("res/interesting_input" + ++times + ".txt", true);
                        BufferedWriter bufferedWriter = new BufferedWriter(fw);
                        bufferedWriter.write(sbInput.toString() + "\n");
                        bufferedWriter.write("============================================\n");
                        if(oldInvariances.peek() == null){
                            bufferedWriter.write("null");
                        }else bufferedWriter.write(oldInvariances.peek().getOriginalType());
                        bufferedWriter.write("============================================\n");
                        bufferedWriter.write("============================================\n");
                        if(newInvariances.peek() == null){
                            bufferedWriter.write("null");
                        }else bufferedWriter.write(newInvariances.peek().getOriginalType());
                        bufferedWriter.write("============================================\n");
                        bufferedWriter.flush();
                        bufferedWriter.close();
                        logger.info("input updated ! By using" + fileName);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                        oldInvariances = newInvariances;
                }
            }
        }
    }
}
