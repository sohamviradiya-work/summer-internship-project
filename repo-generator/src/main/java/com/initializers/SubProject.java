package com.initializers;

import java.io.IOException;

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
        gradleWriter.initSubProjectGradle(subProjectName);
        for(TestModuleWriter testModuleWriter:testModuleWriters){
            testModuleWriter.writeTestModule();
        }
    }

    public void modifySubProject(int moduleNum,int classNum,int methodNum,int x, int y) throws IOException {
        testModuleWriters[moduleNum].modifyTestModule(classNum, methodNum, x, y);
    }
}
