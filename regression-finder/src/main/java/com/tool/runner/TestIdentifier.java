package com.tool.runner;

public final class TestIdentifier {
    String testClass;
    String testMethod;

    public TestIdentifier(String testClass, String testMethod) {
        this.testClass = testClass;
        this.testMethod = testMethod;
    }

    public String getTestClass(){
        return testClass;
    }

    public String getTestMethod(){
        return testMethod;
    }

    public static boolean compare(TestIdentifier testIdentifier1, TestIdentifier testIdentifier2) {
        return testIdentifier1.getTestClass()==testIdentifier2.getTestClass() && testIdentifier1.getTestMethod()==testIdentifier1.getTestMethod();
    }
}