package com.tool.runner;

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

    public TestResult runSingleTest(TestIdentifier testIdentifier) {
        ArrayListWriter<TestResult> arrayListWriter = new ArrayListWriter<>();
        runTests(List.of(testIdentifier), arrayListWriter);
        return arrayListWriter.getList().get(0);
    }

    public void runTests(List<TestIdentifier> testIdentifiers, ItemWriter<TestResult> resultsWriter) {

        TestLauncher testLauncher = projectConnection.newTestLauncher();
        
        for(TestIdentifier testIdentifier:testIdentifiers){
            testLauncher.withTaskAndTestMethods("test", testIdentifier.getTestClass(), List.of(testIdentifier.getTestMethod()));
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
            
            String testClassName = descriptor.getClassName();
            String testMethodName = descriptor.getMethodName();

            if (testClassName == null || testMethodName == null)
                return;

            if (result instanceof DefaultTestFailureResult)
                resultString = "FAILED";
            else if(result instanceof DefaultTestSkippedResult)
                resultString = "SKIPPED";
            else
                resultString = "PASSED";
                
            testMethodName = testMethodName.replace("(", "").replace(")", "");

            TestResult testResult = new TestResult(testClassName, testMethodName, resultString);
            if (testResult != null) {
                try {
                    resultsWriter.write(testResult);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
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
            if(testResult.getResult()==TestResult.Result.FAILED){
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
}
