package com.tool.writers;

import java.io.IOException;

import com.tool.templates.GitCommit;
import com.tool.templates.TestResult;

public interface ResultsWriter {

    public void writeTestResult(TestResult testResult)  throws IOException;

    public void writeCommit(GitCommit gitCommit) throws IOException;
}
