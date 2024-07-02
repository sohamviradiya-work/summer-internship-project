package com.items;

public class JiraTicket {
    private String description;
    private String email;

    public JiraTicket(String description, String email) {
        this.description = description;
        this.email = email;
    }


    public String getDescription() {
        return description;
    }

    public String getEmail() {
        return email;
    }

    static JiraTicket convert(RegressionBlame regressionBlame) {
    
        String description = "Your commit " + regressionBlame.projectCommit.getCommitId() + " caused the test "
                + regressionBlame.testIdentifier.getTestProject() + ":" + regressionBlame.testIdentifier.getTestClass() + "."
                + regressionBlame.testIdentifier.getTestMethod() + " to fail... \n";

        if(regressionBlame.getStackTrace()!=null)
            description = description + regressionBlame.getStackTrace();
    
        if (regressionBlame.projectCommit.getCommitId().compareTo("LAST PHASE") == 0) {
    
            description = "Test " + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " was changed in the last phase, which has been failing since last phase";
        } else if (regressionBlame.type == RegressionBlame.BlameType.TEST_WRITE) {
            
            
            description = "Your commit " + regressionBlame.projectCommit.getCommitId() + " changed the test "
                    + regressionBlame.testIdentifier.getTestProject() + ": "
                    + regressionBlame.testIdentifier.getTestClass() + "." + regressionBlame.testIdentifier.getTestMethod()
                    + " which has been failing since last phase";
        }
    
        return new JiraTicket(description, regressionBlame.projectCommit.getAuthor());
    }
}

