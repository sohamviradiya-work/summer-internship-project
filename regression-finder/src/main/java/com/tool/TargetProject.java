package com.tool;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.GitCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.git.GitWorker;
import com.tool.runner.GradleWorker;
import com.tool.writers.interfaces.ItemWriter;

public class TargetProject {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private GradleWorker gradleWorker;
    private GitWorker gitWorker;

    public TargetProject(GradleWorker gradleWorker, GitWorker gitWorker) {
        this.gradleWorker = gradleWorker;
        this.gitWorker = gitWorker;
    }

    public GradleWorker getRunner() {
        return this.gradleWorker;
    }

    public GitWorker getGitWorker() {
        return this.gitWorker;
    }

    public void close() {
        this.gitWorker.close();
        this.gradleWorker.close();
    }

    public void runFailedTestsBranchWise(ItemWriter<RegressionBlame> regressionBlameWriter)
            throws IOException, NoHeadException, GitAPIException {

        HashMap<String, ArrayList<GitCommit>> branchCommitMap = gitWorker.listCommitsByBranch();

        for (String branch : branchCommitMap.keySet()) {
            System.out.println("Running for branch:" + branch);
            ArrayList<GitCommit> branchCommits = branchCommitMap.get(branch);
            runFailedTestsForCommits(branchCommits, regressionBlameWriter);
        }
    }

    private void runFailedTestsForCommits(ArrayList<GitCommit> branchCommits,
            ItemWriter<RegressionBlame> regressionBlameWriter)
            throws IOException, IllegalArgumentException, GitAPIException {

        GitCommit headCommit = branchCommits.get(0);
        gitWorker.checkoutToCommit(headCommit.getCommitId());

        ArrayList<TestResult> testResults = gradleWorker.runAllTests();
        ArrayList<TestIdentifier> failingTests = TestResult.extractFailingTests(testResults);

        GitCommit commitAfter = headCommit;

        branchCommits.remove(0);

        for (GitCommit gitCommit : branchCommits) {

            if (failingTests.isEmpty())
                break;

            if (isSyncRequired(gitCommit.getCommitId(), commitAfter.getCommitId()))
                gradleWorker.syncDependencies();

            gitWorker.checkoutToCommit(gitCommit.getCommitId());

            testResults = gradleWorker.runTests(failingTests);

            failingTests = evaluateResults(regressionBlameWriter, failingTests, commitAfter, testResults);

            commitAfter = gitCommit;
        }

        for (TestIdentifier testIdentifier : failingTests) {
            RegressionBlame regressionBlame = new RegressionBlame(testIdentifier, commitAfter);
            regressionBlameWriter.write(regressionBlame);
        }

        gitWorker.checkoutToCommit(headCommit.getCommitId());
    }

    private ArrayList<TestIdentifier> evaluateResults(ItemWriter<RegressionBlame> regressionBlameWriter, ArrayList<TestIdentifier> failingTests, GitCommit commitAfter, ArrayList<TestResult> testResults)
            throws IOException {
        HashSet<TestIdentifier> failingTestSet = new HashSet<>();

        for (TestResult testResult : testResults) {
            if (testResult.getResult() == TestResult.Result.FAILED)
                failingTestSet.add(testResult.getIdentifier());
        }

        ArrayList<TestIdentifier> newFailingTests = new ArrayList<>();

        for (TestIdentifier testIdentifier : failingTests) {
            if (failingTestSet.contains(testIdentifier))
                newFailingTests.add(testIdentifier);
            else
                regressionBlameWriter.write(new RegressionBlame(testIdentifier, commitAfter));
        }

        return newFailingTests;
    }

    private boolean isSyncRequired(String commitIdA, String commitIdB) {
        ArrayList<String> changedFilePaths = gitWorker.getChangedFiles(commitIdA, commitIdB);
        for (String path : changedFilePaths) {
            if (path.endsWith(".gradle"))
                return true;
        }
        return false;
    }

    public static TargetProject mountLocalProject(String path, String gradleVersion) throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(path);

        GradleWorker gradleWorker = GradleWorker.mountGradleWorker(gradleVersion, directory);
        GitWorker gitWorker = GitWorker.mountGitWorker(directory);
        return new TargetProject(gradleWorker, gitWorker);
    }
}
