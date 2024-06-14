package com.tool;

import java.util.Scanner;
import com.tool.runners.RegressionTool;
import com.tool.runners.git.RepositoryCloner;

public class Main {

    public static String rootPath = "./resources";

    public static void main(String[] args) {

        String repositoryPath = rootPath + "/test-area/repository";
        String resultsPath = rootPath + "/results";
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = Helper.getRepositoryLink(scanner);
        int days = Helper.getDays(scanner);
        try {
            Helper.setup(repositoryPath, resultsPath);
            RepositoryCloner.getRemoteRepository(repositoryPath, repositoryLink, days);
            String method = Helper.getMethod(scanner);
            String testSrcPath = Helper.getTestSrcPath(scanner);
            scanner.close();    
            
            long time = RegressionTool.run(repositoryPath, testSrcPath,"7.6.4", method, resultsPath, true);      
            System.out.println("time: " + time + " ms");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
