package com.tool;

import java.util.ArrayList;
import java.util.Scanner;

import com.items.TestIdentifier;
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
            ArrayList<TestIdentifier> testIdentifiers = Helper.getTestInputs(scanner);
            scanner.close();

            long time = RegressionTool.runWithTests(repositoryPath, testSrcPath, "7.6.4", method, resultsPath, testIdentifiers);
            System.out.println("time: " + time + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
