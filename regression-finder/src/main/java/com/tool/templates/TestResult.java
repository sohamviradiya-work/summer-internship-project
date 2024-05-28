package com.tool.templates;

public class TestResult {
    public enum Result {
        PASSED,
        FAILED
    }

    private String testClass;
    private String testMethod;
    private Result result;

    public TestResult(String testClass, String testMethod, String result) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.result = parseResult(result);
    }

    private Result parseResult(String result) {
        if (result.equalsIgnoreCase("PASSED")) {
            return Result.PASSED;
        } else if (result.equalsIgnoreCase("FAILED")) {
            return Result.FAILED;
        } else {
            throw new IllegalArgumentException("Invalid test result: " + result);
        }
    }

    public String toCSVString() {
        return testClass + "," + testMethod + "," + result.name();
    }

    public Result getResult(){
        return result;
    }
}
