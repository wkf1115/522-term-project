package auto.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class YamlConverter {
    private static final Logger logger = LogManager.getLogger(YamlConverter.class);

    public static Map<String, String> convert(){
        InputStream inputStream = null;
        try {
            inputStream = Files.newInputStream(Paths.get("config.yml"));
        } catch (IOException e) {
            logger.error("Can't find the config path");
            throw new RuntimeException(e);
        }
        Yaml yaml = new Yaml();

        Map<String, String> data = yaml.load(inputStream);

        logger.info("Read config file successfully!");
        logger.info("The config information is as follows: " + data);
        return data;
    }


}


