package com.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;

public class GitWorker {

    private Git git;
    private CredentialsProvider credentialsProvider;
    private String username;
    private String email;

    private GitWorker(Git git, String username,String email,String token) {
        
        this.credentialsProvider = new UsernamePasswordCredentialsProvider(email, token);
        this.git = git;
        this.username = username;
        this.email = email;
    }

    public static GitWorker mountGitWorker(String path, String username,String email, String token)
            throws IOException, GitAPIException {
        Repository repository = new FileRepositoryBuilder().setGitDir(new File(path + "/.git"))
                .setWorkTree(new File(path))
                .readEnvironment()
                .build();
        Git git = new Git(repository);
        GitWorker gitWorker = new GitWorker(git, username, email, token);
        return gitWorker;
    }

    public void pushCommit() throws InvalidRemoteException, GitAPIException {
        git.push().setCredentialsProvider(credentialsProvider).setPushAll().call();
    }

    public void postCommit(String commitMessage)
            throws GitAPIException, IOException {

        git.add().addFilepattern(".").call();

        RevCommit revCommit = git.commit().setCommitter(new PersonIdent(username, email)).setMessage(commitMessage).call();

        System.out.println("Committed: " + revCommit.getName() + "," + revCommit.getShortMessage());
    }
}
