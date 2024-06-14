package com.tool;

import com.utils.Helper;

import io.github.cdimascio.dotenv.Dotenv;

public class Main {
    static int ITERATIONS = 10000;
    static int MODULES_PER_SUB_PROJECT = 5;
    static int CLASSES_PER_MODULE = 4;
    static int TESTS_PER_CLASS = 10;
    static int RANDOM_INT_LIMIT = 10;
    static String rootPath = "../test-area/large-repo";
    static String projectName = "large-repo";
    static double ADDITION_PROB = 0.01;
    static double FAIL_PROB = 0.01;
    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure().directory("../").load();

        String email = dotenv.get("GITHUB_EMAIL");
        String username = dotenv.get("GITHUB_USERNAME");
        String token = dotenv.get("GITHUB_ACCESS_TOKEN");
        String remote = dotenv.get("GITHUB_REMOTE_LINK");

        try {
            TargetProject targetProject = TargetProject.initializeProject(rootPath,projectName,username,email,token,remote,true);
            
            for(int i=0;i<ITERATIONS;i++){
                if(Math.random() < ADDITION_PROB || targetProject.getNumOfSubprojects()==0){                    
                    targetProject.addSubProject(MODULES_PER_SUB_PROJECT, CLASSES_PER_MODULE, TESTS_PER_CLASS, RANDOM_INT_LIMIT);
                    continue;
                }
                int randomSubProject = Helper.getRandom(targetProject.getNumOfSubprojects());
                int randomModuleNumber = Helper.getRandom(MODULES_PER_SUB_PROJECT);
                int randomClassNumber = Helper.getRandom(CLASSES_PER_MODULE);
                int randomMethodNumber = Helper.getRandom(TESTS_PER_CLASS);

                targetProject.modifyProject(randomSubProject, randomModuleNumber, randomClassNumber, randomMethodNumber, FAIL_PROB,RANDOM_INT_LIMIT);
            }
      
            targetProject.pushChanges();

        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
