package com.tool.runners.git;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.sshd.JGitKeyCache;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.transport.sshd.SshdSessionFactoryBuilder;
import org.eclipse.jgit.util.FS;

import com.items.ProjectCommit;

public class RepositoryCloner {

    public static void getRemoteRepository(String path, String link, long lastDays, List<String> branches)
            throws GitAPIException, IOException {

        List<String> branchesToConsider = new ArrayList<>(branches);
        branchesToConsider.replaceAll(branch -> ("refs/heads/" + branch));

        File dir = new File(path);

        Instant shallowSinceInstant = LocalDateTime.now().minusDays(lastDays).toInstant(ZoneOffset.UTC);

        File sshDir = new File(FS.DETECTED.userHome(), ".ssh");
        SshdSessionFactory sshdSessionFactory = new SshdSessionFactoryBuilder()
                .setPreferredAuthentications("publickey,keyboard-interactive,password")
                .setHomeDirectory(FS.DETECTED.userHome())
                .setSshDirectory(sshDir).build(new JGitKeyCache());
        SshSessionFactory.setInstance(sshdSessionFactory);

        Git git = fetchFromInstant(link, branchesToConsider, dir, shallowSinceInstant);

        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();
        for (Ref ref : remoteBranches) {
            RepositoryCloner.cloneBranchToLocal(git, ref);
        }

        git.close();

        System.out.println("Cloning Complete");

        return;
    }

    private static Git fetchFromInstant(String link, List<String> branches, File dir, Instant shallowSinceInstant)
            throws GitAPIException, InvalidRemoteException, TransportException {
        return Git.cloneRepository()
                .setURI(link)
                .setBranchesToClone(branches)
                .setShallowSince(shallowSinceInstant)
                .setDirectory(dir)
                .call();
    }

    static int computeDepth(Git git, String commitToFind)
            throws GitAPIException, IOException {
        Iterable<RevCommit> commits = git.log().call();

        ProjectCommit firstCommit = ProjectCommit.getprojectCommitFromRevCommit(null, git.getRepository().parseCommit(ObjectId.fromString(commitToFind)));

        int count = 0;
        for (RevCommit commit : commits) {
            count++;
            ProjectCommit projectCommit = ProjectCommit.getprojectCommitFromRevCommit(null, commit);

            if (projectCommit.getDateMilli() < firstCommit.getDateMilli())
                break;
        }
        return count;
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
