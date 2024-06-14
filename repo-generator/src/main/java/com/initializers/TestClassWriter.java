package com.initializers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.utils.Helper;

public class TestClassWriter {
    ClassOrInterfaceDeclaration classDeclaration;
    TestMethodWriter[] testMethodWriters;

    private TestClassWriter(ClassOrInterfaceDeclaration classDeclaration, TestMethodWriter[] testMethodWriters) {
        this.classDeclaration = classDeclaration;
        this.testMethodWriters = testMethodWriters;
    }

    public static TestClassWriter createTestClass(int classNum, int numOfMethods, int randomCeiling) {

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();
        classDeclaration.setName(Helper.getTestClassName(classNum));

        TestMethodWriter[] testMethodWriters = new TestMethodWriter[numOfMethods];

        for (int i = 0; i < numOfMethods; i++) {
            testMethodWriters[i] = TestMethodWriter.createTestMethod(i, randomCeiling);
            classDeclaration.addMember(testMethodWriters[i].methodDeclaration);
        }
        return new TestClassWriter(classDeclaration, testMethodWriters);
    }

    public String modifyTestClass(int methodNum, double failProb,int randomCeiling) {
        return  "." + classDeclaration.getNameAsString() + testMethodWriters[methodNum].modifyTestMethod(failProb,randomCeiling);
    }

    public void writeToFile(String modulePath, String packageName) throws IOException {

        CompilationUnit compilationUnit = new CompilationUnit();

        compilationUnit.setPackageDeclaration(packageName);
        compilationUnit.addType(classDeclaration);

        compilationUnit.addImport("org.junit.jupiter.api.Test");
        compilationUnit.addImport("org.junit.jupiter.api.Assertions.assertEquals", true, false);

        String outputFilePath = modulePath + "/" + classDeclaration.getNameAsString() + ".java";
        FileOutputStream output = new FileOutputStream(outputFilePath);
        output.write(compilationUnit.toString().getBytes());
        output.close();
    }

    public static TestClassWriter readFromFile(String modulePath, String className) throws IOException {
        String filePath = modulePath + "/" + className + ".java";
        File file = new File(filePath);

        CompilationUnit cu = StaticJavaParser.parse(new FileInputStream(file));

        ClassOrInterfaceDeclaration classDeclaration = cu.getClassByName(className)
                .orElseThrow(() -> new RuntimeException("Class not found: " + className));

        TestMethodWriter[] testMethodWriters = classDeclaration.getMethods().stream()
                .map(method -> {
                    try {
                        return TestMethodWriter.readTestMethod(method);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .toArray(TestMethodWriter[]::new);

        return new TestClassWriter(classDeclaration, testMethodWriters);
    }
}
