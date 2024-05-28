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
    private GitWorker gitWorker;

    private TargetProject(String path, ProjectConnection connection, GitWorker gitWorker) {
        this.connection = connection;
        this.gitWorker = gitWorker;
    }

    public static TargetProject mountLocalProject(String path, String gradleVersion) throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(path);

        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitFetcher = new GitWorker(repository);
        return new TargetProject(path, connector.connect(), gitFetcher);
    }

    public ProjectConnection getConnection() {
        return this.connection;
    }

    public GitWorker getGitWorker(){
        return this.gitWorker;
    }

}
