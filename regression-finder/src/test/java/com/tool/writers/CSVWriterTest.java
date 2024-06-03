package com.tool.writers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tool.interfaces.CSVItem;

import java.io.BufferedWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CSVWriterTest {

    @Mock
    private BufferedWriter bufferedWriter;

    @Mock
    private CSVItem csvItem;

    private CSVWriter<CSVItem> csvWriter;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(csvItem.toCSVString()).thenReturn("item1");
        csvWriter = new CSVWriter<>(bufferedWriter);
    }

    @Test
    void testWriteSingleItem() throws IOException {
        csvWriter.write(csvItem);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bufferedWriter, times(1)).write(captor.capture());
        verify(bufferedWriter, times(1)).newLine();

        assertEquals("item1", captor.getValue());
    }

    @Test
    void testWriteMultipleItems() throws IOException {
        CSVItem item2 = mock(CSVItem.class);
        when(item2.toCSVString()).thenReturn("item2");

        csvWriter.write(csvItem);
        csvWriter.write(item2);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(bufferedWriter, times(2)).write(captor.capture());
        verify(bufferedWriter, times(2)).newLine();

        assertEquals("item1", captor.getAllValues().get(0));
        assertEquals("item2", captor.getAllValues().get(1));
    }
}
