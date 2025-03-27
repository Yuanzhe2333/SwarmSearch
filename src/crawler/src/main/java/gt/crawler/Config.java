package gt.crawler;

import java.io.IOException;
import java.util.Properties;
import java.io.FileInputStream;

public class Config {
    private static Config instance;
    private Properties config;

    private Config() {
        String configPath = "./config/config.properties";

        config = new Properties();
        try (FileInputStream fis = new FileInputStream(configPath)) {
            config.load(fis);
        } catch (IOException e) {
            System.err.println("Failed to load configuration from " + configPath);
            e.printStackTrace();
            return;
        }
    }

    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    public Properties getConfig() {
        return config;
    }
}
