package com.tool;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.utils.Helper;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    static int ITERATIONS =1000;
    static int SUB_PROJECTS = 10;
    static int MODULES_PER_SUB_PROJECT = 5;
    static int CLASSES_PER_MODULE = 4;
    static int TESTS_PER_CLASS = 5;
    static int RANDOM_INT_LIMIT = 2;
    static String rootPath = "../test-area/large-repo";
    static String projectName = "large-repo";
    public static void main(String[] args) {
        System.out.println("Hello World!");

        Dotenv dotenv = Dotenv.configure().directory("../").load();

        String email = dotenv.get("GITHUB_EMAIL");
        String username = dotenv.get("GITHUB_USERNAME");
        String token = dotenv.get("GITHUB_ACCESS_TOKEN");

        try {
            TargetProject targetProject = TargetProject.initializeProject(rootPath,projectName,username,email,token);
            
            targetProject.populate(SUB_PROJECTS,MODULES_PER_SUB_PROJECT,CLASSES_PER_MODULE, TESTS_PER_CLASS, RANDOM_INT_LIMIT);
            
            for(int i=0;i<ITERATIONS;i++){
                int randomSubProject = Helper.getRandom(SUB_PROJECTS);
                int randomModuleNumber = Helper.getRandom(MODULES_PER_SUB_PROJECT);
                int randomClassNumber = Helper.getRandom(CLASSES_PER_MODULE);
                int randomMethodNumber = Helper.getRandom(MODULES_PER_SUB_PROJECT);
                int x = Helper.getRandom(RANDOM_INT_LIMIT);
                int y = Helper.getRandom(RANDOM_INT_LIMIT);
                targetProject.modifyProject(randomSubProject, randomModuleNumber, randomClassNumber, randomMethodNumber, x, y);
            }
      
            targetProject.pushChanges();

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        
    }
}
