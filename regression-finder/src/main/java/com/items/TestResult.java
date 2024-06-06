package com.items;

import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.test.TestOperationResult;
import org.gradle.tooling.events.test.internal.DefaultJvmTestOperationDescriptor;
import org.gradle.tooling.events.test.internal.DefaultTestFailureResult;
import org.gradle.tooling.events.test.internal.DefaultTestFinishEvent;
import org.gradle.tooling.events.test.internal.DefaultTestSkippedResult;

import com.items.interfaces.CSVItem;
import com.tool.writers.interfaces.ItemWriter;

public class TestResult implements CSVItem{
    public enum Result {
        PASSED,
        FAILED,
        SKIPPED
    }

    private Result result;
    private TestIdentifier testIdentifier;

    public TestResult(String testClass, String testMethod,String testProject ,String result) {
        this.testIdentifier = new TestIdentifier(testClass,testMethod,testProject);
        this.result = parseResult(result);
    }

    private static Result parseResult(String result) {
        if (result.equalsIgnoreCase("PASSED")) {
            return Result.PASSED;
        }else if (result.equalsIgnoreCase("SKIPPED")) {
            return Result.SKIPPED;
        } else if (result.equalsIgnoreCase("FAILED")) {
            return Result.FAILED;
        } else {
            throw new IllegalArgumentException("Invalid test result: " + result);
        }
    }

    public String toCSVString() {
        return testIdentifier.getTestProject() + "," + testIdentifier.getTestClass() + "," + testIdentifier.getTestMethod() + "," + result.name();
    }

    public Result getResult(){
        return result;
    }

    public TestIdentifier getIdentifier(){
        return testIdentifier;
    }

    public static TestResult extractResult(ProgressEvent event, String testProjectName, ItemWriter<TestResult> resultsWriter) {
        if (event instanceof DefaultTestFinishEvent) {
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
        return null;
    }
}
