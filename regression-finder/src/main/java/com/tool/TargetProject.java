package com.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.tooling.GradleConnector;

import com.tool.templates.GitCommit;
import com.tool.templates.RegressionBlame;
import com.tool.templates.TestResult;
import com.tool.templates.TestResult.Result;
import com.tool.templates.TestResult.TestIndentifier;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.ItemWriter;

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
        GitWorker gitWorker = new GitWorker(repository);
        ProjectRunner projectRunner = new ProjectRunner(connector.connect());
        return new TargetProject(path, projectRunner, gitWorker);
    }

    public ProjectRunner getRunner() {
        return this.projectRunner;
    }

    public GitWorker getGitWorker(){
        return this.gitWorker;
    }

    void runFailedTests(ArrayList<GitCommit> gitCommits,ItemWriter<RegressionBlame> writer)
            throws IOException {
        ArrayList<TestResult.TestIndentifier> failingTests = getInitialResults();
    
        GitCommit lastCommit = GitCommit.createNullCommit();
    
        for (GitCommit gitCommit : gitCommits) {
            gitWorker.checkoutToCommit(gitCommit.getCommitId());
            ArrayList<TestResult.TestIndentifier> toBeRemoved = new ArrayList<>();
            for(TestIndentifier testIndentifier:failingTests){
               TestResult testResult = projectRunner.runSingleTest(testIndentifier);
                if(testResult.getResult()==TestResult.Result.PASSED){
                    writer.write(new RegressionBlame(testIndentifier,gitCommit));
                    toBeRemoved.add(testIndentifier);
                }
            }
    
            for(TestIndentifier testIndentifier:toBeRemoved){
                failingTests.remove(testIndentifier);
            }
            if(failingTests.isEmpty()) break;
    
            lastCommit = gitCommit;
        }
    }

    private ArrayList<TestResult.TestIndentifier> getInitialResults() {
        ArrayList<TestResult.TestIndentifier> failingTests = new ArrayList<TestResult.TestIndentifier>();
    
        ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<TestResult>();
    
        projectRunner.runAlltests(testResultsWriter);
        ArrayList<TestResult> testResults = testResultsWriter.getList();
    
        for (TestResult testResult : testResults) {
            if(testResult.getResult()==TestResult.Result.FAILED){
                failingTests.add(testResult.getUniqueIdentifier());
            }
        }
        return failingTests;
    }

}
