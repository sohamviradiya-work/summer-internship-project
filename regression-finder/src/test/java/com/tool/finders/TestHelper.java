package com.tool.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;

public class TestHelper {

    static ProjectCommit createMockCommit(int i) {
        return new ProjectCommit("test@gmail.com", "abc" + i, "main", Date.from(Instant.now()),"Commit message " + i);
    }

    static List<ProjectCommit> createMockCommits(int num) {
        List<ProjectCommit> commits = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            commits.add(createMockCommit(i));
        }
        return commits;
    }

    static TestIdentifier createMockIdentifier(int i) {
        return new TestIdentifier(":project" + i,"TestClass" + i, "testMethod" + i);
    }

    static TestResult createMockTestResult(int i) {
        return new TestResult("TestClass" + i, "testMethod" + i, ":project" + i, "FAILED");
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
            testResults.add(createMockTestResult(i));
        }
        return testResults;
    }

    static void assertCapturedBlames(List<RegressionBlame> capturedBlames, List<ProjectCommit> projectCommits,
            int regressionCommitIndex,int num) {
        HashSet<String> expectedBlames = new HashSet<>();
        for (int i = 0; i < num; i++) {
            expectedBlames.add((RegressionBlame.constructBlame(createMockIdentifier(i), projectCommits.get(regressionCommitIndex),true)).toCSVString());
        }
    
        assertEquals(expectedBlames.size(), capturedBlames.size(),
                "The number of captured blames should match the expected count.");
        
        for (int i = 0; i < capturedBlames.size(); i++) {
            String capturedBlame = capturedBlames.get(i).toCSVString();
            assertTrue(expectedBlames.contains(capturedBlame));
        }
    }
    
}
