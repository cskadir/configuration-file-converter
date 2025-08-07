package com.korkmazkadir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class YamlToProperties extends ConfigurationFileConverterService {
    private static final Logger log = LoggerFactory.getLogger(YamlToProperties.class);

    protected YamlToProperties(String filePath, boolean sortKeys) {
        super(filePath, sortKeys);
    }

    @Override
    protected void readProperties() {
        Map<String, Object> confs = null;
        try (var fis = new FileInputStream(fullFilePath.toFile())) {
            confs = new Yaml().load(fis);
        } catch (IOException e) {
            log.error("Error occurred while reading configuration file: {}", fullFilePath, e);
            throw new RuntimeException(e);
        }
        configurations.putAll(confs);
        if (sortKeys) {
            orderMap(configurations);
            log.info("Ordered configurations by keys");
        }
    }

    @Override
    protected void writeProperties() {
        String configurationFileContent = creteConfigurationFileContent(configurations, "");
        Path yamlFilePath = fullFilePath.getParent().resolve(getFileName().concat(ConfigurationFileType.PROPERTIES.extension));
        try {
            Files.writeString(yamlFilePath, configurationFileContent, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            log.error("Unable to write properties file", e);
            throw new RuntimeException(e);
        }
    }

    private String creteConfigurationFileContent(Map<String, Object> configurations, String prefix) {
        StringBuilder stringBuilder = new StringBuilder();
        configurations.forEach((key, value) -> {
            if (value instanceof Map) {
                stringBuilder.append(creteConfigurationFileContent((Map<String, Object>) value, prefix + key + "."));
            } else {
                if (value instanceof List<?>) {
                    String allValues = ((List<String>) value).stream().collect(Collectors.joining(","));
                    stringBuilder.append(prefix).append(key).append("=").append(allValues).append("\n");
                } else {
                    stringBuilder.append(prefix).append(key).append("=").append(value).append("\n");
                }
            }
        });
        return stringBuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private TreeMap<String, Object> orderMap(Map<String, Object> map) {
        TreeMap<String, Object> orderedConfigurations = new TreeMap<>(map);
        for (Map.Entry<String, Object> entry : orderedConfigurations.entrySet()) {
            if (entry.getValue() instanceof Map) {
                orderedConfigurations.put(entry.getKey(), orderMap((Map<String, Object>) entry.getValue()));
            }
        }
        return orderedConfigurations;
    }
}
