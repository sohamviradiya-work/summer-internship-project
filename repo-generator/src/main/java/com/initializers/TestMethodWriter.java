package com.initializers;

import java.io.IOException;

import java.util.Optional;
import java.util.List;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.VoidType;
import com.utils.Helper;

public class TestMethodWriter {
    MethodDeclaration methodDeclaration;
    private int x;
    private int y;

    private TestMethodWriter(MethodDeclaration methodDeclaration, int x, int y) {
        this.methodDeclaration = methodDeclaration;
        this.x = x;
        this.y = y;
    }

    public static TestMethodWriter createTestMethod(int methodNum, int randomCeiling) {
        MethodDeclaration method = new MethodDeclaration();
        String methodName = Helper.getTestMethodName(methodNum);
        int x = Helper.getRandom(randomCeiling);
        int y = x;
        method.setName(methodName);
        method.setType(new VoidType());

        method.addMarkerAnnotation("Test");

        MethodCallExpr assertEqualsCall = new MethodCallExpr("assertEquals");
        assertEqualsCall.addArgument(String.valueOf(x));
        assertEqualsCall.addArgument(String.valueOf(y));

        Statement assertEqualsStatement = new ExpressionStmt(assertEqualsCall);

        BlockStmt body = new BlockStmt();
        body.addStatement(assertEqualsStatement);
        method.setBody(body);

        return new TestMethodWriter(method, x, y);
    }

    public static TestMethodWriter readTestMethod(BodyDeclaration<MethodDeclaration> bodyDeclaration)
            throws IOException {
        MethodDeclaration method = bodyDeclaration.asMethodDeclaration();
        int x = 0;
        int y = 0;
        Optional<BlockStmt> optionalBody = method.getBody();
        if (!optionalBody.isPresent())
            return null;
        BlockStmt body = optionalBody.get();
        List<Statement> statements = body.getStatements();

        for (Statement statement : statements) {
            if (!(statement instanceof ExpressionStmt))
                continue;
            ExpressionStmt expressionStmt = (ExpressionStmt) statement;
            Expression expression = expressionStmt.getExpression();

            if (!(expression instanceof MethodCallExpr))
                continue;

            MethodCallExpr methodCallExpr = (MethodCallExpr) expression;

            if (!("assertEquals".equals(methodCallExpr.getNameAsString())))
                continue;
            NodeList<Expression> arguments = methodCallExpr.getArguments();

            if (arguments.size() != 2)
                break;
            Expression arg1 = arguments.get(0);
            Expression arg2 = arguments.get(1);
            
            if (arg1.isIntegerLiteralExpr() && arg2.isIntegerLiteralExpr()) {
                x = Integer.parseInt(arg1.asIntegerLiteralExpr().getValue());
                y = Integer.parseInt(arg2.asIntegerLiteralExpr().getValue());
            }

            break;
        }

        return new TestMethodWriter(bodyDeclaration.asMethodDeclaration(), x, y);
    }


    private void setXY(int x,int y) {
        this.x = x;
        this.y = y;
    }

    public String modifyTestMethod(double failProb, int randomCeiling) {

        MethodCallExpr assertEqualsCall = new MethodCallExpr("assertEquals");
        int x = 0;
        int y = 0;
        if (this.x == this.y) {
            x = Helper.getRandom(randomCeiling);
            if (Math.random() < failProb)
                y = Helper.getRandom(randomCeiling, x);
            else
                y = x;
        } else {
            x = Helper.getRandom(randomCeiling);
            y = Helper.getRandom(randomCeiling, x);
        }

        assertEqualsCall.addArgument(String.valueOf(x));
        assertEqualsCall.addArgument(String.valueOf(y));

        Statement assertEqualsStatement = new ExpressionStmt(assertEqualsCall);

        methodDeclaration.getBody().ifPresent(body -> {
            body.getStatements().clear();
            body.addStatement(assertEqualsStatement);
        });

        String message = "." + methodDeclaration.getNameAsString() + " from x: " + this.x + ", y: " + this.y + " to x: "
                + x + ", y: " + y;

        setXY(x, y);
        
        return message;
    }

}
