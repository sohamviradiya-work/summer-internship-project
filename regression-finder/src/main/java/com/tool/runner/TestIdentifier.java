package com.tool.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class TestIdentifier {
    private String testClass;
    private String testMethod;
    private String testProject;

    public TestIdentifier(String testClass, String testMethod,String testProject) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.testProject = testProject;
    }

    public String getTestClass(){
        return testClass;
    }

    public String getTestProject(){
        return testProject;
    }

    public String getTestMethod(){
        return testMethod;
    }

    public static boolean compare(TestIdentifier testIdentifier1, TestIdentifier testIdentifier2) {
        return testIdentifier1.getTestClass()==testIdentifier2.getTestClass() && testIdentifier1.getTestMethod()==testIdentifier1.getTestMethod() && testIdentifier1.getTestProject()==testIdentifier2.getTestProject();
    }

    public static HashMap<String,HashMap<String,List<String>>> groupByProjectClass(List<TestIdentifier> testIdentifiers){
        HashMap<String,HashMap<String,List<String>>> projectWiseTestGroups = new HashMap<>();

        for(TestIdentifier testIdentifier:testIdentifiers){
            
            projectWiseTestGroups.computeIfAbsent(testIdentifier.getTestProject(), k -> new HashMap<>())
                .computeIfAbsent(testIdentifier.getTestClass(), k -> new ArrayList<>())
                .add(testIdentifier.getTestMethod());
        }

        return projectWiseTestGroups;
    }
}