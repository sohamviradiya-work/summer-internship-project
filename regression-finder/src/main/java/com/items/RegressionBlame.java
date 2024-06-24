package com.items;

import com.items.interfaces.CSVItem;
import com.items.interfaces.JiraItem;
import com.tool.Config;

public class RegressionBlame implements CSVItem, JiraItem {

    enum BlameType {
        TEST_FAIL,
        TEST_WRITE
    }

    private ProjectCommit projectCommit;
    private TestIdentifier testIdentifier;
    private BlameType type;

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

        String summary = "Your commit " + this.projectCommit.getCommitId() + " at " + this.projectCommit.getDateString()
                + " failed some tests";

        String description = "Your commit " + this.projectCommit.getCommitId() + " caused the test "
                + this.testIdentifier.getTestProject() + ":" + this.testIdentifier.getTestClass() + "."
                + this.testIdentifier.getTestMethod() + " to fail";

        if (this.projectCommit.getCommitId().compareTo("LAST PHASE") == 0) {
            summary = "This failing test was changed in the last phase";

            description = "Test "
                    + this.testIdentifier.getTestProject() + ": "
                    + this.testIdentifier.getTestClass() + "." + this.testIdentifier.getTestMethod()
                    + " was changed in the last phase, which has been failing since last phase";
        } else if (type == BlameType.TEST_WRITE) {
            summary = "Your commit " + this.projectCommit.getCommitId() + " at " + this.projectCommit.getDateString()
            + " changed some tests which are failing";
            
            description = "Your commit " + this.projectCommit.getCommitId() + " changed the test "
                    + this.testIdentifier.getTestProject() + ": "
                    + this.testIdentifier.getTestClass() + "." + this.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";
        }

        return new JiraTicket(summary, description, this.projectCommit.getAuthor());
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
