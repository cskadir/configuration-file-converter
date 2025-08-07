package com.korkmazkadir;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigurationFileConverter {
    private static final Logger log = LoggerFactory.getLogger(ConfigurationFileConverter.class);

    public static void main(String[] args) {
        Thread.currentThread().setName("ConfigurationFileConverterService");
        ConfigurationFileConverterService configurationFileReader;

        if (args.length < 2) {
            log.error("❌ Missing arguments.");
            log.error("Usage: java MyApp <file_path> <sort_keys:true|false>");
            return;
        }
        var fullFilepath = args[0];
        if (fullFilepath == null || fullFilepath.isBlank()) {
            log.error("❌ File path cannot be empty.");
            return;
        }

        boolean sortKeys;
        try {
            sortKeys = Boolean.parseBoolean(args[1]);
        } catch (Exception e) {
            log.error("❌ Invalid value for sortKeys. Use 'true' or 'false'.");
            return;
        }
        log.info("✅ File path received: {}", fullFilepath);
        if (sortKeys) {
            log.info("✅ Keys will sort");
        }

        if (fullFilepath.contains(ConfigurationFileType.PROPERTIES.extension)) {
            configurationFileReader = new PropertiesToYamlFileService(fullFilepath, sortKeys);
        } else {
            configurationFileReader = new YamlToProperties(fullFilepath, sortKeys);
        }
        configurationFileReader.convertPropertyFile();
    }
}