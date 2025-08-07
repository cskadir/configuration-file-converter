package com.korkmazkadir;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public abstract class ConfigurationFileConverterService {

    private static final Logger log = LoggerFactory.getLogger(ConfigurationFileConverterService.class);

    protected final Path fullFilePath;
    protected final boolean sortKeys;
    protected final Map<String, Object> configurations;

    protected ConfigurationFileConverterService(String fullFilePath, boolean sortKeys) {
        this.fullFilePath = Path.of(fullFilePath);
        this.sortKeys = sortKeys;
        this.configurations = sortKeys ? new TreeMap<>() : new LinkedHashMap<>();
        log.info("{} class created", this.getClass().getSimpleName());
    }

    public void convertPropertyFile() {
        log.info("Starting to read configurations from file...");
        readProperties();
        log.info("File read and configurations map filled successfully...");
        log.info("Starting to write configurations to file...");
        writeProperties();
        log.info("New property file created successfully...");

    }

    protected abstract void readProperties();

    protected abstract void writeProperties();

    protected final String getFileName() {
        String fileNameWithExtension = fullFilePath.getFileName().toString();
        var indexOfDot = fileNameWithExtension.lastIndexOf('.');
        if (indexOfDot == -1) {
            return fileNameWithExtension;
        }
        return fileNameWithExtension.substring(0, indexOfDot);
    }
}
