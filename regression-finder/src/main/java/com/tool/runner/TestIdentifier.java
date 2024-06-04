package com.tool.runner;

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
            if(!projectWiseTestGroups.containsKey(testIdentifier.getTestProject())){
                HashMap<String,List<String>> testGroup = new HashMap<>();
                projectWiseTestGroups.put(testIdentifier.getTestProject(),testGroup);
            }
            HashMap<String,List<String>> classWiseTestGroup = projectWiseTestGroups.get(testIdentifier.getTestProject());
            if(!classWiseTestGroup.containsKey(testIdentifier.getTestClass())){
                classWiseTestGroup.put(testIdentifier.getTestClass(), List.of());
            }
            classWiseTestGroup.get(testIdentifier.getTestClass()).add(testIdentifier.getTestMethod());
        }

        return projectWiseTestGroups;
    }
}