package com.tool;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static String path = "./test-area/repository";
    public static void main(String[] args) {
        
        
     // String repositoryLink = getRepositoryLink();
    // LocalRepository.getRemoteRepository(path, repositoryLink);

        try {
           
            TargetProject targetProject = TargetProject.mountLocalProject(path, "7.6.4");   
            TestRunner testRunner = new TestRunner(targetProject.getConnection());

            CSVWriter csvWriter = CSVWriter.createNewWriter("./test_results.csv");

            testRunner.runAlltests(csvWriter);
            
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    private static String getRepositoryLink() {
        System.out.println("Enter Repository Link");
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = scanner.nextLine();
        scanner.close();
        return repositoryLink;
    }
}




