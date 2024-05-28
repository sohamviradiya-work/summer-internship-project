package com.tool.templates;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GitCommit implements CSVItem {
    private String authorMail;
    private String commitId;
    private String parentCommitId;
    private String branch;
    private int time;
    private String message;

    public GitCommit(String authorMail, String commitId, String parentCommitId, String branch, int time,
                     String message) {
        this.authorMail = authorMail;
        this.commitId = commitId;
        this.parentCommitId = parentCommitId;
        this.branch = branch;
        this.time = time;
        this.message = message;
    }

    @Override
    public String toCSVString() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        String formattedTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return authorMail + "," + commitId + "," + parentCommitId + "," + branch + "," + formattedTime + "," + message;
    }

    public String getCommitId(){
        return commitId;
    }
}
