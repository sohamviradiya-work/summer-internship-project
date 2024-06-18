package com.tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.TestIdentifier;
import com.tool.runners.git.RepositoryCloner;

public class Helper {

    public static void create(String path) throws IOException {
        Path directoryPath = Paths.get(path);
        Files.createDirectories(directoryPath);
    }

    public static void clean(String path) {
        Helper.cleanDirectory(new File(path));
    }

    static void cleanDirectory(File directory) {
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

    static boolean promptYesNo(Scanner scanner) throws IOException {
        String prompt = scanner.nextLine();
        prompt = prompt.trim().toLowerCase();
        if (prompt.compareTo("yes") == 0)
            return true;
        else if (prompt.compareTo("no") == 0)
            return false;
        else
            throw new IOException("Please answer yes or no");
    }

    static String getRepositoryPath(Scanner scanner) {
        System.out.print("Get Repository path relative to root of the project: ");
        String repositoryPath = scanner.nextLine();
        return repositoryPath.trim();
    }

    static String getRepositoryLink(Scanner scanner) {
        System.out.print("Enter Repository Link: ");
        String repositoryLink = scanner.nextLine();
        return repositoryLink.trim();
    }

    static String getMethod(Scanner scanner) {
        System.out.print("Enter Method (Linear, Bisect or Batch XX): ");
        String methodName = scanner.nextLine();
        return methodName.trim();
    }

    static String getTestSrcPath(Scanner scanner) {
        System.out.print("Enter Test Source Folder path relative to subproject/project root (ex: src/test/java/): ");
        String testPath = scanner.nextLine();
        return testPath.trim();
    }

    static int getDays(Scanner scanner) {
        System.out.print("Enter Number of days to consider: ");
        String days = scanner.nextLine().trim();
        return Integer.parseInt(days);
    }

    static ArrayList<TestIdentifier> getTestInputs(Scanner scanner) throws IOException {
        System.out.print("Do you have a test info csv file? (yes/no): ");
        boolean isFileAvaliable = promptYesNo(scanner);
        if (isFileAvaliable) {
            return getTestInputFromFile(scanner);
        } else {
            return getTestInputManually(scanner);
        }
    }

    private static ArrayList<TestIdentifier> getTestInputFromFile(Scanner scanner) throws IOException {
        ArrayList<TestIdentifier> testIdentifiers = new ArrayList<>();
        System.out.print("Enter file path relative to project root: ");
        String filePath = "../" + scanner.nextLine().trim();
        Path path = Paths.get(filePath);
         List<String> lines = Files.readAllLines(path);
        
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String testProject = ":" + parts[0].trim();
                if(testProject.length()==1)
                    testProject = "";
                String testClass = parts[1].trim();
                String testMethod = parts[2].trim();
                testIdentifiers.add(new TestIdentifier(testProject, testClass, testMethod));
            } else {
                System.out.println("Invalid format in line: " + line);
            }
        }

        return testIdentifiers;
    }

    private static ArrayList<TestIdentifier> getTestInputManually(Scanner scanner) {
        ArrayList<TestIdentifier> testIdentifiers = new ArrayList<>();
        System.out.print("Enter num of tests: ");
        int num = Integer.parseInt(scanner.nextLine());
        for (int i = 1; i <= num; i++) {
            System.out.print("Enter test project for test no. " + i + ": ");
            String testProject = ":" + scanner.nextLine().trim();

            System.out.print("Enter full class (com.package.class) for test no. " + i + ": ");
            String testClass = scanner.nextLine().trim();

            System.out.print("Enter method name for test no. " + i + ": ");
            String testMethod = scanner.nextLine().trim();

            testIdentifiers.add(new TestIdentifier(testProject, testClass, testMethod));
        }
        return testIdentifiers;
    }

    static void setupWithLink(String repositoryPath, String resultsPath, String repositoryLink, long days)
            throws IOException, GitAPIException {

        create(repositoryPath);
        clean(repositoryPath);
        RepositoryCloner.getRemoteRepository(repositoryPath, repositoryLink, days);
    }

    static String setUpRepository(String repositoryPath, String resultsPath, Scanner scanner)
            throws Exception, IOException, GitAPIException {

        create(resultsPath);
        clean(resultsPath);

        System.out.print("Is repo already cloned? enter yes/no: ");

        boolean isRepoCloned = promptYesNo(scanner);

        if (isRepoCloned) {
            repositoryPath = "../" + getRepositoryPath(scanner) + "/";
        } else {
            String repositoryLink = getRepositoryLink(scanner);
            int days = getDays(scanner);
            setupWithLink(repositoryPath, resultsPath, repositoryLink, days);
        }
        return repositoryPath;
    }
}
