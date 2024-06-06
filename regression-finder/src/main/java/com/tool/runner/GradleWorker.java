package com.tool.runner;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.events.ProgressEvent;

import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.interfaces.ItemWriter;

public class GradleWorker {
    ProjectManager projectManager;

    public GradleWorker(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void close() {
        this.projectManager.close();
    }

    public void runTests(List<TestIdentifier> testIdentifiers, ItemWriter<TestResult> resultsWriter) {
        HashMap<String, HashMap<String, List<String>>> testGroups = TestIdentifier.groupByProjectClass(testIdentifiers);
        for (String testProject : testGroups.keySet()) {
            runTestsForProject(testProject, testGroups.get(testProject), resultsWriter);
        }
    }

    private void runTestsForProject(String testProjectName, HashMap<String, List<String>> testMethods,
            ItemWriter<TestResult> resultsWriter) {
        ProjectTester projectTester = ProjectTester.mountProjectTester(projectManager);
        ArrayList<ProgressEvent> events = projectTester.runTestsForProject(testProjectName, testMethods);
        logTests(testProjectName, resultsWriter, events);
    }

    private void runAlltestsForProject(ItemWriter<TestResult> resultsWriter, String testProjectName) {
        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager);
        ArrayList<ProgressEvent> events = projectBuilder.runAlltestsForProject(testProjectName);
        logTests(testProjectName, resultsWriter, events);
    }

    public ArrayList<TestIdentifier> getFailingTests() {

        ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<TestResult>();

        List<String> subProjects = projectManager.getSubProjects();
        for (String testProjectName : subProjects) {
            runAlltestsForProject(testResultsWriter, testProjectName);
        }

        ArrayList<TestResult> testResults = testResultsWriter.getList();
        ArrayList<TestIdentifier> failingTests = new ArrayList<TestIdentifier>();

        for (TestResult testResult : testResults) {
            if (testResult.getResult() == TestResult.Result.FAILED) {
                failingTests.add(testResult.getIdentifier());
            }
        }
        return failingTests;
    }

    public void syncDependencies() {
        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager);
        projectBuilder.syncDependencies();
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory) {
        return new GradleWorker(ProjectManager.mountGradleProject(gradleVersion, directory));
    }

    private static void logTests(String testProjectName, ItemWriter<TestResult> resultsWriter,
            ArrayList<ProgressEvent> events) {
        for (ProgressEvent event : events) {
            logTest(event, testProjectName, resultsWriter);
        }
    }

    private static void logTest(ProgressEvent event, String testProjectName, ItemWriter<TestResult> resultsWriter) {
        TestResult testResult = TestResult.extractResult(event, testProjectName, resultsWriter);
        if (testResult != null) {
            try {
                resultsWriter.write(testResult);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
