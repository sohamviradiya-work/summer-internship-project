package com.tool;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.utils.Helper;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    static int ITERATIONS = 500;
    static int SUB_PROJECTS = 2;
    static int MODULES_PER_SUB_PROJECT = 3;
    static int CLASSES_PER_MODULE = 5;
    static int TESTS_PER_CLASS = 5;
    static int RANDOM_INT_LIMIT = 5;
    static String rootPath = "../test-area/large-repo";
    static String projectName = "large-repo";
     
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().directory("../").load();

        String email = dotenv.get("GITHUB_EMAIL");
        String username = dotenv.get("GITHUB_USERNAME");
        String token = dotenv.get("GITHUB_ACCESS_TOKEN");

        boolean pass_after_fail = dotenv.get("PASS_AFTER_FAIL")=="TRUE";    

        try {
            TargetProject targetProject = TargetProject.initializeProject(rootPath,projectName,username,email,token);
            
            targetProject.populate(SUB_PROJECTS,MODULES_PER_SUB_PROJECT,CLASSES_PER_MODULE, TESTS_PER_CLASS, RANDOM_INT_LIMIT);
            
            for(int i=0;i<ITERATIONS;i++){
                int randomSubProject = Helper.getRandom(SUB_PROJECTS);
                int randomModuleNumber = Helper.getRandom(MODULES_PER_SUB_PROJECT);
                int randomClassNumber = Helper.getRandom(CLASSES_PER_MODULE);
                int randomMethodNumber = Helper.getRandom(MODULES_PER_SUB_PROJECT);

                int x = Helper.getRandom(RANDOM_INT_LIMIT);
                int y = pass_after_fail ? Helper.getRandom(RANDOM_INT_LIMIT) : Helper.getRandom(RANDOM_INT_LIMIT,x);

                targetProject.modifyProject(randomSubProject, randomModuleNumber, randomClassNumber, randomMethodNumber, x, y);
            }
      
            targetProject.pushChanges();

        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        
    }
}
