package com.tool.items;

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

    public static boolean compare(TestIndentifier testIdentifier1, TestIndentifier testIdentifier2) {
        return testIdentifier1.getTestClass()==testIdentifier2.getTestClass() && testIdentifier1.getTestMethod()==testIdentifier1.getTestMethod();
    }
}