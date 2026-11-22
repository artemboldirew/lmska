package com.github.artemboldirew.lmska.bring;

import java.util.Locale;
import java.util.Properties;

/**
 * Класс окружение, откуда будут браться параметры подключения
 */
public class Environment {
    private final Properties properties;

    public Environment() {
        this.properties = new PropertiesConfigHandler().load();
    }

    public String getProperty(String key) {
        String value = System.getProperty(key);

        if (value != null) {
            return value;
        }

        String envKey = key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');

        value = System.getenv(envKey);
        return value != null
                ? value
                : properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }
}
