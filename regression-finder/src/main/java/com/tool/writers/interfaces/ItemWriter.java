package com.tool.writers.interfaces;

import java.io.IOException;
import java.util.Collection;

public interface ItemWriter<T> {
    void write(T item) throws IOException;
    void writeAll(Collection<T> items) throws IOException;
    void close() throws IOException;
}
