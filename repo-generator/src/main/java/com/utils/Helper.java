package com.utils;

public class Helper {

    public static int getRandom(int n) {
        return (int)(Math.random()*n);
    }

    public static int getRandom(int n,int exclude) {
        int random = exclude;
        while(random == exclude)
             random = (int)(Math.random()*n);
        return random;
    }
    
    public static String getTestMethodName(int num){
        return "testMethod" + num;
    }

    public static String getTestClassName(int num){
        return "TestClass" + num;
    }

    public static String getModuleName(int num){
        return "module" + num;
    }

    public static String getSubProjectName(int num){
        return "project" + num;
    }
}
