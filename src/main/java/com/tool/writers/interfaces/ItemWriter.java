package com.tool.writers.interfaces;

import java.io.IOException;

public interface ItemWriter<T> {
    void write(T item) throws IOException;
}
