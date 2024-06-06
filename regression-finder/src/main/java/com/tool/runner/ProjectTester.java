package com.tool.runner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gradle.tooling.TestLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;


public class ProjectTester {
    private TestLauncher testLauncher;

    private ProjectTester(TestLauncher testLauncher) {
        this.testLauncher = testLauncher;
    }

    public static ProjectTester mountProjectTester(ProjectManager projectManager) {
        TestLauncher testLauncher = projectManager.getConnection().newTestLauncher();
        return new ProjectTester(testLauncher);
    }

    public ArrayList<ProgressEvent> runTestsForProject(String testProjectName, HashMap<String, List<String>> testMethods) {
        
        for (String testClass : testMethods.keySet()) {
            testLauncher.withTaskAndTestMethods(testProjectName + ":test", testClass, testMethods.get(testClass));
        }
        testLauncher.addArguments("--parallel");
        
        ArrayList<ProgressEvent> events = new ArrayList<>();
        testLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                events.add(event);
            }
        }, OperationType.TEST);
        try {
            testLauncher.run();
        } catch (Exception e) {

        }
        return events;
    }

}
