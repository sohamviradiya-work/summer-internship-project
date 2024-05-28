package com.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Scanner;

import com.tool.templates.GitCommit;
import com.tool.templates.TestResult;
import com.tool.templates.TestResult.TestIndentifier;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.CSVWriter;

public class Main {
    public static String path = "./test-area/repository";

    public static void main(String[] args) {
        // String repositoryLink = getRepositoryLink();
        String repositoryLink = "https://github.com/sohamviradiya-work/test-repo";
        
        clean(path);
        
        GitWorker.getRemoteRepository(path, repositoryLink);
        
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run() throws IOException {
        TargetProject targetProject = TargetProject.mountLocalProject(path, "7.6.4");

        GitWorker gitFetcher = targetProject.getGitWorker();

        ArrayListWriter<GitCommit> arrayListWriter = new ArrayListWriter<GitCommit>();
        
        CSVWriter<GitCommit> csvWriter = CSVWriter.create("./results/commits-list.csv");
        
        gitFetcher.listCommits(arrayListWriter);

        ArrayList<GitCommit> gitCommits = arrayListWriter.getList();

        ProjectRunner projectRunner = targetProject.getRunner();

        runTestsCommitWise(gitFetcher, projectRunner, gitCommits);
    }

    private static void runTestsCommitWise(GitWorker gitFetcher, ProjectRunner testRunner, ArrayList<GitCommit> gitCommits)
            throws IOException {
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("./results/test-results-commitwise.csv"));
        
        ArrayList<TestResult.TestIndentifier> failingTests = getInitialResults(testRunner);

        GitCommit lastCommit = new GitCommit("","","","",new Date(),"");

        for (GitCommit gitCommit : gitCommits) {
            gitFetcher.checkoutToCommit(gitCommit.getCommitId());
            ArrayList<TestResult.TestIndentifier> toBeRemoved = new ArrayList<>();
            for(TestIndentifier testIndentifier:failingTests){
               TestResult testResult = testRunner.runSingleTest(testIndentifier.testClass, testIndentifier.testMethod);
                if(testResult.getResult()==TestResult.Result.PASSED){
                    bufferedWriter.write(lastCommit.toCSVString() + "," + testResult.toCSVString() + "\n");
                    toBeRemoved.add(testIndentifier);
                }
            }

            for(TestIndentifier testIndentifier:toBeRemoved){
                failingTests.remove(testIndentifier);
            }
            if(failingTests.isEmpty()) break;

            lastCommit = gitCommit;
        }
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    private static ArrayList<TestResult.TestIndentifier> getInitialResults(ProjectRunner testRunner) {
        ArrayList<TestResult.TestIndentifier> failingTests = new ArrayList<TestResult.TestIndentifier>();

        ArrayListWriter<TestResult> testResultsWriter = new ArrayListWriter<TestResult>();

        testRunner.runAlltests(testResultsWriter);
        ArrayList<TestResult> testResults = testResultsWriter.getList();

        for (TestResult testResult : testResults) {
            if(testResult.getResult()==TestResult.Result.FAILED){
                failingTests.add(testResult.getUniqueIdentifier());
            }
        }
        return failingTests;
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
