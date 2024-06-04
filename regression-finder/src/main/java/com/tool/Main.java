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

import com.tool.git.GitCommit;
import com.tool.git.GitWorker;
import com.tool.git.RegressionBlame;
import com.tool.runner.TestIdentifier;
import com.tool.runner.TestResult;
import com.tool.writers.CSVWriter;

public class Main {
    public static String repositoryPath = "../test-area/repository";
    public static String resultsPath = "../results/";

    public static void main(String[] args) {
        String repositoryLink = "https://github.com/sohamviradiya-work/large-repo/";
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
        blameCSVWriter.write(new RegressionBlame(new TestIdentifier("testClass", "testMethod"),new GitCommit("author", "commit", "parent", "branch",Date.from(Instant.now()), "message")));

        targetProject.runFailedTestsBranchWise(blameCSVWriter);
        blameCSVWriter.close();
        targetProject.close();
    }

    private static void writecommits(TargetProject targetProject) throws IOException, NoHeadException, GitAPIException {
        CSVWriter<GitCommit> csvWriter = CSVWriter.create(resultsPath + "commits-list.csv");
        HashMap<String, ArrayList<GitCommit>> branchCommitMap = targetProject.getGitWorker().listCommitsByBranch();
        for(Entry<String, ArrayList<GitCommit>> entry:branchCommitMap.entrySet()){
            for(GitCommit gitCommit:entry.getValue()){
                csvWriter.write(gitCommit);
            }
        }
        csvWriter.close();
    }

    private static void writeTestResults(TargetProject targetProject) throws IOException {
        CSVWriter<TestResult> testResultCSVWriter = CSVWriter.create(resultsPath + "test-results.csv");
        targetProject.getRunner().runAlltests(testResultCSVWriter);
        testResultCSVWriter.close();
    }

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
