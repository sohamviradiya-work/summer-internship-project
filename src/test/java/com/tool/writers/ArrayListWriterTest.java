package com.tool.writers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

public class ArrayListWriterTest {

    @Test
    void testNoWrite() {
        ArrayListWriter<String> writer = new ArrayListWriter<>();
        assertTrue(writer.getList().isEmpty(), "List should be empty when no items are written");
    }

    @Test
    void testSingleWrite() throws IOException {
        ArrayListWriter<String> writer = new ArrayListWriter<>();
        writer.write("item1");
        assertEquals(1, writer.getList().size(), "List should contain one item after one write");
        assertEquals("item1", writer.getList().get(0), "The item in the list should be 'item1'");
    }

    @Test
    void testMultipleWrite() throws IOException {
        ArrayListWriter<String> writer = new ArrayListWriter<>();
        writer.write("item1");
        writer.write("item2");
        writer.write("item3");
        assertEquals(3, writer.getList().size(), "List should contain three items after three writes");
        assertEquals("item1", writer.getList().get(0), "The first item in the list should be 'item1'");
        assertEquals("item2", writer.getList().get(1), "The second item in the list should be 'item2'");
        assertEquals("item3", writer.getList().get(2), "The third item in the list should be 'item3'");
    }
}
