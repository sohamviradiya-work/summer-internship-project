package com.items;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevCommit;

import com.items.interfaces.CSVItem;
import com.tool.Config;

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
        return authorMail + "," + commitId + "," + getDateString() + "," + message;
    }

    public String getDateString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getDefault());
        return dateFormat.format(time);
    }

    public long getDateMilli() {
        return time.getTime();
    }

    public String getInfo(){
        return commitId + ", " + getDateString();
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
                Date.from(Instant.ofEpochMilli(commit.getCommitTime()*Config.MILLISECONDS_PER_SECOND)),
                commit.getShortMessage());
    }

    public static ProjectCommit getLastPhaseCommit() {
        return new ProjectCommit("LAST PHASE", "LAST PHASE", "LAST PHASE", Date.from(Instant.now()), "LAST PHASE");
    }
}
