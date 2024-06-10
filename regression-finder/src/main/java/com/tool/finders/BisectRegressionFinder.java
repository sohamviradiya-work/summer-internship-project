package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class BisectRegressionFinder extends LinearRegressionFinder {


    public BisectRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter) {
        super(projectInstance, blameWriter);
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> gitCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        bisectForCommitsAndTest(gitCommits, startIndex, endIndex, endIndex, testIdentifiers);
    }

    private void bisectForCommitsAndTest(ArrayList<ProjectCommit> gitCommits, int startIndex, int endIndex, int lastIndex, ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {

        if (startIndex >= endIndex - 1 || testIdentifiers.isEmpty()) {
            super.runForCommitsAndTests(gitCommits, startIndex, startIndex, testIdentifiers);
            return;
        }

        int midIndex = (startIndex + endIndex) / 2;

        ArrayList<TestResult> testResults = this.projectInstance.runTestsForCommit(testIdentifiers, gitCommits.get(midIndex),gitCommits.get(lastIndex));

        ArrayList<TestIdentifier> failedTests = new ArrayList<>(TestResult.extractFailingTests(testResults));
        ArrayList<TestIdentifier> passedTests = new ArrayList<>(TestResult.extractNotFailingTests(testResults,testIdentifiers));
        
        bisectForCommitsAndTest(gitCommits, startIndex, midIndex, midIndex, failedTests);
        bisectForCommitsAndTest(gitCommits, midIndex + 1, endIndex, midIndex, passedTests);
    }

}
