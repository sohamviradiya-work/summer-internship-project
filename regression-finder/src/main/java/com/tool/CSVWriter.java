package com.tool;

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

    @Override
    public void writeLine(String line){
        try {
            writer.append(line);
            writer.flush();
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }

    @Override
    public void writeTestResult(TestResult testResult) {
        writeLine(testResult.getTestClass() + "," + testResult.getTestMethod() + "," + testResult.getResult() + "\n");
    }

    @Override
    public void writeCommit(GitCommit gitCommit) {
        writeLine(gitCommit.commitId + "," + gitCommit.parentCommitId + "," + gitCommit.authorMail + "," + gitCommit.branch + "\n");  
    }
}
