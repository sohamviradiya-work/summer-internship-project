package com.items;

import com.items.RegressionBlame.BlameType;

public class JiraTicket {
    private String summary;
    private String description;
    private String email;

    public JiraTicket(String summary, String description, String email) {
        this.summary = summary;
        this.description = description;
        this.email = email;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    static JiraTicket convert(RegressionBlame regressionBlame) {
        String summary = "Your commit " + regressionBlame.projectCommit.getCommitId() + " at " + regressionBlame.projectCommit.getDateString()
                + " failed some tests";
    
        String description = "Your commit " + regressionBlame.projectCommit.getCommitId() + " caused the test "
                + regressionBlame.testIdentifier.getTestProject() + ":" + regressionBlame.testIdentifier.getTestClass() + "."
                + regressionBlame.testIdentifier.getTestMethod() + " to fail";
    
        if (regressionBlame.projectCommit.getCommitId().compareTo("LAST PHASE") == 0) {
            summary = "This failing test was changed in the last phase";
    
            description = "Test " + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " was changed in the last phase, which has been failing since last phase";
        } else if (regressionBlame.type == RegressionBlame.BlameType.TEST_WRITE) {
            summary = "Your commit " + regressionBlame.projectCommit.getCommitId() + " at " + regressionBlame.projectCommit.getDateString()
            + " changed some tests which are failing";
            
            description = "Your commit " + regressionBlame.projectCommit.getCommitId() + " changed the test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";
        }
    
        return new JiraTicket(summary, description, regressionBlame.projectCommit.getAuthor());
    }
}

