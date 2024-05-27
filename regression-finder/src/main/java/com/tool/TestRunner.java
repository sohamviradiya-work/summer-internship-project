package com.tool;

import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;

import com.tool.templates.TestResult;

import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;

public class TestRunner {
    private ProjectConnection projectConnection;

    TestRunner(ProjectConnection projectConnection) {
        this.projectConnection = projectConnection;
    }

    ArrayList<TestResult> runClassTests(String testClassName, List<String> testMethodNames) {

        TestLauncher testLauncher = projectConnection.newTestLauncher();
        testLauncher.setColorOutput(true);
        testLauncher.withTaskAndTestMethods("test", testClassName, testMethodNames);
        ArrayList<TestResult> testResults = new ArrayList<TestResult>(testMethodNames.size());


        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                if (event instanceof DefaultTestFinishEvent) {
                    DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event
                            .getDescriptor();
                    TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
                    String resultString;

                    if (descriptor.getClassName() == null || descriptor.getMethodName() == null)
                        return;

                    if (result instanceof DefaultTestFailureResult)
                        resultString = "FAILED";
                    else
                        resultString = "PASSED";
                    TestResult testResult = new TestResult(descriptor.getClassName(), descriptor.getMethodName(), resultString) ;
                    testResults.add(testResult);
                }
            }
        },OperationType.TEST);
        return testResults;
    }

    ArrayList<TestResult> runAlltests() {
        TestLauncher testLauncher = projectConnection.newTestLauncher();
        ArrayList<TestResult> testResults = new ArrayList<TestResult>();


        return testResults;
    }
}
