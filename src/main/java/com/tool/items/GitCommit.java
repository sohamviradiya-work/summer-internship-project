package com.tool.items;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.eclipse.jgit.revwalk.RevCommit;

import com.tool.items.interfaces.CSVItem;

public class GitCommit implements CSVItem {
    private String authorMail;
    private String commitId;
    private String parentCommitId;
    private String branch;
    private Date time;
    private String message;

    public GitCommit(String authorMail, String commitId, String parentCommitId, String branch, Date time,
                     String message) {
        this.authorMail = authorMail;
        this.commitId = commitId;
        this.parentCommitId = parentCommitId;
        this.branch = branch;
        this.time = time;
        this.message = message;
    }

    public static GitCommit createNullCommit(){
        return new GitCommit(null, null, null, null, null, null);
    }

    @Override
    public String toCSVString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getDefault()); 
                
        return authorMail + "," + commitId + "," + parentCommitId + "," + branch + "," + dateFormat.format(time) + "," + message;
    }
    
    public String getCommitId(){
        return commitId;
    }

    public String getAuthor(){
        return authorMail;
    }

    public String getBranch(){
        return branch;
    }

    public static GitCommit getGitCommitFromRevCommit(String branchName, RevCommit commit) {
        String parentId;
        if (commit.getParentCount() > 0) {
            RevCommit parent = commit.getParent(0);
            parentId = parent.getName();
        } else {
            parentId = "HEAD";
        }

        return new GitCommit(
                commit.getAuthorIdent().getEmailAddress(),
                commit.getName(),
                parentId,
                branchName,
                commit.getAuthorIdent().getWhen(),
                commit.getShortMessage());
    }
}
