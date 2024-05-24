package com.main;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;

public class LocalRepository {

    public String path;
    public Repository repository;

    private LocalRepository(String path, Repository repository) {
        this.path = path;
        this.repository = repository;
    }

    static LocalRepository getLocalRepository(String path) {
        File dir = new File(path);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        try (Repository repository = builder.findGitDir(dir).build()) {
            return new LocalRepository(path, repository);
        } catch (IOException e) {
            System.out.println("Unable to open" + path);
            return null;
        }
    }

    static LocalRepository getRemoteRepository(String path, String link) {
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
            return new LocalRepository(path, repository);
        } catch (InvalidRemoteException e) {
            System.err.println("Invalid Remote link" + e.getMessage());
        } catch (TransportException e) {
            System.err.println("Unable to clone the Repository" + e.getMessage());
        } catch (GitAPIException e) {
            System.err.println(e.getMessage());
        }

        return null;
    }
}
