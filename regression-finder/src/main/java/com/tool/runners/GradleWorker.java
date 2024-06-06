package com.tool.runners;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.model.GradleProject;

import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.interfaces.ItemWriter;

public class GradleWorker {
    private ProjectConnection projectConnection;

    public GradleWorker(ProjectConnection projectConnection) {
        this.projectConnection = projectConnection;
    }

    public void close() {
        this.projectConnection.close();
    }

    public void runTests(List<TestIdentifier> testIdentifiers, ItemWriter<TestResult> resultsWriter) {

        HashMap<String, HashMap<String, List<String>>> testGroups = TestIdentifier.groupByProjectClass(testIdentifiers);

        for (String testProject : testGroups.keySet()) {
            runTestsForProject(testProject, testGroups.get(testProject), resultsWriter);
        }
    }

    private void runTestsForProject(String testProjectName, HashMap<String, List<String>> testMethods,
            ItemWriter<TestResult> resultsWriter) {
        TestLauncher testLauncher = projectConnection.newTestLauncher();

        for (String testClass : testMethods.keySet()) {
            testLauncher.withTaskAndTestMethods(testProjectName + ":test", testClass, testMethods.get(testClass));
        }
        testLauncher.addArguments("--parallel");
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                logTest(event, testProjectName, resultsWriter);
            }
        }, OperationType.TEST);
        try {
            testLauncher.run();
        } catch (Exception e) {

        }
    }

    public void runAlltests(ItemWriter<TestResult> resultsWriter) {
        List<String> subProjects = getSubProjects();
        for (String testProjectName : subProjects) {
            runAlltestsForProject(resultsWriter, testProjectName);
        }
    }

    private void runAlltestsForProject(ItemWriter<TestResult> resultsWriter, String testProjectName) {
        BuildLauncher buildLauncher = projectConnection.newBuild();
        buildLauncher.forTasks(testProjectName + ":test");
        buildLauncher.addArguments("--continue", "--quiet", "--parallel");
        buildLauncher.addProgressListener(new ProgressListener() {

            @Override
            public void statusChanged(ProgressEvent event) {
                logTest(event, testProjectName, resultsWriter);
            }
        }, OperationType.TEST);

        try {
            buildLauncher.run();
        } catch (Exception e) {

        }
    }

    public ArrayList<TestIdentifier> getFailingTests() {

        ArrayList<TestIdentifier> failingTests = new ArrayList<TestIdentifier>();
        ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<TestResult>();

        runAlltests(testResultsWriter);

        ArrayList<TestResult> testResults = testResultsWriter.getList();
        for (TestResult testResult : testResults) {
            if (testResult.getResult() == TestResult.Result.FAILED) {
                failingTests.add(testResult.getIdentifier());
            }
        }
        return failingTests;
    }

    public void syncDependencies() {
        BuildLauncher buildLauncher = projectConnection.newBuild();
        buildLauncher.forTasks("dependencies");
        buildLauncher.addArguments("--refresh-dependencies");

        try {
            System.out.println("Syncing Dependencies");
            buildLauncher.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public List<String> getSubProjects() {

        List<String> subProjects = new ArrayList<>();
        ModelBuilder<GradleProject> modelBuilder = projectConnection.model(GradleProject.class);
        try {
            GradleProject rootProject = modelBuilder.get();
            for (GradleProject subProject : rootProject.getChildren()) {
                subProjects.add(subProject.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return subProjects;
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory) {
        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);
        GradleWorker gradleWorker = new GradleWorker(connector.connect());
        return gradleWorker;
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
