package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.finders.interfaces.Finder;
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class LinearRegressionFinder implements Finder {


    protected ProjectInstance projectInstance;
    private ItemWriter<RegressionBlame> blameWriter;
    private int remainingTests;

    public LinearRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter) {
        this.projectInstance = projectInstance;
        this.blameWriter = blameWriter;
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        ArrayList<TestIdentifier> failedTests = testIdentifiers;

        for (int i = endIndex; i >= startIndex; i--) {
            if(failedTests.isEmpty())
                break;
            
            ProjectCommit currentCommit = projectCommits.get(i);
            ProjectCommit previousCommit = projectCommits.get(i+1);

            ArrayList<TestIdentifier> newFailedTests = new ArrayList<>();

            HashSet<TestIdentifier> newFailedTestsSet = TestResult
                    .extractFailingTests(this.projectInstance.runTestsForCommit(failedTests, currentCommit,previousCommit));

            for (TestIdentifier testIdentifier : failedTests) {
                if (newFailedTestsSet.contains(testIdentifier))
                    newFailedTests.add(testIdentifier);
                else
                    putBlame(new RegressionBlame(testIdentifier, previousCommit));
            }
            failedTests = newFailedTests;
        }

        for(TestIdentifier testIdentifier:failedTests)
            putBlame(new RegressionBlame(testIdentifier, projectCommits.get(startIndex)));
    }

    public void putBlame(RegressionBlame regressionBlame) throws IOException {
        remainingTests--;
        blameWriter.write(regressionBlame);
        System.out.println("Remaining Tests: " + remainingTests);
    }

    @Override
    public void setTotalTests(int totalTests) {
        this.remainingTests = totalTests;
    }

    @Override
    public void close() throws IOException {
        this.blameWriter.close();
    }

}
