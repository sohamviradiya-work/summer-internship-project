package com.tool;

import java.util.Scanner;
import com.tool.git.GitWorker;

public class Main {
    public static String repositoryPath = "../test-area/repository";
    public static String resultsPath = "../results";

    public static void main(String[] args) {
        
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = Helper.getRepositoryLink(scanner);
        // String method = Helper.getMethod(scanner);
        scanner.close();

        Helper.clean(repositoryPath);
        Helper.clean(resultsPath);

        try {
            GitWorker.getRemoteRepository(repositoryPath, repositoryLink);
            BenchMarker.benchmark(repositoryPath, resultsPath, "7.6.4");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
