package com.tool;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.tool.items.GitCommit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GitWorker {

    private Repository repository;
    private RevWalk revWalk;
    private Git git;

    public GitWorker(Repository repository) {
        this.repository = repository;
        this.revWalk = new RevWalk(repository);
        this.git = new Git(repository);
    }

    public GitWorker(Git git) {
        this.git = git;
        this.repository = git.getRepository();
        this.revWalk = new RevWalk(repository);
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void close() {
        revWalk.close();
    }

    public void checkoutToCommit(String commitTag) throws GitAPIException, IllegalArgumentException, IOException {
        RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
        git.checkout().setName(commit.getName()).call();
        System.out.println("Checked out to commit: " + commit.getName());
    }

    public HashMap<String, ArrayList<GitCommit>> listCommitsByBranch()
            throws IOException, NoHeadException, GitAPIException {
        List<Ref> branches = git.branchList().call();

        HashMap<String, ArrayList<GitCommit>> branchCommitMap = new HashMap<>();
        HashSet<String> assignedCommits = new HashSet<String>();

        for (Ref branch : branches) {
            String branchName = branch.getName();
            ObjectId branchObjectId = repository.resolve(branchName);
            if (branchObjectId == null) {
                continue;
            }
            Iterable<RevCommit> commits = git.log().add(branchObjectId).call();
            for (RevCommit commit : commits) {
                GitCommit gitCommit = GitCommit.getGitCommitFromRevCommit(branchName, commit);

                if (assignedCommits.contains(gitCommit.getCommitId()))
                    continue;

                if (!branchCommitMap.containsKey(gitCommit.getBranch()))
                    branchCommitMap.put(gitCommit.getBranch(), new ArrayList<>());

                branchCommitMap.get(gitCommit.getBranch()).add(gitCommit);
                assignedCommits.add(gitCommit.getCommitId());

            }
        }
        return branchCommitMap;
    }

    public static GitWorker getRemoteRepository(String path, String link) throws GitAPIException {
        File dir = new File(path);
        System.out.println("Cloning Repository");
        Git git = Git.cloneRepository()
                .setURI(link)
                .setDirectory(dir)
                .setCloneAllBranches(false)
                .call();
        
        git.fetch().call();
        
        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref ref : remoteBranches) {
            cloneBranchToLocal(git, ref);
        }
        System.out.println("Cloning Complete");
        return new GitWorker(git);
    }

    private static void cloneBranchToLocal(Git git, Ref ref) {
        try {
            String branchName = ref.getName().replace("refs/remotes/origin/", "");
            git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(ref.getName())
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .call();
            System.out.println("Cloned branch " + branchName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
