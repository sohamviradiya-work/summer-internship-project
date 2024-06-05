package com.initializers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.utils.Helper;

public class TestModuleWriter {
    TestClassWriter[] testClassWriters;
    String modulePath;
    String packageName;
    
    private TestModuleWriter(TestClassWriter[] testClassWriters,String modulePath,String packageName){
        this.testClassWriters = testClassWriters;
        this.modulePath = modulePath;
        this.packageName = packageName;
    }

    public static TestModuleWriter createTestModule(String testSrcPath,int moduleNum,int numOfClasses,int numOfMethods,int randomCeiling) throws IOException{
        TestClassWriter[] testClassWriters = new TestClassWriter[numOfClasses];
        
        String moduleName = Helper.getModuleName(moduleNum);
        String modulePath = testSrcPath + "/" + moduleName + "/";
        String packageName = "com." + moduleName;

        for (int i = 0; i < numOfClasses; i++) {
            testClassWriters[i] = TestClassWriter.createTestClass(i, numOfMethods, randomCeiling);
        }
        return new TestModuleWriter(testClassWriters, modulePath,packageName);
    }

    public TestModuleWriter modifyTestModule(int classNum,int methodNum,int x, int y) throws IOException {
        this.testClassWriters[classNum].modifyTestClass(methodNum, x, y).writeToFile(modulePath, packageName);
        return this;
    }

    public void writeTestModule() throws IOException {
        Files.createDirectories(Path.of(modulePath));
        for(TestClassWriter testClassWriter:testClassWriters){
            testClassWriter.writeToFile(modulePath, packageName);
        }
    }
}
