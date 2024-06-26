package com.tool.runners.gradle;

import java.io.OutputStream;

import org.gradle.tooling.BuildLauncher;

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
}
