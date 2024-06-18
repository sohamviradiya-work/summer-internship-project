package com.tool.runners.gradle;

import java.io.File;
import java.io.IOException;
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
import com.tool.writers.ArrayListWriter;

public class GradleWorker {
    ProjectManager projectManager;

    public GradleWorker(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public void close() {
        this.projectManager.close();
    }

    public ArrayList<TestResult> runTests(List<TestIdentifier> testIdentifiers) throws IOException {
        HashMap<String, HashMap<String, List<String>>> testGroups = TestIdentifier.groupByProjectClass(testIdentifiers);
        ArrayListWriter<TestResult> testResultsWriter = ArrayListWriter.create();
        for (String testProjectName : testGroups.keySet()) {
            System.out.println("Running tests for project: " + testProjectName);
            testResultsWriter.writeAll(runTestsForProject(testProjectName, testGroups.get(testProjectName)));
        }
        System.out.println("Test run complete");

        return testResultsWriter.getList();
    }

    private ArrayList<TestResult> runTestsForProject(String testProjectName,
            HashMap<String, List<String>> testMethods) {
        ProjectTester projectTester = ProjectTester.mountProjectTester(projectManager);
        ArrayList<ProgressEvent> events = projectTester.runTestsForProject(testProjectName, testMethods);
        return extractResults(testProjectName, events);
    }


    public ArrayList<TestResult> runAllTests() throws IOException {

        List<String> subProjects = projectManager.getSubProjects();
        ArrayListWriter<TestResult> testResultsWriter = ArrayListWriter.create();
        for (String testProjectName : subProjects) {
            System.out.println("Running tests for project: " + testProjectName);
            testResultsWriter.writeAll(runAlltestsForProject(testProjectName));
        }
        System.out.println("Test run complete");

        ArrayList<TestResult> testResults = testResultsWriter.getList();

        return testResults;
    }

    private ArrayList<TestResult> runAlltestsForProject(String testProjectName) {
        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager);
        ArrayList<ProgressEvent> events = projectBuilder.runAlltestsForProject(testProjectName);
        return extractResults(testProjectName, events);
    }

    public void syncDependencies() {
        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager);
        projectBuilder.syncDependencies();
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory) {
        return new GradleWorker(ProjectManager.mountGradleProject(gradleVersion, directory));
    }


    private static ArrayList<TestResult> extractResults(String testProjectName,
            ArrayList<ProgressEvent> events) {
        ArrayList<TestResult> testResults = new ArrayList<>();
        for (ProgressEvent event : events) {
            if (event instanceof DefaultTestFinishEvent) {
                TestResult testResult = GradleWorker.extractResult(event, testProjectName);
                if (testResult != null)
                    testResults.add(testResult);
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
