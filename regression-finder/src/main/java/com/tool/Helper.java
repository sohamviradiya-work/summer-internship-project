package com.tool;

import java.io.File;
import java.util.Scanner;

public class Helper {

    private static final double MILLISECONDS_PER_SECOND = 1000.0;
    private static final int MILLISECONDS_PER_MINUTE = 1000*60;

    static String getRepositoryLink(Scanner scanner) {
        System.out.println("Enter Repository Link");
        String repositoryLink = scanner.nextLine();
        return repositoryLink;
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

    static String getMethod(Scanner scanner) {
        System.out.println("Enter Method (Linear, Bisect or Batch XX): ");
        String methodName = scanner.nextLine();
        return methodName;
    }

    static void printRunTime(long start,long end) {
        long milliSecs = end - start;
        long minutes = milliSecs/MILLISECONDS_PER_MINUTE;
        milliSecs %= MILLISECONDS_PER_MINUTE;

        double seconds = milliSecs/MILLISECONDS_PER_SECOND;

        System.out.println("Runtime: "+ minutes + " minutes, " + seconds + " seconds");
    }
    
}
