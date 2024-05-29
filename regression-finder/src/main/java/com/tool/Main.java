package com.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.tool.templates.GitCommit;
import com.tool.templates.RegressionBlame;
import com.tool.writers.CSVWriter;

public class Main {
    public static String path = "./test-area/repository";

    public static void main(String[] args) {
        String repositoryLink = "https://github.com/sohamviradiya-work/test-repo";
        clean(path);
        GitWorker.getRemoteRepository(path, repositoryLink);

        try {
            run();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private static void run() throws IOException, NoHeadException, GitAPIException {
        TargetProject targetProject = TargetProject.mountLocalProject(path, "7.6.4");

        CSVWriter<GitCommit> csvWriter = CSVWriter.create("./results/commits-list.csv");

        HashMap<String, ArrayList<GitCommit>> branchCommitMap = targetProject.getGitWorker().listCommitsByBranch();

        for(Entry<String, ArrayList<GitCommit>> entry:branchCommitMap.entrySet()){
            for(GitCommit gitCommit:entry.getValue()){
                csvWriter.write(gitCommit);
            }
        }

        CSVWriter<RegressionBlame> blameCSVWriter = CSVWriter.create("./results/blame-tests.csv");
        targetProject.runFailedTestsBranchWise(blameCSVWriter);
    }

    private static String getRepositoryLink() {
        System.out.println("Enter Repository Link");
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = scanner.nextLine();
        scanner.close();
        return repositoryLink;
    }

    public static void clean(String path) {
        cleanDirectory(new File(path));
    }

    private static void cleanDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
                file.delete();
            }
        }
    }

}
