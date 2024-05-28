package com.tool;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.DepthWalk.Commit;

import com.tool.templates.GitCommit;

import java.io.File;

public class GitFetcher {

    private Repository repository;
    private RevWalk revWalk;

    public GitFetcher(Repository repository) {
        this.repository = repository;
        this.revWalk = new RevWalk(repository);
    }

    public Repository getRepository(){
        return this.repository;
    }

    public void close(){
        revWalk.close();
    }

    static GitFetcher getRemoteRepository(String path, String link) {
        File dir = new File(path);
        try {
            System.out.println("Cloning Repository");
            Repository repository = Git.cloneRepository()
                    .setURI(link)
                    .setDirectory(dir)
                    .setCloneAllBranches(true)
                    .call()
                    .getRepository();
            System.out.println("Cloning Complete");
            return new GitFetcher(repository);

        } catch (InvalidRemoteException e) {
            System.err.println("Invalid Remote link" + e.getMessage());
        } catch (TransportException e) {
            System.err.println("Unable to clone the Repository" + e.getMessage());
        } catch (GitAPIException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public void checkoutToCommit(Repository repository,String commitTag){
        try (Git git = new Git(repository)) {
                RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
                git.checkout().setName(commit.getName()).call();
                System.out.println("Checked out to commit: " + commit.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void listCommits(ResultsWriter resultsWriter){
        
        try (Git git = new Git(repository)) {
                Iterable<RevCommit> commits = git.log().call();
                
                for (RevCommit commit : commits) {

                    String parentId;
                    if(commit.getParentCount()>0){
                        RevCommit parent = commit.getParent(0);
                        parentId = parent.getName();
                    }
                    else{
                        parentId = "HEAD";
                    }

                    resultsWriter.writeCommit(new GitCommit(commit.getAuthorIdent().getEmailAddress(), commit.getName(),parentId,"", commit.getCommitTime()));
                }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
    }


}
