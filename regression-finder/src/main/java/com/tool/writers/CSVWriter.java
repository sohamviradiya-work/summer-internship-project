package com.tool.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.tool.templates.CSVItem;

public class CSVWriter<T extends CSVItem> implements ItemWriter<T> {
    private BufferedWriter writer;

    private CSVWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public void write(T item) throws IOException {
        writer.write(item.toCSVString());
        writer.newLine();
        writer.flush();
    }

    public static <T extends CSVItem> CSVWriter<T> create(String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true)); 
        return new CSVWriter<T>(writer);
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }
}