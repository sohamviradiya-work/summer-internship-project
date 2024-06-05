package com.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class TestIdentifier implements Comparable<TestIdentifier> {
    private String testClass;
    private String testMethod;
    private String testProject;

    public TestIdentifier(String testClass, String testMethod, String testProject) {
        this.testClass = testClass;
        this.testMethod = testMethod;
        this.testProject = testProject;
    }

    public String getTestClass() {
        return testClass;
    }

    public String getTestProject() {
        return testProject;
    }

    public String getTestMethod() {
        return testMethod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        TestIdentifier that = (TestIdentifier) o;
        return this.testClass.equals(that.testClass) && this.testMethod.equals(that.testMethod)
                && this.testProject.equals(that.testProject);
    }

    @Override
    public int compareTo(TestIdentifier other) {
        int projectComparison = this.testProject.compareTo(other.testProject);
        if (projectComparison != 0) {
            return projectComparison;
        }
        int classComparison = this.testClass.compareTo(other.testClass);
        if (classComparison != 0) {
            return classComparison;
        }
        return this.testMethod.compareTo(other.testMethod);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testClass, testMethod, testProject);
    }

    public static HashMap<String, HashMap<String, List<String>>> groupByProjectClass(
            List<TestIdentifier> testIdentifiers) {
        HashMap<String, HashMap<String, List<String>>> projectWiseTestGroups = new HashMap<>();

        for (TestIdentifier testIdentifier : testIdentifiers) {

            projectWiseTestGroups.computeIfAbsent(testIdentifier.getTestProject(), k -> new HashMap<>())
                    .computeIfAbsent(testIdentifier.getTestClass(), k -> new ArrayList<>())
                    .add(testIdentifier.getTestMethod());
        }

        return projectWiseTestGroups;
    }
}