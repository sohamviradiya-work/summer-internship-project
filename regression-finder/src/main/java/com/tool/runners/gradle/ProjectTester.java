package com.tool.runners.gradle;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;
import com.tool.Config;

public class ProjectTester {
    private TestLauncher testLauncher;
    private OutputStream logStream;


    public class CompilationException extends Exception {

        public CompilationException(String string) {
            super(string);
        }
        
    }

    private ProjectTester(TestLauncher testLauncher, OutputStream logStream) {
        this.testLauncher = testLauncher;
        this.logStream = logStream;
    }

    public static ProjectTester mountProjectTester(ProjectManager projectManager, OutputStream logStream) {
        TestLauncher testLauncher = projectManager.getConnection().newTestLauncher();
        return new ProjectTester(testLauncher, logStream);
    }

    public ArrayList<ProgressEvent> runTestsForProject(String testProjectName,
            HashMap<String, List<String>> testMethods) throws CompilationException {
        for (String testClass : testMethods.keySet()) {
            testLauncher.withTaskAndTestMethods(testProjectName + ":test", testClass, testMethods.get(testClass));
        }
        testLauncher.addArguments("--parallel", "--console=verbose", "--stacktrace");

        ArrayList<ProgressEvent> events = new ArrayList<>();
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                events.add(event);
            }
        }, OperationType.TEST);

        try {
            System.out.println(Config.ANSI_PURPLE + "Running " + testProjectName + Config.ANSI_RESET);
            testLauncher.setStandardOutput(logStream);
            testLauncher.run();
        } catch (Exception e) {
            if(events.isEmpty()) // no test fail events, but run still failed
                throw new CompilationException("Compilation Failed");
        }
        return events;
    }

}
