package com.tool.writers;

import java.io.IOException;
import java.util.ArrayList;

import com.tool.writers.interfaces.ItemWriter;

public class ArrayListWriter<T> implements ItemWriter<T> {
    private ArrayList<T> list;

    public ArrayListWriter() {
        this.list = new ArrayList<>();
    }

    @Override
    public void write(T item) throws IOException {
        list.add(item);
    }

    public ArrayList<T> getList() {
        return list;
    }
}
