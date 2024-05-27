package com.tool;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import java.io.File;

public class GitFetcher {

    static Repository getRemoteRepository(String path, String link) {
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
            return repository;
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
