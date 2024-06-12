package com.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class GitWorker {

    private Git git;
    private CredentialsProvider credentialsProvider;
    private String username;
    private String email;

    private GitWorker(Git git, String username, String email, String token) {

        this.credentialsProvider = new UsernamePasswordCredentialsProvider(email, token);
        this.git = git;
        this.username = username;
        this.email = email;
    }

    public static GitWorker mountNewGitWorker(String path, String username, String email, String token, String remote)
            throws IOException, GitAPIException, URISyntaxException {
        File repoDir = new File(path);
        Git git = Git.init().setDirectory(repoDir).call();

        git.getRepository().getConfig().setString("user", null, "name", username);
        git.getRepository().getConfig().setString("user", null, "email", email);
        git.getRepository().getConfig().save();

        git.commit()
                .setMessage("Initial commit")
                .setAuthor(new PersonIdent(username, email))
                .setCommitter(new PersonIdent(username, email))
                .call();

        git.remoteAdd()
                .setName("origin")
                .setUri(new URIish(remote))
                .call();

        return new GitWorker(git, username, email, token);
    }

    public static GitWorker cloneNewGitWorker(String path, String username, String email, String token, String remote)
            throws IOException, GitAPIException, URISyntaxException {
        File repoDir = new File(path);

        Git git = Git.cloneRepository()
                .setURI(remote)
                .setDirectory(repoDir)
                .call();

        git.getRepository().getConfig().setString("user", null, "name", username);
        git.getRepository().getConfig().setString("user", null, "email", email);
        git.getRepository().getConfig().save();

        return new GitWorker(git, username, email, token);
    }

    public void pushCommit() throws InvalidRemoteException, GitAPIException {
        git.push().setCredentialsProvider(credentialsProvider).setForce(true).setPushAll().call();
    }

    public void postCommit(String commitMessage)
            throws GitAPIException, IOException {

        git.add().addFilepattern(".").call();

        RevCommit revCommit = git.commit().setCommitter(new PersonIdent(username, email)).setMessage(commitMessage)
                .call();

        System.out.println("Committed: " + revCommit.getName() + ", " + revCommit.getShortMessage());
    }

    private void hardReset(String commitId) throws GitAPIException {
        git.reset().setMode(ResetCommand.ResetType.HARD).setRef(commitId).call();
    }

    public void resetRepository() throws GitAPIException, RevisionSyntaxException, MissingObjectException,
            IncorrectObjectTypeException, AmbiguousObjectException, IOException {
        try (RevWalk revWalk = new RevWalk(git.getRepository())) {
            revWalk.markStart(revWalk.parseCommit(git.getRepository().resolve("HEAD")));
            revWalk.sort(RevSort.COMMIT_TIME_DESC);
            revWalk.sort(RevSort.REVERSE);
            RevCommit commit = revWalk.next();
            if (commit != null) {
                hardReset(commit.getName());
            }
        }
    }
}
