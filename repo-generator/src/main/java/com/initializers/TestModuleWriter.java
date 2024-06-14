package com.initializers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import com.utils.Helper;

public class TestModuleWriter {
    TestClassWriter[] testClassWriters;
    String modulePath;
    String packageName;

    private TestModuleWriter(TestClassWriter[] testClassWriters, String modulePath, String packageName) {
        this.testClassWriters = testClassWriters;
        this.modulePath = modulePath;
        this.packageName = packageName;
    }

    public static TestModuleWriter createTestModule(String testSrcPath, int moduleNum, int numOfClasses,
            int numOfMethods, int randomCeiling) throws IOException {
        TestClassWriter[] testClassWriters = new TestClassWriter[numOfClasses];

        String moduleName = Helper.getModuleName(moduleNum);
        String modulePath = testSrcPath + "/" + moduleName + "/";
        String packageName = "com." + moduleName;

        for (int i = 0; i < numOfClasses; i++) {
            testClassWriters[i] = TestClassWriter.createTestClass(i, numOfMethods, randomCeiling);
        }
        return new TestModuleWriter(testClassWriters, modulePath, packageName);
    }

    public String modifyTestModule(int classNum, int methodNum, double failProb, int randomCeiling) throws IOException {
        String commitMessage = "." + packageName
                + this.testClassWriters[classNum].modifyTestClass(methodNum, failProb, randomCeiling);
        this.testClassWriters[classNum].writeToFile(modulePath, packageName);
        return commitMessage;
    }

    public void writeTestModule() throws IOException {
        Files.createDirectories(Path.of(modulePath));
        for (TestClassWriter testClassWriter : testClassWriters) {
            testClassWriter.writeToFile(modulePath, packageName);
        }
    }

    public static TestModuleWriter readTestModule(String testSrcPath, int moduleNum) throws IOException {
        String moduleName = Helper.getModuleName(moduleNum);
        String modulePath = testSrcPath + "/" + moduleName + "/";
        String packageName = "com." + moduleName;

        File[] files = new File(modulePath).listFiles((dir, name) -> name.endsWith(".java"));

        TestClassWriter[] testClassWriters = new TestClassWriter[files.length];

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String className = file.getName().replace(".java", "");
            testClassWriters[i] = TestClassWriter.readFromFile(modulePath, className);
        }

        return new TestModuleWriter(testClassWriters, modulePath, packageName);
    }

}
