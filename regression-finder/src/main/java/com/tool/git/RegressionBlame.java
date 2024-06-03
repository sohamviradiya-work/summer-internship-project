package com.tool.git;

import com.tool.interfaces.CSVItem;
import com.tool.runner.TestIdentifier;

public class RegressionBlame implements CSVItem{
    private String author;
    private String commit;
    private String branch;
    private String testClass;
    private String testMethod;
    public RegressionBlame(TestIdentifier testIdentifier, GitCommit gitCommit) {
        this.author = gitCommit.getAuthor();
        this.branch = gitCommit.getBranch();
        this.commit = gitCommit.getCommitId();
        this.testClass = testIdentifier.getTestClass();
        this.testMethod = testIdentifier.getTestMethod();
    }
    
    @Override
    public String toCSVString() {
        return testClass +  "," + testMethod + "," + branch + "," + commit + "," + author; 
    }
}
