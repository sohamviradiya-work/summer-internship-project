package com.tool.runners.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ModelBuilder;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

public class ProjectManager {

    private ProjectConnection projectConnection;

    private ProjectManager(ProjectConnection projectConnection) {
        this.projectConnection = projectConnection;
    }

    public static ProjectManager mountGradleProject(String gradleVersion, File directory) {
        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);
        ProjectConnection projectConnection = connector.connect();
        return new ProjectManager(projectConnection);
    }

    public ProjectConnection getConnection() {
        return this.projectConnection;
    }

    public void close() {
        this.projectConnection.close();
    }

    public List<String> getSubProjects() {
        ModelBuilder<GradleProject> modelBuilder = getConnection().model(GradleProject.class);
        GradleProject rootProject = modelBuilder.get();
        
        List<String> subProjects = getSubProjects(rootProject);

        if (subProjects.isEmpty())
            subProjects.add("");

        return subProjects;
    }

    private List<String> getSubProjects(GradleProject rootProject) {
        List<String> subProjects = new ArrayList<>();
        for (GradleProject subProject : rootProject.getChildren()) {
            subProjects.add(subProject.getPath());
            subProjects.addAll(getSubProjects(subProject));
        }
        return subProjects;
    }
}
