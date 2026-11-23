package com.github.artemboldirew.lmska.bring;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Класс обработчик конфигурационного файла
 */
public class PropertiesConfigHandler {
//    Конфигурается намеренно задается только одним .properties файлом, для упрощения реализации,
//    ведь большее для задачи и не нужно
    private static final String CONFIG_FILE = "application.properties";

    public Properties load() {
        Properties properties = new Properties();

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                throw new IllegalStateException("Configuration file not found: " + CONFIG_FILE);
            }

            properties.load(stream);
            return properties;

        } catch (IOException e) {
            throw new IllegalStateException("Failed to load configuration: " + CONFIG_FILE, e);
        }
    }
}