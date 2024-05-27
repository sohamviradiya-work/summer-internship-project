package com.tool;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;

public class TargetProject {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private ProjectConnection connection;
    private Repository repository;

    private TargetProject(String path, ProjectConnection connection, Repository repository) {
        this.connection = connection;
        this.repository = repository;
    }

    public static TargetProject mountLocalProject(String path, String gradleVersion) throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(path);

        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.findGitDir(directory).build();

        return new TargetProject(path, connector.connect(), repository);
    }

    public ProjectConnection getConnection() {
        return this.connection;
    }

    public Repository getRepository() {
        return this.repository;
    }

}
