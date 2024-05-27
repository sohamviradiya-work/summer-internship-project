package com.tool;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.tool.templates.TestResult;

public class CSVWriter {
    public static void writeTestResultsToCSV(ArrayList<TestResult> testResults, String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            
            for (TestResult result : testResults) {
                writer.write(result.getTestClass() + "," + result.getTestMethod() + "," + result.getResult() + "\n");
            }
        }
    }
}

