package com.items;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevCommit;

import com.items.interfaces.CSVItem;

public class ProjectCommit implements CSVItem {
    private String authorMail;
    private String commitId;
    private String branch;
    private Date time;
    private String message;

    public ProjectCommit(String authorMail, String commitId, String branch, Date time,
            String message) {
        this.authorMail = authorMail;
        this.commitId = commitId;
        this.branch = branch;
        this.time = time;
        this.message = message;
    }

    @Override
    public String toCSVString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());

        return authorMail + "," + commitId + "," + dateFormat.format(time) + "," + message;
    }

    public String getCommitId() {
        return commitId;
    }

    public String getAuthor() {
        return authorMail;
    }

    public String getBranch() {
        return branch;
    }

    public static ProjectCommit getprojectCommitFromRevCommit(String branchName, RevCommit commit) {
        return new ProjectCommit(
                commit.getAuthorIdent().getEmailAddress(),
                commit.getName(),
                branchName,
                commit.getAuthorIdent().getWhen(),
                commit.getShortMessage());
    }
}
