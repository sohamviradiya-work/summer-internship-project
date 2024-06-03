package com.tool;

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import com.tool.items.GitCommit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

    public Repository getRepository() {
        return this.git.getRepository();
    }

    public void close() {
        this.git.close();
    }

    public void checkoutToCommit(String commitTag) throws GitAPIException, IllegalArgumentException, IOException {
        Repository repository = getRepository();
        System.out.println("Checked out to commit: " + commitTag);
        RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
        git.checkout().setName(commit.getName()).call();
    }

    public boolean isFileChanged(String commitTagA, String commitTagB, String filePath) {
        Repository repository = getRepository();
        try {
            ObjectId commitIdA = repository.resolve(commitTagA);
            ObjectId commitIdB = repository.resolve(commitTagB);

            if (commitIdA == null || commitIdB == null) {
                throw new IllegalArgumentException("Invalid commit ID or tag.");
            }

            CanonicalTreeParser treeParserA = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParserA.reset(reader, revWalk.parseCommit(commitIdA).getTree());
            }

            CanonicalTreeParser treeParserB = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader()) {
                treeParserB.reset(reader, revWalk.parseCommit(commitIdB).getTree());
            }

            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                    DiffFormatter diffFormatter = new DiffFormatter(out)) {
                diffFormatter.setRepository(repository);
                List<DiffEntry> diffs = diffFormatter.scan(treeParserA, treeParserB);

                for (DiffEntry diff : diffs) {
                    if (diff.getNewPath().equals(filePath) || diff.getOldPath().equals(filePath)) {
                        return true; 
                    }
                }
            }
            return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public HashMap<String, ArrayList<GitCommit>> listCommitsByBranch()
            throws IOException, NoHeadException, GitAPIException {
        List<Ref> branches = git.branchList().call();
        Repository repository = getRepository();

        HashMap<String, ArrayList<GitCommit>> branchCommitMap = new HashMap<>();
        HashSet<String> assignedCommits = new HashSet<String>();

        for (Ref branch : branches) {
            String branchName = branch.getName();
            ObjectId branchObjectId = repository.resolve(branchName);

            if (branchObjectId == null)
                continue;

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

    public static GitWorker mountGitWorker(File directory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitWorker = new GitWorker(repository);
        return gitWorker;
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
