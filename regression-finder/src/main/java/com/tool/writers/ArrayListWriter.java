package com.tool.writers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import com.tool.writers.interfaces.ItemWriter;

public class ArrayListWriter<T> implements ItemWriter<T> {
    private ArrayList<T> list;

    private ArrayListWriter() {
        this.list = new ArrayList<>();
    }

    public static <T> ArrayListWriter<T> create(){
        return new ArrayListWriter<>();
    }

    @Override
    public void write(T item) throws IOException {
        list.add(item);
    }

    @Override
    public void writeAll(Collection<T> items) throws IOException {
        this.list.addAll(items);
    }

    public ArrayList<T> getList() {
        return list;
    }

    @Override
    public void close() throws IOException {
        this.list.clear();
    }
}
