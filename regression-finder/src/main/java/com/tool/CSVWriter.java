package com.tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import com.tool.templates.TestResult;

public class CSVWriter implements ResultsWriter {

    private String filePath;
    private BufferedWriter writer;

    CSVWriter(String filePath,BufferedWriter writer){
        this.filePath = filePath;
        this.writer = writer;
    }

    public void close() throws IOException {
        this.writer.close();
    }

    public static CSVWriter createNewWriter(String filePath) throws IOException{
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
        return new CSVWriter(filePath, bufferedWriter);
    }

    public void writeTestResult(TestResult testResult) throws IOException {
        writer.append(testResult.getTestClass() + "," + testResult.getTestMethod() + "," + testResult.getResult() + "\n");
    }
}

