package com.tool.runner;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
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
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;
import org.gradle.tooling.events.test.internal.DefaultTestSkippedResult;
import org.gradle.tooling.model.GradleProject;

import com.tool.writers.ArrayListWriter;
import com.tool.writers.interfaces.ItemWriter;

import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;

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

    private void runTestsForProject(String testProject, HashMap<String, List<String>> testMethods,
            ItemWriter<TestResult> resultsWriter) {
        TestLauncher testLauncher = projectConnection.newTestLauncher();

        for (String testClass : testMethods.keySet()) {
            testLauncher.withTaskAndTestMethods(testProject + ":test", testClass, testMethods.get(testClass));
        }
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                logTest(event, testProject, resultsWriter);
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
        buildLauncher.withArguments("--continue", "--quiet");
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

    private void logTest(ProgressEvent event, String testProjectName, ItemWriter<TestResult> resultsWriter) {
        if (event instanceof DefaultTestFinishEvent) {
            DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event.getDescriptor();

            TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
            String resultString;

            String testClassName = descriptor.getClassName();
            String testMethodName = descriptor.getMethodName();

            if (testClassName == null || testMethodName == null)
                return;

            if (result instanceof DefaultTestFailureResult)
                resultString = "FAILED";
            else if (result instanceof DefaultTestSkippedResult)
                resultString = "SKIPPED";
            else
                resultString = "PASSED";

            testMethodName = testMethodName.replace("(", "").replace(")", "");

            TestResult testResult = new TestResult(testClassName, testMethodName, testProjectName, resultString);

            if (testResult != null) {
                try {
                    resultsWriter.write(testResult);
                } catch (IOException e) {
                    
                }
            }
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

    public static ArrayList<TestIdentifier> evaulateTestResults(ArrayList<TestResult> testResults) throws IOException {
        ArrayList<TestIdentifier> passingTestIdentifiers = new ArrayList<>();

        for (TestResult testResult : testResults) {
            TestIdentifier testIdentifier = testResult.getIdentifier();
            if (testResult.getResult() != TestResult.Result.FAILED)
                passingTestIdentifiers.add(testIdentifier);
        }
        return passingTestIdentifiers;
    }

    public void syncDependencies() {
        BuildLauncher buildLauncher = projectConnection.newBuild();
        buildLauncher.forTasks("dependencies");
        buildLauncher.withArguments("--refresh-dependencies");

        try {
            System.out.println("Syncing Dependencies");
            buildLauncher.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory) {
        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);
        GradleWorker gradleWorker = new GradleWorker(connector.connect());
        return gradleWorker;
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
}
