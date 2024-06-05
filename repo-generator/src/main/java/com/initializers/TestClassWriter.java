package com.initializers;

import java.io.FileOutputStream;
import java.io.IOException;

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

    public TestClassWriter modifyTestClass(int methodNum, int x, int y) {
        testMethodWriters[methodNum].modifyTestMethod(x, y);
        return this;
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
}
