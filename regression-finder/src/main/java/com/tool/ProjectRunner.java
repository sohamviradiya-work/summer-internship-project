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
import com.tool.writers.ArrayListWriter;
import com.tool.writers.ItemWriter;

import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;

public class ProjectRunner {
    private ProjectConnection projectConnection;

    ProjectRunner(ProjectConnection projectConnection) {
        this.projectConnection = projectConnection;
    }

    TestResult runSingleTest(String testClassName, String testMethodName) {
        ArrayListWriter<TestResult> arrayListWriter = new ArrayListWriter<>();
        runClassTests(testClassName, List.of(testMethodName), arrayListWriter);
        return arrayListWriter.getList().get(0);
    }

    void runClassTests(String testClassName, List<String> testMethodNames, ItemWriter<TestResult> resultsWriter) {

        TestLauncher testLauncher = projectConnection.newTestLauncher();
        testLauncher.setColorOutput(true);
        testLauncher.withTaskAndTestMethods("test", testClassName, testMethodNames);
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                testLogger(event, resultsWriter);
            }
        }, OperationType.TEST);
        try {
            testLauncher.run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void runAlltests(ItemWriter<TestResult> resultsWriter) {
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
            System.out.println(e.getMessage());
        }
    }

    private void testLogger(ProgressEvent event, ItemWriter<TestResult> resultsWriter) {
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
            TestResult testResult = new TestResult(descriptor.getClassName(), descriptor.getMethodName(), resultString);
            if (testResult != null) {
                try {
                    resultsWriter.write(testResult);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
