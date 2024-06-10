package com.tool;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.git.GitWorker;
import com.tool.runner.GradleWorker;

public class ProjectInstance {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private GradleWorker gradleWorker;
    private GitWorker gitWorker;

    public ProjectInstance(GradleWorker gradleWorker, GitWorker gitWorker) {
        this.gradleWorker = gradleWorker;
        this.gitWorker = gitWorker;
    }
    

    public GitWorker getGitWorker() {
        return gitWorker;
    }


    public void close() {
        this.gitWorker.close();
        this.gradleWorker.close();
    }

    public ArrayList<TestResult> runTestsForCommit(ArrayList<TestIdentifier> testIdentifiers,
            ProjectCommit projectCommit, ProjectCommit previousCommit) throws GitAPIException, IOException {
        gitWorker.checkoutToCommit(projectCommit);
        syncIfRequired(projectCommit, previousCommit);
        return gradleWorker.runTests(testIdentifiers);
    }

    public ArrayList<TestResult> runAllTestsForCommit(ProjectCommit projectCommit) throws GitAPIException, IOException {
        gitWorker.checkoutToCommit(projectCommit);
        return gradleWorker.runAllTests();
    }

    public void syncIfRequired(ProjectCommit commitA, ProjectCommit commitB) {
        if (isSyncRequired(commitA, commitB))
            gradleWorker.syncDependencies();
    }

    private boolean isRunRequired(ProjectCommit commitA, ProjectCommit commitB) {
        String commitIdA = commitA.getCommitId();
        String commitIdB = commitB.getCommitId();

        ArrayList<String> changedFilePaths = gitWorker.getChangedFiles(commitIdA, commitIdB);
        for (String path : changedFilePaths) {
            if (path.endsWith(".java"))
                return true;
        }
        return false;
    }

    private boolean isSyncRequired(ProjectCommit commitA, ProjectCommit commitB) {
        String commitIdA = commitA.getCommitId();
        String commitIdB = commitB.getCommitId();

        ArrayList<String> changedFilePaths = gitWorker.getChangedFiles(commitIdA, commitIdB);
        for (String path : changedFilePaths) {
            if (path.endsWith(".gradle"))
                return true;
        }
        return false;
    }

    public static ProjectInstance mountLocalProject(String path) throws IOException {
        return mountLocalProject(path, "");
    }

    public static ProjectInstance mountLocalProject(String path, String gradleVersion) throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(path);

        GradleWorker gradleWorker = GradleWorker.mountGradleWorker(gradleVersion, directory);
        GitWorker gitWorker = GitWorker.mountGitWorker(directory);
        return new ProjectInstance(gradleWorker, gitWorker);
    }
}
