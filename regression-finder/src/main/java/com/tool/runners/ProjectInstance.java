package com.tool.runners;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.runners.git.GitWorker;
import com.tool.runners.gradle.GradleWorker;
import com.tool.writers.interfaces.ItemWriter;

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

    public void close() throws IOException, GitAPIException {
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

    public ArrayList<TestIdentifier> extractTestsToRun(ProjectCommit firstCommit, ProjectCommit lastCommit,ItemWriter<RegressionBlame> blameWriter) throws GitAPIException, IOException {
        ArrayList<TestResult> testResults = runAllTestsForCommit(lastCommit);
    
        ArrayList<TestIdentifier> failingTests = new ArrayList<>(TestResult.extractFailingTests(testResults));
    
        ArrayList<TestResult> lastPhaseTestResults = runTestsForCommit(failingTests,firstCommit,lastCommit);
    
        HashSet<TestIdentifier> falseWrittenTests = TestResult.extractFailingTests(lastPhaseTestResults);
    
        gitWorker.checkoutToCommit(lastCommit);
       
        for (TestIdentifier testIdentifier : falseWrittenTests) {
            failingTests.remove(testIdentifier);
            blameWriter.writeAll(blameTestOnAuthor(testIdentifier));
        }

        return failingTests;
    }


    private ArrayList<RegressionBlame> blameTestOnAuthor(TestIdentifier testIdentifier) throws GitAPIException {

        String testFilePath = testIdentifier.getTestProject() + "/" + testSrcPath + "/" + testIdentifier.getTestClass().replace(".", "/");

        testFilePath = testFilePath.replaceAll("\\.", "")  
        .replaceAll(":", "/")   
        .replaceAll("/{2,}", "/").substring(1) + ".java";
        
        ArrayList<ProjectCommit> authorCommits = gitWorker.blameTest(testFilePath, testIdentifier.getTestMethod());

        ArrayList<RegressionBlame> regressionBlames = new ArrayList<>(); 
        for(ProjectCommit authorCommit:authorCommits){
            regressionBlames.add(new RegressionBlame(testIdentifier,authorCommit, false));
        }
        return regressionBlames;
    }

    public static ProjectInstance mountLocalProject(String rootPath, String testSrcPath) throws IOException {
        return mountLocalProject(rootPath, testSrcPath, "");
    }

    public static ProjectInstance mountLocalProject(String rootPath, String testSrcPath, String gradleVersion)
            throws IOException {

        if (gradleVersion.length() == 0)
            gradleVersion = DEFAULT_GRADLE_VERSION;

        File directory = new File(rootPath);

        GradleWorker gradleWorker = GradleWorker.mountGradleWorker(gradleVersion, directory);
        GitWorker gitWorker = GitWorker.mountGitWorker(directory);

        return new ProjectInstance(gradleWorker, gitWorker, testSrcPath);
    }
}
