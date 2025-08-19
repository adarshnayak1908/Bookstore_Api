// File: src/test/java/util/ConfigReader.java
package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {
    private final Properties properties = new Properties();

    public ConfigReader(String filePath) {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Could not load config file: " + filePath, e);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}