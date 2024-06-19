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

    public GitWorker(Repository repository) {
        this.revWalk = new RevWalk(repository);
        this.git = new Git(repository);
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
        String commitTag = projectCommit.getCommitId();
        Repository repository = git.getRepository();
        RevCommit commit = revWalk.parseCommit(repository.resolve(commitTag));
        try{
            git.checkout().setName(commit.getName()).call();
        }
        catch(CheckoutConflictException e){
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

    public HashMap<String, ArrayList<ProjectCommit>> listCommitsByBranch(String firstCommit)
            throws IOException, NoHeadException, GitAPIException {
        List<Ref> branches = git.branchList().call();
        Repository repository = git.getRepository();

        HashMap<String, ArrayList<ProjectCommit>> branchCommitMap = new HashMap<>();
        HashSet<String> assignedCommits = new HashSet<String>();

        for (Ref branch : branches) {

            String branchName = branch.getName();
            ObjectId branchObjectId = repository.resolve(branchName);

            if (branchObjectId == null || branchName == "HEAD")
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
                if (firstCommit!=null && projectCommit.getCommitId().compareTo(firstCommit) == 0)
                    break;
            }
            if (!branchCommitMap.containsKey(branchName))
                continue;
            Collections.reverse(branchCommitMap.get(branchName));
        }
        return branchCommitMap;
    }

    public static GitWorker mountGitWorker(File directory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.findGitDir(directory).build();
        GitWorker gitWorker = new GitWorker(repository);
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
