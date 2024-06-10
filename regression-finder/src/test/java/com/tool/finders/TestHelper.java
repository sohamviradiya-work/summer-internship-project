package com.tool.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;

public class TestHelper {

    static ProjectCommit createMockCommit(int i) {
        return new ProjectCommit("test@gmail.com", "abc", "main", Date.from(Instant.now()),"Commit message" + i);
    }

    static List<ProjectCommit> createMockCommits(int num) {
        List<ProjectCommit> commits = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            commits.add(createMockCommit(i));
        }
        return commits;
    }

    static TestIdentifier createMockIdentifier(int i) {
        return new TestIdentifier("project" + i,"TestClass" + i, "testMethod" + i);
    }

    static TestResult createMockTest(int i) {
        return new TestResult("TestClass" + i, "testMethod" + i, "project" + i, "FAILED");
    }

    static List<TestIdentifier> createMockTestIdentifiers(int num) {
        List<TestIdentifier> testIdentifiers = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            testIdentifiers.add(createMockIdentifier(i));
        }
        return testIdentifiers;
    }

    static ArrayList<TestResult> createMockTestResults(int num) {
        ArrayList<TestResult> testResults = new ArrayList<>();
        
        for (int i = 0; i < num; i++) {
            testResults.add(createMockTest(i));
        }
        return testResults;
    }

    static void assertCapturedBlames(List<RegressionBlame> capturedBlames, List<ProjectCommit> projectCommits,
            int regressionCommitIndex,int num) {
        List<RegressionBlame> expectedBlames = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            expectedBlames.add(
                    new RegressionBlame(createMockIdentifier(i), projectCommits.get(regressionCommitIndex)));
        }
    
        assertEquals(expectedBlames.size(), capturedBlames.size(),
                "The number of captured blames should match the expected count.");
    
        for (int i = 0; i < expectedBlames.size(); i++) {
            RegressionBlame expectedBlame = expectedBlames.get(i);
            RegressionBlame capturedBlame = capturedBlames.get(i);
    
            assertEquals(expectedBlame.toCSVString(), capturedBlame.toCSVString(), "CSV string should match.");
        }
    }
    
}
