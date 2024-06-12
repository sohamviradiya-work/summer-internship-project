package com.tool.runners.git;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import com.items.ProjectCommit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class GitWorker {

    private RevWalk revWalk;
    private Git git;

    public GitWorker(Repository repository) {
        this.revWalk = new RevWalk(repository);
        this.git = new Git(repository);
    }

    public GitWorker(Git git) {
        this.git = git;
        this.revWalk = new RevWalk(git.getRepository());
    }

    public void close() {
        this.git.close();
    }

    public void checkoutToCommit(ProjectCommit projectCommit)
            throws GitAPIException, IllegalArgumentException, IOException {
        String commitTag = projectCommit.getCommitId();
        Repository repository = git.getRepository();
        RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
        git.checkout().setName(commit.getName()).call();
    }

    public ArrayList<String> getChangedFiles(String commitTagA, String commitTagB) {
        Repository repository = git.getRepository();
        ArrayList<String> changedFiles = new ArrayList<>();

        try (RevWalk revWalk = new RevWalk(repository)) {
            ObjectId commitIdA = repository.resolve(commitTagA);
            ObjectId commitIdB = repository.resolve(commitTagB);

            if (commitIdA == null || commitIdB == null) {
                throw new IllegalArgumentException("Invalid commit ID or tag.");
            }

            RevCommit commitA = revWalk.parseCommit(commitIdA);
            RevCommit commitB = revWalk.parseCommit(commitIdB);

            CanonicalTreeParser treeParserA = new CanonicalTreeParser();
            CanonicalTreeParser treeParserB = new CanonicalTreeParser();

            try (ObjectReader reader = repository.newObjectReader()) {
                treeParserA.reset(reader, commitA.getTree());
                treeParserB.reset(reader, commitB.getTree());
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DiffFormatter diffFormatter = new DiffFormatter(out)) {
                diffFormatter.setRepository(repository);
                List<DiffEntry> diffs = diffFormatter.scan(treeParserA, treeParserB);

                for (DiffEntry diff : diffs) {
                    changedFiles.add(diff.getNewPath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return changedFiles;
    }

    public HashMap<String, ArrayList<ProjectCommit>> listCommitsByBranch()
            throws IOException, NoHeadException, GitAPIException {
        List<Ref> branches = git.branchList().call();
        Repository repository = git.getRepository();

        HashMap<String, ArrayList<ProjectCommit>> branchCommitMap = new HashMap<>();
        HashSet<String> assignedCommits = new HashSet<String>();

        for (Ref branch : branches) {

            String branchName = branch.getName();
            ObjectId branchObjectId = repository.resolve(branchName);

            if (branchObjectId == null)
                continue;

            Iterable<RevCommit> commits = git.log().add(branchObjectId).call();

            for (RevCommit commit : commits) {
                ProjectCommit projectCommit = ProjectCommit.getprojectCommitFromRevCommit(branchName, commit);

                if (assignedCommits.contains(projectCommit.getCommitId()))
                    continue;

                if (!branchCommitMap.containsKey(projectCommit.getBranch()))
                    branchCommitMap.put(projectCommit.getBranch(), new ArrayList<>());

                branchCommitMap.get(projectCommit.getBranch()).add(projectCommit);
                assignedCommits.add(projectCommit.getCommitId());
            }
            Collections.reverse(branchCommitMap.get(branchName));
        }
        return branchCommitMap;
    }

    public void restoreRepository() throws InvalidRemoteException, TransportException, GitAPIException, IOException {
        git.fetch().call();

        List<Ref> remoteBranches = git.branchList().setListMode(ListMode.REMOTE).call();

        for (Ref ref : remoteBranches) {
            String remoteBranchName = ref.getName();
            String localBranchName = remoteBranchName.replace("refs/remotes/origin/", "");

            Ref localBranch = git.getRepository().findRef(localBranchName);
            if (localBranch != null) {
                git.checkout().setName(localBranchName).call();

                git.reset().setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                        .setRef(remoteBranchName).call();
            } else {
                git.checkout().setCreateBranch(true)
                        .setName(localBranchName)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                        .setStartPoint(remoteBranchName)
                        .call();
            }
            System.out.println("Reseted branch " + localBranchName);
        }
    }

    public static GitWorker mountGitWorker(File directory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitWorker = new GitWorker(repository);
        return gitWorker;
    }

    public static GitWorker getRemoteRepository(String path, String link, long lastDays)
            throws GitAPIException, IOException {
        File dir = new File(path);

        Instant shallowSinceInstant = LocalDateTime.now().minusDays(lastDays).toInstant(ZoneOffset.UTC);

        Git git = Git.cloneRepository().setShallowSince(shallowSinceInstant)
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