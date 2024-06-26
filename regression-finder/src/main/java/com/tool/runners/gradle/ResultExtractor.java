package com.tool.runners.gradle;

import org.gradle.tooling.Failure;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestFailureResult;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;
import org.gradle.tooling.events.test.internal.DefaultTestSkippedResult;

import com.items.TestResult;

public class ResultExtractor {

    public static TestResult extractResult(ProgressEvent event, String testProjectName, String logs) {
        DefaultJvmTestOperationDescriptor descriptor = (DefaultJvmTestOperationDescriptor) event.getDescriptor();

        TestOperationResult result = ((DefaultTestFinishEvent) event).getResult();
        String resultString;

        String testClassName = descriptor.getClassName();
        String testMethodName = descriptor.getMethodName();

        if (testClassName == null || testMethodName == null)
            return null;

        testMethodName = testMethodName.replace("(", "").replace(")", "");

        if (result instanceof TestFailureResult) {
            resultString = "FAILED";

            String stackTrace = ResultExtractor.extractStackTrace(logs, testProjectName, testClassName, testMethodName);

            TestFailureResult failureResult = (TestFailureResult) result;
            StringBuilder failCause = new StringBuilder(stackTrace);

            for (Failure failure : failureResult.getFailures())
                failCause.append("\n" + failure.getMessage());

            return new TestResult(testClassName, testMethodName, testProjectName, resultString, failCause.toString());
        } else if (result instanceof DefaultTestSkippedResult)
            resultString = "SKIPPED";
        else
            resultString = "PASSED";

        return new TestResult(testClassName, testMethodName, testProjectName, resultString);
    }

    static String extractStackTrace(String logs, String testProjectName, String testClassName,
            String testMethodName) {

        StringBuilder stackTraceBuilder = new StringBuilder();

        String taskLine = "Task " + testProjectName + ":test";
        int taskLineIndex = logs.indexOf(taskLine);

        if (taskLineIndex == -1)
            return stackTraceBuilder.toString();

        int taskLineEndIndex = logs.indexOf('\n', taskLineIndex);
        
        if (taskLineEndIndex == -1)
            return stackTraceBuilder.toString();

        stackTraceBuilder.append(logs, taskLineIndex, taskLineEndIndex + 1);

        String simpleClassName = testClassName.substring(testClassName.lastIndexOf('.') + 1);
        String searchPattern = simpleClassName + " > " + testMethodName;

        int methodLineIndex = logs.indexOf(searchPattern, taskLineEndIndex);

        if (methodLineIndex == -1)
            return stackTraceBuilder.toString();

        int lastLineIndex = logs.indexOf(" at " + simpleClassName + ".java:", methodLineIndex);

        if (lastLineIndex == -1)
            return stackTraceBuilder.toString();

        int endIndex = logs.indexOf('\n', lastLineIndex);

        if (endIndex == -1)
            return stackTraceBuilder.toString();

        stackTraceBuilder.append(logs, methodLineIndex, endIndex);

        return stackTraceBuilder.toString();
    }

}
