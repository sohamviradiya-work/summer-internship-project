package com.items;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.items.interfaces.CSVItem;
import com.tool.Config;

public class TestResult implements CSVItem {
    public enum Result {
        PASSED,
        FAILED,
        SKIPPED
    }

    private Result result;
    private TestIdentifier testIdentifier;
    private String stackTrace;

    public TestResult(String testClass, String testMethod, String testProject, String result) {
        this.testIdentifier = new TestIdentifier(testProject, testClass, testMethod);
        this.result = parseResult(result);
        this.stackTrace = null;
    }


    public TestResult(String testClass, String testMethod, String testProject, String result,String stackTrace) {
        this.testIdentifier = new TestIdentifier(testProject, testClass, testMethod);
        this.result = parseResult(result);
        this.stackTrace = stackTrace;
    }

    private static Result parseResult(String result) {
        if (result.equalsIgnoreCase("PASSED")) {
            return Result.PASSED;
        } else if (result.equalsIgnoreCase("SKIPPED")) {
            return Result.SKIPPED;
        } else if (result.equalsIgnoreCase("FAILED")) {
            return Result.FAILED;
        } else {
            throw new IllegalArgumentException("Invalid test result: " + result);
        }
    }

    public String toCSVString() {
        return testIdentifier.toCSVString() + ", " + (result.name() == "FAILED" ? Config.ANSI_RED : Config.ANSI_GREEN)
                + result.name() + Config.ANSI_RESET;
    }

    public Result getResult() {
        return result;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public TestIdentifier getIdentifier() {
        return testIdentifier;
    }

    public static HashSet<TestIdentifier> extractFailingTests(List<TestResult> testResults) {
        HashSet<TestIdentifier> failingTests = new HashSet<>();
        for (TestResult testResult : testResults) {
            if (testResult.getResult() == TestResult.Result.FAILED){
                failingTests.add(testResult.getIdentifier());
                System.out.println("Test failed: " + Config.ANSI_RED + testResult.testIdentifier.toCSVString() + Config.ANSI_RESET);
            }
        }
        return failingTests;
    }

    public static HashSet<TestIdentifier> extractNotFailingTests(List<TestResult> testResults,
            ArrayList<TestIdentifier> allTests) {
        HashSet<TestIdentifier> passingTests = new HashSet<>(allTests);
        for (TestResult testResult : testResults) {
            if (testResult.getResult() == TestResult.Result.FAILED)
                passingTests.remove(testResult.getIdentifier());
        }
        return passingTests;
    }
}
