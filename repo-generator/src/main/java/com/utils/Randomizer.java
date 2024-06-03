package com.utils;

public class Randomizer {

    public static int getRandom(int n) {
        return (int)(Math.random()*n);
    }
    
    public static String getRandomTestMethodName(int range){
        return "testMethod" + getRandom(range);
    }

    public static String getRandomTestClassName(int range){
        return "TestClass" + getRandom(range);
    }

    public static String getRandomModuleName(int range){
        return "module" + getRandom(range);
    }

    public static String getRandomSubProjectName(int range){
        return "project" + getRandom(range);
    }
}
