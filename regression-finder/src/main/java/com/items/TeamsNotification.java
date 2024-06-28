package com.items;

public class TeamsNotification {
    private String content;
    private String email;

    public TeamsNotification(String content, String email) {
        this.content = content;
        this.email = email;
    }


    public String getContent() {
        return content;
    }

    public String getEmail() {
        return email;
    }

    static TeamsNotification convert(RegressionBlame regressionBlame) {
        
        String content = "Your commit " + regressionBlame.projectCommit.getCommitId() + " caused the test "
                + regressionBlame.testIdentifier.getTestProject() + ":" + regressionBlame.testIdentifier.getTestClass() + "."
                + regressionBlame.testIdentifier.getTestMethod() + " to fail";

        if(regressionBlame.getStackTrace() != null)
            content = content + "\n" + regressionBlame.getStackTrace();
    
        if (regressionBlame.projectCommit.getCommitId().compareTo("LAST PHASE") == 0) {
    
            content = "Test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " was changed in the last phase, which has been failing since last phase";
        } else if (regressionBlame.type == RegressionBlame.BlameType.TEST_WRITE) {
            
            content = "Your commit " + regressionBlame.projectCommit.getCommitId() + " changed the test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";
        }
    
        return new TeamsNotification(content, regressionBlame.projectCommit.getAuthor());
    }
}
