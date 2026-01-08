package com.beldenDNE.config;

import com.beldenDNE.constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class propertiesHandler {
    private static final Logger logger = LoggerFactory.getLogger(propertiesHandler.class);
    public static final String environment;
    private static final String propFileName = "src/test/resources/properties";

    static {
        try {
            String env = System.getProperty("env");
            environment = env == null || env.isEmpty() ? getProperties("config").getProperty(constants.ENV) : env;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties getProperties(String name) throws IOException {
        if (name.equals("data")) {
            name = envProperties().get("dataProperties");
        } else {
            name = propFileName + "/" + name + ".properties";
        }

        Properties prop = new Properties();
        FileReader reader = new FileReader(name);
        prop.load(reader);
        reader.close();
        return prop;
    }

    public static HashMap<String, String> envProperties() {
        HashMap<String, String> prop = new HashMap<>();
        switch (environment) {
            case "dev":
                prop.put("dataProperties", propFileName + "/devData.properties");
                break;
            case "prod":
                prop.put("dataProperties", propFileName + "/prodData.properties");
                break;
            default:
                logger.error(environment + " value is not valid, try dev, qa, stg or prod values");
        }
        return prop;
    }

    public static HashMap<String, String> paths() {
        HashMap<String, String> prop = new HashMap<>();
        switch (environment) {
            case "dev":
                prop.put("baseURL", constants.DEV_BASE_URL);
                break;
            case "prod":
                prop.put("baseURL", constants.PROD_BASE_URL);
            default:
                logger.error(environment + " value is not valid, try dev, qa, stg or prod values");
        }
        return prop;
    }
}