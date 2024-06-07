package com.tool;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.GitCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.git.GitWorker;
import com.tool.writers.CSVWriter;

public class Main {
    public static String repositoryPath = "../test-area/repository";
    public static String resultsPath = "../results/";

    public static void main(String[] args) {
        String repositoryLink = "https://github.com/sohamviradiya-work/small-test-repo/";
        clean(repositoryPath);
        clean(resultsPath);

        try {
            GitWorker.getRemoteRepository(repositoryPath, repositoryLink);
            run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void run() throws IOException, NoHeadException, GitAPIException {

        TargetProject targetProject = TargetProject.mountLocalProject(repositoryPath, "7.6.4");

        writeTestResults(targetProject);

        writecommits(targetProject);

        CSVWriter<RegressionBlame> blameCSVWriter = CSVWriter.create(resultsPath + "blame-tests.csv");

        blameCSVWriter.write(new RegressionBlame(new TestIdentifier(":testProject","testClass", "testMethod"),
                new GitCommit("author", "commit", "parent", "branch", Date.from(Instant.now()), "message")));

        targetProject.runFailedTestsBranchWise(blameCSVWriter);
        blameCSVWriter.close();
        targetProject.close();
    }

    private static void writecommits(TargetProject targetProject) throws IOException, NoHeadException, GitAPIException {
        CSVWriter<GitCommit> commitsCSVWriter = CSVWriter.create(resultsPath + "commits-list.csv");
        commitsCSVWriter.write(new GitCommit("author", "commit", "parent", "branch", Date.from(Instant.now()), "message"));
        HashMap<String, ArrayList<GitCommit>> branchCommitMap = targetProject.getGitWorker().listCommitsByBranch();
        for (Entry<String, ArrayList<GitCommit>> entry : branchCommitMap.entrySet()) {
            for (GitCommit gitCommit : entry.getValue()) {
                commitsCSVWriter.write(gitCommit);
            }
        }
        commitsCSVWriter.close();
    }

    private static void writeTestResults(TargetProject targetProject) throws IOException {
        ArrayList<TestResult> testResults = targetProject.getRunner().runAllTests();
        CSVWriter<TestResult> testResultsCSVWriter = CSVWriter.create(resultsPath + "failed-tests.csv");

        for(TestResult testResult:testResults){
            testResultsCSVWriter.write(testResult);
        }
        testResultsCSVWriter.close();
    }

    @SuppressWarnings("unused")
    private static String getRepositoryLink() {
        System.out.println("Enter Repository Link");
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = scanner.nextLine();
        scanner.close();
        return repositoryLink;
    }

    public static void clean(String path) {
        cleanDirectory(new File(path));
    }

    private static void cleanDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
                file.delete();
            }
        }
    }

}
