package com.tool;

import java.io.File;
import java.util.Scanner;
import com.tool.git.GitWorker;

public class Main {
    public static String repositoryPath = "../test-area/repository";
    public static String resultsPath = "../results";

    public static void main(String[] args) {
        //String repositoryLink = "https://github.com/sohamviradiya-work/large-repo/";
        String repositoryLink = getRepositoryLink();
        String method = getMethod();
        clean(repositoryPath);
        clean(resultsPath);

        try {
            GitWorker.getRemoteRepository(repositoryPath, repositoryLink);
            RegressionTool.run(repositoryPath, "7.6.4", method,resultsPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String getRepositoryLink() {
        System.out.println("Enter Repository Link");
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = scanner.nextLine();
        return repositoryLink;
    }

    private static String getMethod() {
        System.out.println("Enter Method (Linear, Bisect or Batch XX): ");
        Scanner scanner = new Scanner(System.in);
        String methodName = scanner.nextLine();
        scanner.close();
        return methodName;
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
