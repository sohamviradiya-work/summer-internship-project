package com.tool.items;

import com.tool.items.interfaces.CSVItem;

public class TestResult implements CSVItem{
    public enum Result {
        PASSED,
        FAILED,
        SKIPPED
    }

    private Result result;
    private TestIdentifier testIdentifier;

    public TestResult(String testClass, String testMethod, String result) {
        this.testIdentifier = new TestIdentifier(testClass,testMethod);
        this.result = parseResult(result);
    }

    private static Result parseResult(String result) {
        if (result.equalsIgnoreCase("PASSED")) {
            return Result.PASSED;
        }else if (result.equalsIgnoreCase("SKIPPED")) {
            return Result.SKIPPED;
        } else if (result.equalsIgnoreCase("FAILED")) {
            return Result.FAILED;
        } else {
            throw new IllegalArgumentException("Invalid test result: " + result);
        }
    }

    public String toCSVString() {
        return testIdentifier.testClass + "," + testIdentifier.testMethod + "," + result.name();
    }

    public Result getResult(){
        return result;
    }

    public TestIdentifier getIdentifier(){
        return testIdentifier;
    }
}
