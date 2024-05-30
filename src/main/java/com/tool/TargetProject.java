package com.tool;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import org.gradle.tooling.GradleConnector;

import com.tool.items.GitCommit;
import com.tool.items.RegressionBlame;
import com.tool.items.TestIndentifier;
import com.tool.items.TestResult;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.interfaces.ItemWriter;

public class TargetProject {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private GradleWorker gradleWorker;
    private GitWorker gitWorker;

    private TargetProject(String path, GradleWorker gradleWorker, GitWorker gitWorker) {
        this.gradleWorker = gradleWorker;
        this.gitWorker = gitWorker;
    }

    public GradleWorker getRunner() {
        return this.gradleWorker;
    }

    public GitWorker getGitWorker() {
        return this.gitWorker;
    }

    public void runFailedTestsBranchWise(ItemWriter<RegressionBlame> regressionBlameWriter)
            throws IOException, NoHeadException, GitAPIException {

        HashMap<String, ArrayList<GitCommit>> branchCommitMap = gitWorker.listCommitsByBranch();
        
        for (String branch : branchCommitMap.keySet()) {
            ArrayList<GitCommit> branchCommits = branchCommitMap.get(branch);
            runFailedTestsForCommits(branchCommits,regressionBlameWriter);
        }
    }

    private void runFailedTestsForCommits(ArrayList<GitCommit> branchCommits,ItemWriter<RegressionBlame> regressionBlameWriter)
            throws IOException, IllegalArgumentException, GitAPIException {

        gitWorker.checkoutToCommit(branchCommits.get(0).getCommitId());

        ArrayList<TestIndentifier> failingTests = gradleWorker.getFailingTests();

        GitCommit commitAfter = GitCommit.createNullCommit();

        for (GitCommit gitCommit : branchCommits) {

            gitWorker.checkoutToCommit(gitCommit.getCommitId());

            ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<>();
            gradleWorker.runTests(failingTests, testResultsWriter);

            final ArrayList<TestIndentifier> nextBatchTests = new ArrayList<>();

            for (TestResult testResult : testResultsWriter.getList()) {
                TestIndentifier testIdentifier = testResult.getIdentifier();
                if (testResult.getResult() != TestResult.Result.FAILED) {
                    RegressionBlame regressionBlame = new RegressionBlame(testIdentifier, commitAfter);
                    regressionBlameWriter.write(regressionBlame);
                }
                else    
                    nextBatchTests.add(testIdentifier);
            
            }

            failingTests = nextBatchTests;

            if (failingTests.isEmpty())
                break;

            commitAfter = gitCommit;
        }

        gitWorker.checkoutToCommit(branchCommits.get(0).getCommitId());
        gitWorker.close();
    }

    public static TargetProject mountLocalProject(String path, String gradleVersion) throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(path);

        GradleConnector connector = GradleConnector.newConnector().useGradleVersion(gradleVersion);
        connector.forProjectDirectory(directory);

        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitWorker = new GitWorker(repository);
        GradleWorker gradleWorker = new GradleWorker(connector.connect());
        return new TargetProject(path, gradleWorker, gitWorker);
    }
}
