package com.tool.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import com.items.interfaces.CSVItem;
import com.tool.writers.interfaces.ItemWriter;

public class CSVWriter<T extends CSVItem> implements ItemWriter<T> {
    private BufferedWriter writer;

    public CSVWriter(BufferedWriter writer) {
        this.writer = writer;
    }

    @Override
    public void write(T item) throws IOException {
        writer.write(item.toCSVString());
        writer.newLine();
    }

    @Override
    public void close() throws IOException {
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public void writeAll(Collection<T> items) throws IOException {
        for(T item:items){
            this.write(item);
        }
        this.writer.flush();
    }

    public static <T extends CSVItem> CSVWriter<T> create(String filePath) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true)); 
        return new CSVWriter<T>(writer);
    }
}
