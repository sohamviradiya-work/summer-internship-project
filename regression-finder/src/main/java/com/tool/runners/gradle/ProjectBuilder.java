package com.tool.runners.gradle;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;

public class ProjectBuilder {
    private BuildLauncher buildLauncher;
    private OutputStream logStream;

    private ProjectBuilder(BuildLauncher buildLauncher, OutputStream logStream) {
        this.logStream = logStream;
        this.buildLauncher = buildLauncher;
    }

    public static ProjectBuilder mountProjectBuilder(ProjectManager projectManager, OutputStream logStream) {
        BuildLauncher buildLauncher = projectManager.getConnection().newBuild();
        return new ProjectBuilder(buildLauncher, logStream);
    }

    public void syncDependencies() {
        buildLauncher.forTasks("dependencies");
        buildLauncher.withArguments("--refresh-dependencies");

        try {
            buildLauncher.setStandardOutput(logStream);
            buildLauncher.run();
        } catch (Exception e) {

        }
    }

    public ArrayList<ProgressEvent> runAlltestsForProject(String testProjectName) {
        buildLauncher.forTasks(testProjectName + ":test");
        buildLauncher.withArguments("--continue", "--quiet", "--parallel");
        ArrayList<ProgressEvent> events = new ArrayList<>();

        buildLauncher.addProgressListener(new ProgressListener() {

            @Override
            public void statusChanged(ProgressEvent event) {
                events.add(event);
            }
        }, OperationType.TEST);

        try {
            buildLauncher.setStandardOutput(logStream);
            buildLauncher.run();
        } catch (Exception e) {

        }
        return events;
    }

    public List<ProgressEvent> runTestSuiteForSubProject(String subProjectName, String testSuiteName) {
        buildLauncher.forTasks(subProjectName + ":test");
        buildLauncher.withArguments("--tests", testSuiteName, "--continue", "--quiet", "--parallel");
        List<ProgressEvent> events = new ArrayList<>();

        buildLauncher.addProgressListener(new ProgressListener() {
            @Override
            public void statusChanged(ProgressEvent event) {
                events.add(event);
            }
        }, OperationType.TEST);

        try {
            buildLauncher.run();
        } catch (Exception e) {

        }
        return events;
    }
}
