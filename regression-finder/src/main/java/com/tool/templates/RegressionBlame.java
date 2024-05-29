package com.tool.templates;

import com.tool.templates.TestResult.TestIndentifier;

public class RegressionBlame implements CSVItem{
    private String author;
    private String commit;
    private String branch;
    private String testClass;
    private String testMethod;
    public RegressionBlame(TestIndentifier testIndentifier, GitCommit gitCommit) {
        this.author = gitCommit.getAuthor();
        this.branch = gitCommit.getBranch();
        this.commit = gitCommit.getCommitId();
        this.testClass = testIndentifier.getTestClass();
        this.testMethod = testIndentifier.getTestMethod();
    }
    
    @Override
    public String toCSVString() {
        return testClass +  "," + testMethod + "," + branch + "," + commit + "," + author; 
    }
}
