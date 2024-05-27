package com.tool.templates;

public class GitCommit {
    public String authorMail;
    public String commitId;
    public String parentCommitId;
    public String branch;
    public int time;

    public GitCommit(String authorMail, String commitId, String parentCommitId, String branch,int time) {
        this.authorMail = authorMail;
        this.commitId = commitId;
        this.parentCommitId = parentCommitId;
        this.branch = branch;
        this.time = time;
    }
}
