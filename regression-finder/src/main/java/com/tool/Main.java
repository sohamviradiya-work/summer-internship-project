package com.tool;

import java.util.Scanner;
import com.tool.git.GitWorker;

public class Main {
    public static String repositoryPath = "../test-area/repository";
    public static String resultsPath = "../results";

    public static void main(String[] args) {
        //String repositoryLink = "https://github.com/sohamviradiya-work/large-repo/";
        
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = Helper.getRepositoryLink(scanner);
        String method = Helper.getMethod(scanner);
        scanner.close();

        Helper.clean(repositoryPath);
        Helper.clean(resultsPath);

        try {
            GitWorker.getRemoteRepository(repositoryPath, repositoryLink);
            RegressionTool.run(repositoryPath, "7.6.4", method,resultsPath);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
