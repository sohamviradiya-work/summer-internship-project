package com.initializers;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.utils.Helper;

public class TestClassWriter {
    ClassOrInterfaceDeclaration classDeclaration;
    TestMethodWriter[] testMethodWriters;
     
    private TestClassWriter(ClassOrInterfaceDeclaration classDeclaration,TestMethodWriter[] testMethodWriters) {
        this.classDeclaration = classDeclaration;
        this.testMethodWriters = testMethodWriters;
    }

    public static TestClassWriter writeTestClass(String testClassName,int[] x, int[] y) {
        
        ClassOrInterfaceDeclaration classDeclaration = new ClassOrInterfaceDeclaration();

        TestMethodWriter[] testMethodWriters = new TestMethodWriter[x.length];

        for (int i = 0; i < x.length; i++) {
            testMethodWriters[i] = TestMethodWriter.writeTestMethod(Helper.getTestMethodName(i), x[i],y[i]);
            classDeclaration.addMember(testMethodWriters[i].methodDeclaration);
        }
        
        return new TestClassWriter(classDeclaration, testMethodWriters);
    }

    public void modifyTestClass(int methodNum,int x, int y) {
        testMethodWriters[methodNum].modifyTestMethod(x, y);
    }

}
