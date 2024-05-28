package com.tool;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.tooling.GradleConnector;

public class TargetProject {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private ProjectRunner projectRunner;
    private GitWorker gitWorker;

    private TargetProject(String path, ProjectRunner projectRunner, GitWorker gitWorker) {
        this.projectRunner = projectRunner;
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
        ProjectRunner projectRunner = new ProjectRunner(connector.connect());
        return new TargetProject(path, projectRunner, gitFetcher);
    }

    public ProjectRunner getRunner() {
        return this.projectRunner;
    }

    public GitWorker getGitWorker(){
        return this.gitWorker;
    }

}
