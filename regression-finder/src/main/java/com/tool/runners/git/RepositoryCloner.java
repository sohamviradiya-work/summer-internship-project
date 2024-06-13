package com.tool.runners.git;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;

public class RepositoryCloner {

    public static GitWorker getRemoteRepository(String path, String link, long lastDays)
            throws GitAPIException, IOException {
        File dir = new File(path);
    
        Instant shallowSinceInstant = LocalDateTime.now().minusDays(lastDays).toInstant(ZoneOffset.UTC);
    
        Git git = Git.cloneRepository().setShallowSince(shallowSinceInstant)
                .setURI(link)
                .setDirectory(dir)
                .call();
    
        git.fetch().call();
    
        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref ref : remoteBranches) {
            RepositoryCloner.cloneBranchToLocal(git, ref);
        }
        System.out.println("Cloning Complete");
        return new GitWorker(git);
    }

    static void cloneBranchToLocal(Git git, Ref ref) {
        try {
            String branchName = ref.getName().replace("refs/remotes/origin/", "");
            git.branchCreate()
                    .setName(branchName)
                    .setStartPoint(ref.getName())
                    .setUpstreamMode(SetupUpstreamMode.TRACK)
                    .call();
            System.out.println("Cloned branch " + branchName);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
}
