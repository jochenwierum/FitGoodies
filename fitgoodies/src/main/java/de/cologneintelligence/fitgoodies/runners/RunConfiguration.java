package de.cologneintelligence.fitgoodies.runners;

import de.cologneintelligence.fitgoodies.file.FileInformation;

public class RunConfiguration {
    private FileInformation[] source;
    private String destination;
    private String encoding;

    public void setSource(FileInformation[] source) {
        this.source = source;
    }

    public FileInformation[] getSources() {
        return source;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestination() {
        return destination;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }
}
