package com.items;

import com.items.interfaces.CSVItem;
import com.items.interfaces.JiraItem;
import com.items.interfaces.TeamsItem;
import com.tool.Config;

public class RegressionBlame implements CSVItem, JiraItem, TeamsItem {

    enum BlameType {
        TEST_FAIL,
        TEST_WRITE
    }

    ProjectCommit projectCommit;
    TestIdentifier testIdentifier;
    BlameType type;

    private RegressionBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit, boolean isTestFail) {
        this.testIdentifier = testIdentifier;
        this.projectCommit = projectCommit;
        this.type = isTestFail ? BlameType.TEST_FAIL : BlameType.TEST_WRITE;
    }

    @Override
    public String toCSVString() {
        return testIdentifier.getTestProject() + "," + testIdentifier.getTestClass() + ","
                + testIdentifier.getTestMethod() + "," + projectCommit.getCommitId() + "," + projectCommit.getAuthor()
                + "," + type.toString();
    }

    @Override
    public JiraTicket toJiraTicket() {
        return JiraTicket.convert(this);
    }

    @Override
    public TeamsNotification toTeamsNotification(){
        return TeamsNotification.convert(this);
    }

    public static RegressionBlame constructBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit,boolean isTestFail){
        RegressionBlame regressionBlame = new RegressionBlame(testIdentifier, projectCommit,isTestFail);
        System.out.println("Blame found: "+ Config.ANSI_CYAN + regressionBlame.getInfo() + Config.ANSI_RESET);
        return regressionBlame;
    }

    public String getInfo() {
        return this.testIdentifier.toCSVString() + ", " + this.projectCommit.getCommitId();
    }
}
