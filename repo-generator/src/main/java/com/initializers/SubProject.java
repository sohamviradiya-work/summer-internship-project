package com.initializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import com.utils.Helper;

public class SubProject {
 
    TestModuleWriter[] testModuleWriters;
    String subProjectName;
    String subProjectPath;

    public SubProject(TestModuleWriter[] testModuleWriters, String subProjectName,String subProjectPath) {
        this.testModuleWriters = testModuleWriters;
        this.subProjectName = subProjectName;
        this.subProjectPath = subProjectPath;
    }

    public static SubProject createSubProject(String rootPath,int subProjectNum,int numOfModules,int numOfClasses,int numOfMethods,int randomCeiling) throws IOException{
        String subProjectName = Helper.getSubProjectName(subProjectNum);
        String subProjectPath = rootPath + "/" + subProjectName;
        TestModuleWriter[] testModuleWriters = new TestModuleWriter[numOfModules];
        String testSrcPath = subProjectPath + "/src/test/java/com";

        for(int i=0;i<numOfModules;i++){
            testModuleWriters[i] = TestModuleWriter.createTestModule(testSrcPath, i, numOfClasses, numOfMethods, randomCeiling);
        }
        return new SubProject(testModuleWriters, subProjectName,subProjectPath);
    }

    public void writeSubProject(GradleWriter gradleWriter) throws IOException {
        Files.createDirectories(Path.of(subProjectPath));
        gradleWriter.initSubProjectGradle(subProjectName);
        for(TestModuleWriter testModuleWriter:testModuleWriters){
            testModuleWriter.writeTestModule();
        }
    }

    public static SubProject readSubProject(String rootPath, int subProjectNum) throws IOException {
        String subProjectName = Helper.getSubProjectName(subProjectNum);
        String subProjectPath = rootPath + "/" + subProjectName;
        String testSrcPath = subProjectPath + "/src/test/java/com";
        
        File[] moduleDirs = new File(testSrcPath).listFiles(File::isDirectory);

        TestModuleWriter[] testModuleWriters = Arrays.stream(moduleDirs)
                .filter(dir -> dir.getName().startsWith("module"))
                .map(moduleDir -> {
                    try {
                        int moduleNum = Integer.parseInt(moduleDir.getName().substring(6));
                        return TestModuleWriter.readTestModule(testSrcPath, moduleNum);
                    } catch (NumberFormatException | IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .toArray(TestModuleWriter[]::new);

        return new SubProject(testModuleWriters, subProjectName, subProjectPath);
    }


    public String modifySubProject(int moduleNum,int classNum,int methodNum,double failProb,int randomCeiling) throws IOException {
       return  subProjectName + testModuleWriters[moduleNum].modifyTestModule(classNum, methodNum, failProb,randomCeiling);
        
    }
}
