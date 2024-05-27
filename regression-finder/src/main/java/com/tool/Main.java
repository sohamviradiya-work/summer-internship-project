package com.tool;

import java.io.IOException;
import java.util.Scanner;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;


public class Main {
    public static String path = "./test-area/repository";
    public static void main(String[] args) {
        
        
    //  String repositoryLink = getRepositoryLink();
    //  GitFetcher gitFetcher = GitFetcher.getRemoteRepository(path, repositoryLink);
        
        try {
           
            TargetProject targetProject = TargetProject.mountLocalProject(path, "7.6.4");   
            Repository repository = targetProject.getRepository();
            GitFetcher gitFetcher = new GitFetcher(repository);
            CSVWriter csvWriter = CSVWriter.createNewWriter("./commmit-list.csv");
            gitFetcher.listCommits(csvWriter);
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




