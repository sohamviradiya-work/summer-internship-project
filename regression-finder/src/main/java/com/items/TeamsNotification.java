package com.items;

public class TeamsNotification {
    private String preview;
    private String content;
    private String email;

    public TeamsNotification(String preview, String content, String email) {
        this.preview = preview;
        this.content = content;
        this.email = email;
    }

    public String getPreview() {
        return preview;
    }

    public String getContent() {
        return content;
    }

    public String getEmail() {
        return email;
    }

    static TeamsNotification convert(RegressionBlame regressionBlame) {
        String preview = "Your commit " + regressionBlame.projectCommit.getCommitId() + " at " + regressionBlame.projectCommit.getDateString()
                + " failed some tests";
    
        String content = "Your commit " + regressionBlame.projectCommit.getCommitId() + " caused the test "
                + regressionBlame.testIdentifier.getTestProject() + ":" + regressionBlame.testIdentifier.getTestClass() + "."
                + regressionBlame.testIdentifier.getTestMethod() + " to fail";
    
        if (regressionBlame.projectCommit.getCommitId().compareTo("LAST PHASE") == 0) {
            preview = "This failing test was changed in the last phase";
    
            content = "Test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " was changed in the last phase, which has been failing since last phase";
        } else if (regressionBlame.type == RegressionBlame.BlameType.TEST_WRITE) {
            preview = "Your commit " + regressionBlame.projectCommit.getCommitId() + " at " + regressionBlame.projectCommit.getDateString()
            + " changed some tests which are failing";
            
            content = "Your commit " + regressionBlame.projectCommit.getCommitId() + " changed the test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";
        }
    
        return new TeamsNotification(preview, content, regressionBlame.projectCommit.getAuthor());
    }
}
