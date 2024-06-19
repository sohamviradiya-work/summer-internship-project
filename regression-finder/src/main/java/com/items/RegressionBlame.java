package com.items;

import com.items.interfaces.CSVItem;
import com.items.interfaces.JiraItem;
import com.items.interfaces.JiraTicket;

public class RegressionBlame implements CSVItem, JiraItem {

    enum BlameType {
        TEST_FAIL,
        TEST_WRITE
    }

    private ProjectCommit projectCommit;
    private TestIdentifier testIdentifier;
    private BlameType type;

    public RegressionBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit, boolean isTestFail) {
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

        String description;

        String summary = "Your commit " + this.projectCommit.getCommitId() + " at " + this.projectCommit.getDate()
                + " failed some tests";

        if (type == BlameType.TEST_WRITE) {

            description = "Your commit " + this.projectCommit.getCommitId() + " changed the test "
                    + this.testIdentifier.getTestProject() + ": "
                    + this.testIdentifier.getTestClass() + "." + this.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";

        } else {
            description = "Your commit " + this.projectCommit.getCommitId() + " caused the test "
                    + this.testIdentifier.getTestProject() + ":" + this.testIdentifier.getTestClass() + "."
                    + this.testIdentifier.getTestMethod() + " to fail";
        }

        return new JiraTicket(summary, description, this.projectCommit.getAuthor());
    }

    public String getInfo() {
        return this.testIdentifier.toCSVString() + this.projectCommit.getCommitId();
    }
}
