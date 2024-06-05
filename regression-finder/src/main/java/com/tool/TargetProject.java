package com.tool;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.tool.git.GitCommit;
import com.tool.git.GitWorker;
import com.tool.git.RegressionBlame;
import com.tool.runner.GradleWorker;
import com.tool.runner.TestIdentifier;
import com.tool.runner.TestResult;
import com.tool.writers.ArrayListWriter;
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

        ArrayList<TestIdentifier> failingTests = gradleWorker.getFailingTests();

        GitCommit commitAfter = headCommit;

        branchCommits.remove(0);
        for (GitCommit gitCommit : branchCommits) {

            if (failingTests.isEmpty())
                break;

            if (isSyncRequired(gitCommit.getCommitId(), commitAfter.getCommitId()))
                gradleWorker.syncDependencies();

            gitWorker.checkoutToCommit(gitCommit.getCommitId());

            ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<>();
            gradleWorker.runTests(failingTests, testResultsWriter);

            failingTests = evaulateResults(regressionBlameWriter, commitAfter, testResultsWriter);

            commitAfter = gitCommit;
        }
        
        for(TestIdentifier testIdentifier:failingTests){
            RegressionBlame regressionBlame = new RegressionBlame(testIdentifier, commitAfter);
            regressionBlameWriter.write(regressionBlame);
        }

        gitWorker.checkoutToCommit(headCommit.getCommitId());
    }

    private ArrayList<TestIdentifier> evaulateResults(ItemWriter<RegressionBlame> regressionBlameWriter,
            GitCommit commitAfter, ArrayListWriter<TestResult> testResultsWriter) throws IOException {
        final ArrayList<TestIdentifier> nextBatchTests = new ArrayList<>();

        for (TestResult testResult : testResultsWriter.getList()) {
            TestIdentifier testIdentifier = testResult.getIdentifier();
            if (testResult.getResult() != TestResult.Result.FAILED) {
                RegressionBlame regressionBlame = new RegressionBlame(testIdentifier, commitAfter);
                regressionBlameWriter.write(regressionBlame);
            } else
                nextBatchTests.add(testIdentifier);
        }
        return nextBatchTests;
    }

    private boolean isSyncRequired(String commitId, String commitId2) {
        ArrayList<String> changedFilePaths = gitWorker.getChangedFiles(commitId, commitId2);
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
