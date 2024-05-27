package com.tool;

import java.io.IOException;

import com.tool.templates.TestResult;

public interface ResultsWriter {

    public void writeTestResult(TestResult testResult);
}
