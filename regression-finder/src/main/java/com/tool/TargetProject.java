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
        ArrayList<TestResult.TestIndentifier> failingTests = projectRunner.getFailingTests();
    
        GitCommit lastCommit = GitCommit.createNullCommit();
    
        for (GitCommit gitCommit : gitCommits) {
            gitWorker.checkoutToCommit(gitCommit.getCommitId());
            ArrayList<TestResult.TestIndentifier> toBeRemoved = new ArrayList<>();
            for(TestIndentifier testIndentifier:failingTests){
               TestResult testResult = projectRunner.runSingleTest(testIndentifier);
                if(testResult.getResult()==TestResult.Result.PASSED){
                    writer.write(new RegressionBlame(testIndentifier,lastCommit));
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

}
