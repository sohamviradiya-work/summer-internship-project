package com.tool.git;

import com.tool.interfaces.CSVItem;
import com.tool.runner.TestIdentifier;

public class RegressionBlame implements CSVItem{

    private GitCommit gitCommit;
    private TestIdentifier testIdentifier;

    public RegressionBlame(TestIdentifier testIdentifier, GitCommit gitCommit) {
        this.testIdentifier = testIdentifier;
        this.gitCommit = gitCommit;
    }
    
    @Override
    public String toCSVString() {
        return testIdentifier.getTestProject() + ","+ testIdentifier.getTestClass() +  "," + testIdentifier.getTestMethod() + "," + gitCommit.getBranch() + "," + gitCommit.getCommitId() + "," + gitCommit.getAuthor(); 
    }
}
