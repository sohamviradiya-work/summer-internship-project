package com.tool.templates;

public final class TestIndentifier {
    String testClass;
    String testMethod;

    public TestIndentifier(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public String getTestClass(){
        return testClass;
    }

    public String getTestMethod(){
        return testMethod;
    }
}