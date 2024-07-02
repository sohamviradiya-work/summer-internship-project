package com.tool.runners.git;

import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
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
import com.tool.Config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitWorker {

    private RevWalk revWalk;
    private Git git;
    private OutputStream logStream;

    public GitWorker(Repository repository, OutputStream logStream) {
        this.revWalk = new RevWalk(repository);
        this.git = new Git(repository);
        this.logStream = logStream;
    }

    public GitWorker(Git git) {
        this.git = git;
        this.revWalk = new RevWalk(git.getRepository());
    }

    public void close(String headRef) throws IOException, GitAPIException {
        git.checkout().setName(headRef).call();
        this.git.close();
    }

    public void checkoutToCommit(ProjectCommit projectCommit)
            throws GitAPIException, IllegalArgumentException, IOException {
        System.out
                .println("Checked out to commit: " + Config.ANSI_YELLOW + projectCommit.getInfo() + Config.ANSI_RESET);

        logStream.write((projectCommit.toCSVString() + "\n").getBytes());

        String commitTag = projectCommit.getCommitId();
        Repository repository = git.getRepository();
        RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
        try {
            git.checkout().setName(commit.getName()).call();
        } catch (CheckoutConflictException e) {
            System.out.println("Failed to checkout, Discard the changes in working directory and re-run the program");
            System.exit(0);
        }
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
                    changedFiles.add(diff.getOldPath());
                    changedFiles.add(diff.getNewPath());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return changedFiles;
    }

    public HashMap<String, ArrayList<ProjectCommit>> listCommitsByBranch(String firstCommitId,
            List<String> inputBranches, long days)
            throws IOException, NoHeadException, GitAPIException {

        Repository repository = git.getRepository();

        HashMap<String, ArrayList<ProjectCommit>> branchCommitMap = new HashMap<>();
        HashSet<String> assignedCommits = new HashSet<String>();

        List<String> branchesToConsider = new ArrayList<>(inputBranches);
        branchesToConsider.replaceAll(branchName -> ("refs/heads/" + branchName));

        HashSet<String> branchNameSet = new HashSet<>(branchesToConsider);

        List<Ref> branches = git.branchList().call();

        ProjectCommit firstCommit =  firstCommitId==null ? null : ProjectCommit.getprojectCommitFromRevCommit(null, repository.parseCommit(ObjectId.fromString(firstCommitId)));

        for (Ref branch : branches) {

            String branchName = branch.getName();

            if (!branchNameSet.contains(branchName))
                continue;

            ObjectId branchObjectId = repository.resolve(branchName);

            if (branchObjectId == null)
                continue;

            RevCommit commit = repository.parseCommit(branchObjectId);

            while (commit != null) {
                ProjectCommit projectCommit = ProjectCommit.getprojectCommitFromRevCommit(branchName, commit);

                if (days > 0 && projectCommit.getDateMilli() < System.currentTimeMillis()
                        - days * Config.MILLISECONDS_PER_DAY)
                    break;

                if (assignedCommits.contains(projectCommit.getCommitId()))
                    continue;

                branchCommitMap
                        .computeIfAbsent(branchName, k -> new ArrayList<>())
                        .add(projectCommit);

                assignedCommits.add(projectCommit.getCommitId());

                if (commit.getParentCount() == 0)
                    break;

                if (firstCommit!=null && projectCommit.getCommitId().equals(firstCommit.getCommitId()))
                    break;

                commit = revWalk.parseCommit(commit.getParent(0).getId());
            }

            if (!branchCommitMap.containsKey(branchName))
                continue;
            Collections.reverse(branchCommitMap.get(branchName));
        }
        return branchCommitMap;
    }

    public static GitWorker mountGitWorker(File directory, OutputStream logStream) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitWorker = new GitWorker(repository, logStream);
        return gitWorker;
    }

    public ArrayList<ProjectCommit> blameTest(String filePath, String testName) throws GitAPIException {
        BlameCommand blameCommand = git.blame().setFilePath(filePath);
        BlameResult blameResult = blameCommand.call();

        if (blameResult == null) {
            throw new RuntimeException("Invalid path: " + filePath + "," + testName);
        }

        String functionSignaturePattern = testName + "\\s*\\([^\\)]*\\)\\s*\\{";
        Pattern pattern = Pattern.compile(functionSignaturePattern);

        int startLine = -1;
        int endLine = -1;
        int bracketBalance = 0;
        boolean inFunction = false;

        for (int i = 0; i < blameResult.getResultContents().size(); i++) {
            String line = blameResult.getResultContents().getString(i);
            if (!inFunction) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    startLine = i;
                    inFunction = true;
                    bracketBalance = 1;
                }
            } else {
                for (char ch : line.toCharArray()) {
                    if (ch == '{') {
                        bracketBalance++;
                    } else if (ch == '}') {
                        bracketBalance--;
                        if (bracketBalance == 0) {
                            endLine = i;
                            break;
                        }
                    }
                }
                if (endLine != -1)
                    break;
            }
        }

        if (startLine == -1 || endLine == -1) {
            throw new IllegalStateException("Function not found in the file.");
        }

        HashSet<String> authorCommitIDs = new HashSet<>();
        ArrayList<ProjectCommit> authorCommits = new ArrayList<>();

        for (int i = startLine; i <= endLine; i++) {
            RevCommit commit = blameResult.getSourceCommit(i);
            if (authorCommitIDs.contains(commit.getName()))
                continue;
            else {
                authorCommits.add(ProjectCommit.getprojectCommitFromRevCommit("", commit));
                authorCommitIDs.add(commit.getName());
            }
        }

        return authorCommits;
    }
}
