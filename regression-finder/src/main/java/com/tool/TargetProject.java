package com.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.tooling.GradleConnector;

import com.tool.templates.GitCommit;
import com.tool.templates.RegressionBlame;
import com.tool.templates.TestResult;
import com.tool.templates.TestResult.TestIndentifier;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.ItemWriter;

public class TargetProject {

    private static String DEFAULT_GRADLE_VERSION = "7.6.4";
    private GradleWorker gradleWorker;
    private GitWorker gitWorker;

    private TargetProject(String path, GradleWorker gradleWorker, GitWorker gitWorker) {
        this.gradleWorker = gradleWorker;
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
        GradleWorker gradleWorker = new GradleWorker(connector.connect());
        return new TargetProject(path, gradleWorker, gitWorker);
    }

    public GradleWorker getRunner() {
        return this.gradleWorker;
    }

    public GitWorker getGitWorker(){
        return this.gitWorker;
    }

    void runFailedTests(ArrayList<GitCommit> gitCommits,ItemWriter<RegressionBlame> regressionBlameWriter)
            throws IOException {
        ArrayList<TestResult.TestIndentifier> failingTests = gradleWorker.getFailingTests();
    
        GitCommit lastCommit = GitCommit.createNullCommit();
    
        for (GitCommit gitCommit : gitCommits) {
            gitWorker.checkoutToCommit(gitCommit.getCommitId());
            
            ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<>();

            gradleWorker.runTests(failingTests, testResultsWriter);            
            
            for(TestResult testResults: testResultsWriter.getList()){
                if(testResults.getResult()==TestResult.Result.PASSED){

                    RegressionBlame regressionBlame = new RegressionBlame(testResults.getIdentifier(), lastCommit);
                    regressionBlameWriter.write(regressionBlame);
                    failingTests.remove(testResults.getIdentifier());
                }
            }
            
            if(failingTests.isEmpty()) break;
    
            lastCommit = gitCommit;
        }
    }

}
