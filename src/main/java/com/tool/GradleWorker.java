package com.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;
import org.gradle.tooling.events.test.internal.DefaultTestSkippedResult;

import com.tool.items.TestIndentifier;
import com.tool.items.TestResult;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.interfaces.ItemWriter;

import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;

public class GradleWorker {
    private ProjectConnection projectConnection;

    public GradleWorker(ProjectConnection projectConnection) {
        this.projectConnection = projectConnection;
    }

    public void close(){
        this.projectConnection.close();
    }

    public TestResult runSingleTest(TestIndentifier testIndentifier) {
        ArrayListWriter<TestResult> arrayListWriter = new ArrayListWriter<>();
        runTests(List.of(testIndentifier), arrayListWriter);
        return arrayListWriter.getList().get(0);
    }

    public void runTests(List<TestIndentifier> testIndentifiers, ItemWriter<TestResult> resultsWriter) {

        TestLauncher testLauncher = projectConnection.newTestLauncher();
        testLauncher.setColorOutput(true);
        
        for(TestIndentifier testIndentifier:testIndentifiers){
            testLauncher.withTaskAndTestMethods("test", testIndentifier.getTestClass(), List.of(testIndentifier.getTestMethod()));
        }
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                testLogger(event, resultsWriter);
            }
        }, OperationType.TEST);
        try {
            testLauncher.run();
        } catch (Exception e) {
            
        }
    }

    public void runAlltests(ItemWriter<TestResult> resultsWriter) {
        BuildLauncher buildLauncher = projectConnection.newBuild();

        buildLauncher.forTasks("test");
        buildLauncher.withArguments("--continue", "--quiet");
        buildLauncher.addProgressListener(new ProgressListener() {

            @Override
            public void statusChanged(ProgressEvent event) {
                testLogger(event, resultsWriter);
            }
        }, OperationType.TEST);

        try {
            buildLauncher.run();
        } catch (Exception e) {
            
        }
    }

    private void testLogger(ProgressEvent event, ItemWriter<TestResult> resultsWriter) {
        if (event instanceof DefaultTestFinishEvent) {
            DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event.getDescriptor();
            
            TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
            String resultString;

            if (descriptor.getClassName() == null || descriptor.getMethodName() == null)
                return;

            if (result instanceof DefaultTestFailureResult)
                resultString = "FAILED";
            else if(result instanceof DefaultTestSkippedResult)
                resultString = "SKIPPED";
            else
                resultString = "PASSED";
                
            TestResult testResult = new TestResult(descriptor.getClassName(), descriptor.getMethodName(), resultString);
            if (testResult != null) {
                try {
                    resultsWriter.write(testResult);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public ArrayList<TestIndentifier> getFailingTests() {
        ArrayList<TestIndentifier> failingTests = new ArrayList<TestIndentifier>();
    
        ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<TestResult>();
    
        runAlltests(testResultsWriter);
        
        ArrayList<TestResult> testResults = testResultsWriter.getList();
    
        for (TestResult testResult : testResults) {
            if(testResult.getResult()==TestResult.Result.FAILED){
                failingTests.add(testResult.getIdentifier());
            }
        }
        return failingTests;
    }

    static ArrayList<TestIndentifier> evaulateTestResults(ArrayList<TestResult> testResults) throws IOException {
        ArrayList<TestIndentifier> passingTestIndentifiers = new ArrayList<>();
    
        for (TestResult testResult : testResults) {
            TestIndentifier testIdentifier = testResult.getIdentifier();
            if (testResult.getResult() != TestResult.Result.FAILED)
                passingTestIndentifiers.add(testIdentifier);
        }
        return passingTestIndentifiers;
    }

    public static GradleWorker mountGradleWorker(String gradleVersion, File directory) {
        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);
        GradleWorker gradleWorker = new GradleWorker(connector.connect());
        return gradleWorker;
    }
}
