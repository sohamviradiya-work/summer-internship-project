package com.initializers;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.VoidType;
import com.utils.Helper;

public class TestMethodWriter {
    MethodDeclaration methodDeclaration;
    
    private TestMethodWriter(MethodDeclaration methodDeclaration) {
        this.methodDeclaration = methodDeclaration;
    }

    public static TestMethodWriter writeTestMethod(int methodNum, int randomCeiling) {
        MethodDeclaration method = new MethodDeclaration();
        String methodName = Helper.getTestMethodName(methodNum);
        int x = Helper.getRandom(randomCeiling);
        int y = Helper.getRandom(randomCeiling);
        method.setName(methodName);
        method.setType(new VoidType());
        
        method.addAnnotation("Test");
        
        MethodCallExpr assertEqualsCall = new MethodCallExpr("assertEquals");
        assertEqualsCall.addArgument(String.valueOf(x));
        assertEqualsCall.addArgument(String.valueOf(y));
        
        Statement assertEqualsStatement = new ExpressionStmt(assertEqualsCall);
        
        BlockStmt body = new BlockStmt();
        body.addStatement(assertEqualsStatement);
        method.setBody(body);
        
        return new TestMethodWriter(method);
    }
    
    public void modifyTestMethod(int x, int y) {
        
        MethodCallExpr assertEqualsCall = new MethodCallExpr("assertEquals");
        
        assertEqualsCall.addArgument(String.valueOf(x));
        assertEqualsCall.addArgument(String.valueOf(y));
        
        Statement assertEqualsStatement = new ExpressionStmt(assertEqualsCall);

        methodDeclaration.getBody().ifPresent(body -> {
            body.getStatements().clear();
            body.addStatement(assertEqualsStatement);
        });
    }
}
