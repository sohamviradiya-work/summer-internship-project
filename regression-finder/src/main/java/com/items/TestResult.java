package com.items;

import java.util.ArrayList;

import com.items.interfaces.CSVItem;

public class TestResult implements CSVItem{
    public enum Result {
        PASSED,
        FAILED,
        SKIPPED
    }

    private Result result;
    private TestIdentifier testIdentifier;

    public TestResult(String testClass, String testMethod,String testProject ,String result) {
        this.testIdentifier = new TestIdentifier(testProject,testClass,testMethod);
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
        return testIdentifier.getTestProject() + "," + testIdentifier.getTestClass() + "," + testIdentifier.getTestMethod() + "," + result.name();
    }

    public Result getResult(){
        return result;
    }

    public TestIdentifier getIdentifier(){
        return testIdentifier;
    }

    public static ArrayList<TestIdentifier> extractFailingTests(ArrayList<TestResult> testResults) {
        ArrayList<TestIdentifier> failingTests = new ArrayList<>();
        for(TestResult testResult:testResults){
            if(testResult.getResult()==TestResult.Result.FAILED)
                failingTests.add(testResult.getIdentifier());
        }
        return failingTests;
    }


    public static ArrayList<TestIdentifier> extractNotFailingTests(ArrayList<TestResult> testResults) {
        ArrayList<TestIdentifier> failingTests = new ArrayList<>();
        for(TestResult testResult:testResults){
            if(testResult.getResult()!=TestResult.Result.FAILED)
                failingTests.add(testResult.getIdentifier());
        }
        return failingTests;
    }
}
