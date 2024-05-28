package com.tool.writers;

import java.util.ArrayList;

import com.tool.templates.GitCommit;
import com.tool.templates.TestResult;

public class ArrayListWriter implements ResultsWriter {

    private ArrayList<TestResult> testResults;
    private ArrayList<GitCommit> gitCommits;

    public ArrayListWriter() {
        this.testResults = new ArrayList<TestResult>();
        this.gitCommits = new ArrayList<GitCommit>();
    }

    @Override
    public void writeTestResult(TestResult testResult) {
        testResults.add(testResult);
    }

    @Override
    public void writeCommit(GitCommit gitCommit) {
        gitCommits.add(gitCommit);
    }

    public ArrayList<TestResult> getTestResults(){
        return testResults;
    }


    public ArrayList<GitCommit> getGitCommits(){
        return gitCommits;
    }
    
}
