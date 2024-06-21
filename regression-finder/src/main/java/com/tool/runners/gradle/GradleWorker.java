package com.tool.runners.gradle;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;
import org.gradle.tooling.events.test.internal.DefaultTestSkippedResult;

import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.Config;
import com.tool.writers.ArrayListWriter;

public class GradleWorker {
    ProjectManager projectManager;
    private OutputStream logStream;

    public GradleWorker(ProjectManager projectManager, OutputStream logStream) {
        this.projectManager = projectManager;
        this.logStream = logStream;
    }

    public void close() {
        this.projectManager.close();
    }

    public ArrayList<TestResult> runTests(List<TestIdentifier> testIdentifiers) throws IOException {
        HashMap<String, HashMap<String, List<String>>> testGroups = TestIdentifier.groupByProjectClass(testIdentifiers);
        ArrayListWriter<TestResult> testResultsWriter = ArrayListWriter.create();
        for (String testProjectName : testGroups.keySet()) {
            testResultsWriter.writeAll(runTestsForProject(testProjectName, testGroups.get(testProjectName)));
        }

        return testResultsWriter.getList();
    }

    private ArrayList<TestResult> runTestsForProject(String testProjectName,
            HashMap<String, List<String>> testMethods) {
        ProjectTester projectTester = ProjectTester.mountProjectTester(projectManager, logStream);
        ArrayList<ProgressEvent> events = projectTester.runTestsForProject(testProjectName, testMethods);
        return extractResults(testProjectName, events);
    }

    public ArrayList<TestResult> runAllTests() throws IOException {

        List<String> subProjects = projectManager.getSubProjects();
        ArrayListWriter<TestResult> testResultsWriter = ArrayListWriter.create();
        for (String testProjectName : subProjects) {
            testResultsWriter.writeAll(runAllTestsForProject(testProjectName));
        }

        ArrayList<TestResult> testResults = testResultsWriter.getList();

        return testResults;
    }

    private ArrayList<TestResult> runAllTestsForProject(String testProjectName) {
        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager, logStream);
        ArrayList<ProgressEvent> events = projectBuilder.runAlltestsForProject(testProjectName);
        return extractResults(testProjectName, events);
    }

    public void syncDependencies() {

        System.out.println(Config.ANSI_PURPLE + "Syncing Dependencies " + Config.ANSI_RESET);

        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager, logStream);
        projectBuilder.syncDependencies();
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory, OutputStream logStream) {
        return new GradleWorker(ProjectManager.mountGradleProject(gradleVersion, directory), logStream);
    }

    private static ArrayList<TestResult> extractResults(String testProjectName,
            ArrayList<ProgressEvent> events) {
        ArrayList<TestResult> testResults = new ArrayList<>();
        for (ProgressEvent event : events) {
            if (event instanceof DefaultTestFinishEvent) {
                TestResult testResult = GradleWorker.extractResult(event, testProjectName);
                if (testResult != null) {
                    System.out.println("Test run complete: " + testResult.toCSVString());
                    testResults.add(testResult);
                }
            }
        }
        return testResults;
    }

    public static TestResult extractResult(ProgressEvent event, String testProjectName) {
        DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event.getDescriptor();

        TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
        String resultString;

        String testClassName = descriptor.getClassName();
        String testMethodName = descriptor.getMethodName();

        if (testClassName == null || testMethodName == null)
            return null;

        if (result instanceof DefaultTestFailureResult)
            resultString = "FAILED";
        else if (result instanceof DefaultTestSkippedResult)
            resultString = "SKIPPED";
        else
            resultString = "PASSED";

        testMethodName = testMethodName.replace("(", "").replace(")", "");

        return new TestResult(testClassName, testMethodName, testProjectName, resultString);
    }
}
