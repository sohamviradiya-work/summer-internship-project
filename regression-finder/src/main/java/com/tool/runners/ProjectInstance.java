package com.tool.runners;

import java.io.File;
import java.io.IOException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.runners.git.GitWorker;
import com.tool.runners.gradle.GradleWorker;

public class ProjectInstance {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private GradleWorker gradleWorker;
    private GitWorker gitWorker;
    private String testSrcPath;

    public ProjectInstance(GradleWorker gradleWorker, GitWorker gitWorker, String testSrcPath) {
        this.gradleWorker = gradleWorker;
        this.gitWorker = gitWorker;
        this.testSrcPath = testSrcPath;
    }

    public GitWorker getGitWorker() {
        return gitWorker;
    }

    public void close(String mainBranch) throws IOException, GitAPIException {
        this.gitWorker.close(mainBranch);
        this.gradleWorker.close();
    }

    public ArrayList<TestResult> runTestsForCommit(List<TestIdentifier> testIdentifiers,
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

    public boolean isRunRequired(ProjectCommit commitA, ProjectCommit commitB) {
        String commitIdA = commitA.getCommitId();
        String commitIdB = commitB.getCommitId();

        ArrayList<String> changedFilePaths = gitWorker.getChangedFiles(commitIdA, commitIdB);
        for (String path : changedFilePaths) {
            if (path.endsWith(".java") || path.endsWith(".gradle"))
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

    public ArrayList<RegressionBlame> blameTestOnAuthor(TestIdentifier testIdentifier,
            HashSet<String> projectCommitsSet)
            throws GitAPIException, IllegalArgumentException, IOException {

        String testFilePath = testIdentifier.getTestProject() + "/" + testSrcPath + "/"
                + testIdentifier.getTestClass().replace(".", "/");

        testFilePath = testFilePath.replaceAll("\\.", "")
                .replaceAll(":", "/")
                .replaceAll("/{2,}", "/").substring(1) + ".java";

        ArrayList<ProjectCommit> authorCommits = gitWorker.blameTest(testFilePath, testIdentifier.getTestMethod());

        ArrayList<RegressionBlame> regressionBlames = new ArrayList<>();
        for (ProjectCommit authorCommit : authorCommits) {
            if (projectCommitsSet.contains(authorCommit.getCommitId()))
                regressionBlames.add(RegressionBlame.constructBlame(testIdentifier, authorCommit, false));
        }
        if(regressionBlames.isEmpty())
            regressionBlames.add(RegressionBlame.constructBlame(testIdentifier, ProjectCommit.getLastPhaseCommit(), false));
        return regressionBlames;
    }

    public List<RegressionBlame> blameTestsOnAuthor(ArrayList<TestIdentifier> testIdentifiers,
            ArrayList<ProjectCommit> projectCommits)
            throws IllegalArgumentException, GitAPIException, IOException {

        gitWorker.checkoutToCommit(projectCommits.get(projectCommits.size() - 1));

        ArrayList<RegressionBlame> regressionBlames = new ArrayList<>();

        HashSet<String> projectCommitSet = new HashSet<>();

        projectCommits.forEach(projectCommit -> projectCommitSet.add(projectCommit.getCommitId()));

        projectCommitSet.remove(projectCommits.get(0).getCommitId());

        for (TestIdentifier testIdentifier : testIdentifiers) {
            regressionBlames.addAll(blameTestOnAuthor(testIdentifier, projectCommitSet));
        }
        return regressionBlames;
    }

    public static ProjectInstance mountLocalProject(String rootPath, String testSrcPath, String gradleVersion,
            OutputStream logStream)
            throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(rootPath);
        GradleWorker gradleWorker = GradleWorker.mountGradleWorker(gradleVersion, directory, logStream);
        GitWorker gitWorker = GitWorker.mountGitWorker(directory, logStream);

        return new ProjectInstance(gradleWorker, gitWorker, testSrcPath);
    }
}
