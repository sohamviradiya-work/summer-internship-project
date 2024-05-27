package com.tool;

import java.io.IOException;
import java.util.List;

import org.gradle.tooling.BuildLauncher;
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

    void runClassTests(String testClassName, List<String> testMethodNames,ResultsWriter resultsWriter) {

        TestLauncher testLauncher = projectConnection.newTestLauncher();
        testLauncher.setColorOutput(true);
        testLauncher.withTaskAndTestMethods("test", testClassName, testMethodNames);

        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                TestResult testResult = testLogger(event);
                if(testResult != null) {
                    try {
                        resultsWriter.writeTestResult(testResult);
                    } catch (IOException e) {
                        
                    }
                }
            }
        },OperationType.TEST);

        testLauncher.run();
    }

    void runAlltests(ResultsWriter resultsWriter) {
         BuildLauncher buildLauncher = projectConnection.newBuild();
            
         buildLauncher.forTasks("test");

         buildLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event){
                TestResult testResult = testLogger(event);
                if(testResult != null)
                    try {
                        resultsWriter.writeTestResult(testResult);
                    } catch (IOException e) {

                    }
            }
         },OperationType.TEST);

         buildLauncher.run();
    }

    private TestResult testLogger(ProgressEvent event) {
        if (event instanceof DefaultTestFinishEvent) {
            DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event
                    .getDescriptor();
            TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
            String resultString;

            if (descriptor.getClassName() == null || descriptor.getMethodName() == null)
                return null;

            if (result instanceof DefaultTestFailureResult)
                resultString = "FAILED";
            else
                resultString = "PASSED";
            TestResult testResult = new TestResult(descriptor.getClassName(), descriptor.getMethodName(), resultString) ;
           return testResult;
        }
        return null;
    }
}
