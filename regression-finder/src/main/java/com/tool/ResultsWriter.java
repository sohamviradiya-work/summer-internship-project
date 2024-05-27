package com.tool;

import com.tool.templates.GitCommit;
import com.tool.templates.TestResult;

public interface ResultsWriter {

    public void writeLine(String line);

    public void writeTestResult(TestResult testResult);

    public void writeCommit(GitCommit gitCommit);
}
