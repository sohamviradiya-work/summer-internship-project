package com.items;

import com.items.interfaces.CSVItem;

public class RegressionBlame implements CSVItem{

    enum BlameType {
        TEST_FAIL,
        TEST_WRITE
    }

    private ProjectCommit projectCommit;
    private TestIdentifier testIdentifier;
    private BlameType type;

    public RegressionBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit,boolean isTestFail) {
        this.testIdentifier = testIdentifier;
        this.projectCommit = projectCommit;
        this.type = isTestFail ? BlameType.TEST_FAIL : BlameType.TEST_WRITE;
    }
    
    @Override
    public String toCSVString() {
        return testIdentifier.getTestProject().substring(1) + ","+ testIdentifier.getTestClass() +  "," + testIdentifier.getTestMethod()+ "," + projectCommit.getCommitId() + "," + projectCommit.getAuthor() + "," + type.toString(); 
    }
}
