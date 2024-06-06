package com.tool.runner;

import java.util.ArrayList;

import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.events.OperationType;
import org.gradle.tooling.events.ProgressEvent;
import org.gradle.tooling.events.ProgressListener;

public class ProjectBuilder {
    private BuildLauncher buildLauncher;

    private ProjectBuilder(BuildLauncher buildLauncher) {
        this.buildLauncher = buildLauncher;
    }

    public static ProjectBuilder mountProjectBuilder(ProjectManager projectManager) {
        BuildLauncher buildLauncher = projectManager.getConnection().newBuild();
        return new ProjectBuilder(buildLauncher);
    }

    public void syncDependencies() {
        buildLauncher.forTasks("dependencies");
        buildLauncher.withArguments("--refresh-dependencies");

        try {
            System.out.println("Syncing Dependencies");
            buildLauncher.run();
        } catch (Exception e) {
            e.printStackTrace();
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
            buildLauncher.run();
        } catch (Exception e) {

        }
        return events;
    }
}
