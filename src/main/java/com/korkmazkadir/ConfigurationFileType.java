package com.korkmazkadir;

public enum ConfigurationFileType {
    YAML(".yaml"),
    PROPERTIES(".properties");

    public final String extension;

    ConfigurationFileType(String extension) {
        this.extension = extension;
    }
}
