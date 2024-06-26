package com.tool.runners.gradle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;

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
            HashMap<String, List<String>> testMethods) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ProjectTester projectTester = ProjectTester.mountProjectTester(projectManager, outputStream);
        ArrayList<ProgressEvent> events = projectTester.runTestsForProject(testProjectName, testMethods);

        String logs = outputStream.toString();

        logStream.write(logs.getBytes());

        return extractResults(testProjectName, events, logs);
    }

    public void syncDependencies() {

        System.out.println(Config.ANSI_PURPLE + "Syncing Dependencies " + Config.ANSI_RESET);

        ProjectBuilder projectBuilder = ProjectBuilder.mountProjectBuilder(projectManager, logStream);
        projectBuilder.syncDependencies();
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory, OutputStream logStream) {
        return new GradleWorker(ProjectManager.mountGradleProject(gradleVersion, directory), logStream);
    }

    private ArrayList<TestResult> extractResults(String testProjectName,
            ArrayList<ProgressEvent> events, String logs) throws IOException {
        ArrayList<TestResult> testResults = new ArrayList<>();

        for (ProgressEvent event : events) {

            logEvent(event);

            if (event instanceof DefaultTestFinishEvent) {
                TestResult testResult = ResultExtractor.extractResult(event, testProjectName, logs);
                if (testResult != null) {
                    testResults.add(testResult);
                }
            }
        }
        return testResults;
    }

    private void logEvent(ProgressEvent event) throws IOException {
        String eventInfoString = event.getDisplayName() + " at " + Date.from(Instant.ofEpochMilli(event.getEventTime()))
                + "\n";
        logStream.write(eventInfoString.getBytes());
    }
}
