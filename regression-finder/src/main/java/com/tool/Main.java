package com.tool;

import java.util.Scanner;
import com.tool.runners.RegressionTool;
import com.tool.runners.git.GitWorker;

public class Main {

    public static String rootPath = "./resources";

    public static void main(String[] args) {
    
    String repositoryPath = rootPath + "/test-area/repository";
    String resultsPath = rootPath +  "/results";
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = Helper.getRepositoryLink(scanner);
        String method = Helper.getMethod(scanner);
        scanner.close();


        try {

        Helper.create(resultsPath);
        Helper.create(repositoryPath);
        Helper.clean(repositoryPath);

            GitWorker.getRemoteRepository(repositoryPath, repositoryLink);
            RegressionTool.run(repositoryPath, "7.6.4", method, resultsPath, true);
            // BenchMarker.benchmark(repositoryPath, resultsPath, "7.6.4");
            
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
