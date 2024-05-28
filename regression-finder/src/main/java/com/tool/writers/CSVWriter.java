package com.tool.writers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.tool.templates.GitCommit;
import com.tool.templates.TestResult;

public class CSVWriter implements ResultsWriter {

    private BufferedWriter writer;

    CSVWriter(String filePath, BufferedWriter writer) {
        this.writer = writer;
    }

    public void close() throws IOException {
        writer.close();
    }

    public static CSVWriter createNewWriter(String filePath) throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
        return new CSVWriter(filePath, bufferedWriter);
    }

    public void writeLine(String line) throws IOException {
        writer.append(line);
        writer.flush();
    }

    @Override
    public void writeTestResult(TestResult testResult) throws IOException {
        writeLine(testResult.getTestClass() + "," + testResult.getTestMethod() + "," + testResult.getResult() + "\n");
    }

    @Override
    public void writeCommit(GitCommit gitCommit) throws IOException {
        writeLine(gitCommit.commitId + "," + gitCommit.parentCommitId + "," + gitCommit.authorMail + ","
                + gitCommit.branch + "," + gitCommit.message + "\n");
    }
}
