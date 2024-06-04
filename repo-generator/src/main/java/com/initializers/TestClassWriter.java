package com.initializers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.utils.Helper;

public class TestClassWriter {
    ClassOrInterfaceDeclaration classDeclaration;
    TestMethodWriter[] testMethodWriters;

    private TestClassWriter(ClassOrInterfaceDeclaration classDeclaration, TestMethodWriter[] testMethodWriters) {
        this.classDeclaration = classDeclaration;
        this.testMethodWriters = testMethodWriters;
    }

    public static TestClassWriter writeTestClass(String testClassName, int numOfMethods, int randomCeiling) {

        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();

        TestMethodWriter[] testMethodWriters = new TestMethodWriter[numOfMethods];

        for (int i = 0; i < numOfMethods; i++) {
            testMethodWriters[i] = TestMethodWriter.writeTestMethod(i,randomCeiling);
            classDeclaration.addMember(testMethodWriters[i].methodDeclaration);
        }
        return new TestClassWriter(classDeclaration, testMethodWriters);
    }

    public void modifyTestClass(int methodNum, int x, int y) {
        testMethodWriters[methodNum].modifyTestMethod(x, y);
    }

}
