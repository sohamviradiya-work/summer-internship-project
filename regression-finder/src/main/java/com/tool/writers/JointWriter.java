package com.tool.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.tool.writers.interfaces.ItemWriter;

public class JointWriter<T> implements ItemWriter<T> {

    ArrayList<ItemWriter<T>> writers;

    private JointWriter() {
        this.writers = new ArrayList<>();
    }

    public static <T> JointWriter<T> create(){
        return new JointWriter<>();
    }

    public void addWriter(ItemWriter<T> writer){
        this.writers.add(writer);
    }

    @Override
    public void write(T item) throws IOException {
        for (ItemWriter<T> writer : writers)
            writer.write(item);
    }

    @Override
    public void writeAll(Collection<T> items) throws IOException {
        for (ItemWriter<T> writer : writers)
            writer.writeAll(items);
    }

    @Override
    public void close() throws IOException {
        for (ItemWriter<T> writer : writers) 
            writer.close();
    }

}
