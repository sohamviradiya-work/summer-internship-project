package com.items;

import com.items.interfaces.CSVItem;

public class RegressionBlame implements CSVItem{

    private ProjectCommit projectCommit;
    private TestIdentifier testIdentifier;

    public RegressionBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit) {
        this.testIdentifier = testIdentifier;
        this.projectCommit = projectCommit;
    }
    
    @Override
    public String toCSVString() {
        return testIdentifier.getTestProject().substring(1) + ","+ testIdentifier.getTestClass() +  "," + testIdentifier.getTestMethod() + "," + projectCommit.getBranch() + "," + projectCommit.getCommitId() + "," + projectCommit.getAuthor(); 
    }
}
