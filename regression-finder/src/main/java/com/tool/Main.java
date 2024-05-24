package com.tool;

import java.util.Scanner;

public class Main {
    public static String path = "./src/main/resources/repository";
    public static void main(String[] args) {
        System.out.println("Enter Repository Link");
        Scanner scanner = new Scanner(System.in);
        String repositoryLink = scanner.nextLine();
        
        LocalRepository.getRemoteRepository(path, repositoryLink);

        scanner.close();
    }
}




