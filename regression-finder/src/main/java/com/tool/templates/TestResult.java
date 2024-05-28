package com.tool.templates;

public class TestResult {
    public enum Result {
        PASSED,
        FAILED
    }

    public final class TestIndentifier {
        public String testClass;
        public String testMethod;

        public TestIndentifier(String testClass, String testMethod) {
            this.testClass = testClass;
            this.testMethod = testMethod;
        }
    }

    private Result result;
    private TestIndentifier testIndentifier;

    public TestResult(String testClass, String testMethod, String result) {
        this.testIndentifier = new TestIndentifier(testClass,testMethod);
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
        return testIndentifier.testClass + "," + testIndentifier.testMethod + "," + result.name();
    }

    public Result getResult(){
        return result;
    }

    public TestIndentifier getUniqueIdentifier(){
        return testIndentifier;
    }
}
