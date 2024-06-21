package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class BatchRegressionFinder extends LinearRegressionFinder {

    private int batchSize;

    public BatchRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter,
            int batchSize) {
        super(projectInstance, blameWriter);
        this.batchSize = batchSize;
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {

        ArrayList<TestIdentifier> failedTests = testIdentifiers;
        int i = endIndex;

        for (; i >= startIndex; i -= batchSize) {

            if (failedTests.isEmpty())
                break;

            int lastIndex = Math.min(i + batchSize, endIndex);

            if (!projectInstance.isRunRequired(projectCommits.get(i), projectCommits.get(lastIndex)))
                continue;

            ArrayList<TestIdentifier> newFailedTests = new ArrayList<>();
            ArrayList<TestIdentifier> batchTests = new ArrayList<>();

            ArrayList<TestResult> testResults = projectInstance.runTestsForCommit(failedTests, projectCommits.get(i),
                    projectCommits.get(lastIndex));

            HashSet<TestIdentifier> newFailedTestsSet = TestResult.extractFailingTests(testResults);

            for (TestIdentifier test : failedTests) {
                if (newFailedTestsSet.contains(test))
                    newFailedTests.add(test);
                else
                    batchTests.add(test);
            }

            super.runForCommitsAndTests(projectCommits, i + 1, Math.min(i + batchSize - 1, endIndex), batchTests);
            failedTests = newFailedTests;
        }
        if(!failedTests.isEmpty())
            super.runForCommitsAndTests(projectCommits, startIndex, Math.min(i + batchSize - 1, endIndex), failedTests);
    }


    @Override
    public void runForTests(ArrayList<ProjectCommit> projectCommits, ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        runForCommitsAndTests(projectCommits,0,projectCommits.size() - 1,testIdentifiers);
    }
}
