package com.korkmazkadir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Stream;


public class PropertiesToYamlFileService extends ConfigurationFileConverterService {

    private static final Logger log = LoggerFactory.getLogger(PropertiesToYamlFileService.class);

    public PropertiesToYamlFileService(String filePath, boolean sortKeys) {
        super(filePath, sortKeys);
    }

    @Override
    protected void readProperties() {
        try (Stream<String> lines = Files.lines(fullFilePath)) {
            lines.forEach(this::readProperty);
            log.info("File read and configurations map filled successfully...");
        } catch (IOException e) {
            log.error("Error occurred while reading configuration file: {}", fullFilePath, e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void readProperty(String propertyLine) {
        String[] keyValue = propertyLine.split("=", 2);
        if (keyValue.length != 2) {
            throw new IllegalArgumentException("Invalid line format (missing '='): " + propertyLine);
        }

        String rawKey = keyValue[0].trim();
        String rawValue = keyValue[1].trim();

        Object value = rawValue.contains(",")
                ? Arrays.stream(rawValue.split("\\s*,\\s*"))
                .map(String::trim)
                .toList()
                : rawValue;

        String[] keys = rawKey.split("\\.");
        Map<String, Object> currentMap = configurations;

        var i = 0;
        while (i < keys.length - 1) {
            String key = keys[i];
            currentMap = (Map<String, Object>) currentMap
                    .computeIfAbsent(key, k -> sortKeys ?
                            new TreeMap<>() :
                            new HashMap<>());
            i++;
        }
        String lastKey = keys[keys.length - 1];
        currentMap.put(lastKey, value);
        if (sortKeys) {
            log.info("Ordered configurations by keys");
        }
    }

    @Override
    protected void writeProperties() {
        String configurationFileContent = creteConfigurationFileContent(configurations, 0);
        Path propertiesFilePath = fullFilePath.getParent().resolve(getFileName().concat(ConfigurationFileType.YAML.extension));
        try {
            Files.writeString(propertiesFilePath, configurationFileContent, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Unable to write properties file", e);
            throw new RuntimeException(e);
        }
    }

    private String creteConfigurationFileContent(Map<String, Object> map, int indentLevel) {
        StringBuilder yaml = new StringBuilder();
        String indent = "  ".repeat(indentLevel);

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                yaml.append(indent).append(key).append(":\n");
                yaml.append(creteConfigurationFileContent((Map<String, Object>) value, indentLevel + 1));
            } else if (value instanceof Collection) {
                yaml.append(indent).append(key).append(":\n");
                for (Object item : (Collection<?>) value) {
                    yaml.append(indent).append("  - ").append(item).append("\n");
                }
            } else {
                yaml.append(indent).append(key).append(": ").append(value).append("\n");
            }
        }
        return yaml.toString();
    }
}
