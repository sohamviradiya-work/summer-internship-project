package com.tool;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import com.tool.templates.GitCommit;
import com.tool.writers.ItemWriter;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GitWorker {

    private Repository repository;
    private RevWalk revWalk;

    public GitWorker(Repository repository) {
        this.repository = repository;
        this.revWalk = new RevWalk(repository);
    }

    public Repository getRepository() {
        return this.repository;
    }

    public void close() {
        revWalk.close();
    }

    static GitWorker getRemoteRepository(String path, String link) {
        File dir = new File(path);
        try {
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

            return new GitWorker(git.getRepository());

        } catch (InvalidRemoteException e) {
            System.err.println("Invalid Remote link" + e.getMessage());
        } catch (TransportException e) {
            System.err.println("Unable to clone the Repository" + e.getMessage());
        } catch (GitAPIException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    private static void cloneBranchToLocal(Git git, Ref ref) {
        try{
        String branchName = ref.getName().replace("refs/remotes/origin/", "");
        git.branchCreate()
                .setName(branchName)
                .setStartPoint(ref.getName())
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                .call();
            System.out.println("Cloned branch " + branchName);
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void checkoutToCommit(String commitTag) {
        try (Git git = new Git(repository)) {
            RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
            git.checkout().setName(commit.getName()).call();
            System.out.println("Checked out to commit: " + commit.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listCommits(ItemWriter<GitCommit> commitsWriter) throws IOException {
        try (Git git = new Git(repository)) {
            
            List<Ref> branches = git.branchList().call();

            for (Ref branch : branches) {
                String branchName = branch.getName();
                
                ObjectId branchObjectId = repository.resolve(branchName);
                if (branchObjectId == null) {
                    continue;
                }

                Iterable<RevCommit> commits = git.log().add(branchObjectId).call();

                for (RevCommit commit : commits) {
                    String parentId;
                    if (commit.getParentCount() > 0) {
                        RevCommit parent = commit.getParent(0);
                        parentId = parent.getName();
                    } else {
                        parentId = "HEAD";
                    }

                    commitsWriter.write(new GitCommit(
                            commit.getAuthorIdent().getEmailAddress(),
                            commit.getName(),
                            parentId,
                            branchName,
                            commit.getAuthorIdent().getWhen(),
                            commit.getShortMessage()));
                }
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }
}
