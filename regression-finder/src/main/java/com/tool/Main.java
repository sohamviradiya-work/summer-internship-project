package com.tool;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.tool.templates.CSVItem;
import com.tool.templates.GitCommit;
import com.tool.templates.RegressionBlame;
import com.tool.writers.ArrayListWriter;
import com.tool.writers.CSVWriter;

public class Main {
    public static String path = "./test-area/repository";

    public static void main(String[] args) {
        // String repositoryLink = getRepositoryLink();
        String repositoryLink = "https://github.com/sohamviradiya-work/test-repo";
        
        clean(path);
        
        GitWorker.getRemoteRepository(path, repositoryLink);
        
        try {
            run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void run() throws IOException {
        TargetProject targetProject = TargetProject.mountLocalProject(path, "7.6.4");

        GitWorker gitFetcher = targetProject.getGitWorker();

        ArrayListWriter<GitCommit> arrayListWriter = new ArrayListWriter<GitCommit>();
        
        // CSVWriter<GitCommit> csvWriter = CSVWriter.create("./results/commits-list.csv");

        gitFetcher.listCommits(arrayListWriter);

        ArrayList<GitCommit> gitCommits = arrayListWriter.getList();

        CSVWriter<RegressionBlame> csvWriter = CSVWriter.create("./results/blame-tests.csv");

        targetProject.runFailedTests(gitCommits,csvWriter);
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
