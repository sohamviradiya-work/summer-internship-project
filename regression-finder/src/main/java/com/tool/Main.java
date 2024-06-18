package com.tool;

import java.util.Scanner;

import com.tool.runners.RegressionTool;

public class Main {

    public static String rootPath = "../resources";

    public static void main(String[] args) throws Exception {

        String repositoryPath = rootPath + "/test-area/repository";
        String resultsPath = rootPath + "/results";
        Scanner scanner = new Scanner(System.in);

        repositoryPath = Helper.setUpRepository(repositoryPath, resultsPath, scanner);
        
        try {
            String method = Helper.getMethod(scanner);
            String testSrcPath = Helper.getTestSrcPath(scanner);
            scanner.close();

            long time = RegressionTool.run(repositoryPath, testSrcPath, "7.6.4", method, resultsPath, true);
            System.out.println("time: " + time + " ms");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
