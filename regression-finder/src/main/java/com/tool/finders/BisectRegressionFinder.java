package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class BisectRegressionFinder extends LinearRegressionFinder {


    public BisectRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter) {
        super(projectInstance, blameWriter);
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        bisectForCommitsAndTest(projectCommits, startIndex, endIndex, endIndex, testIdentifiers);
    }

    private void bisectForCommitsAndTest(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex, int lastIndex, ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        if(testIdentifiers.isEmpty())
            return;

        if (startIndex >= endIndex - 1) {
            super.runForCommitsAndTests(projectCommits, startIndex, startIndex, testIdentifiers);
            return;
        }

        int midIndex = (startIndex + endIndex) / 2;

        ProjectCommit previousCommit = projectCommits.get(lastIndex);
        ProjectCommit currentCommit = projectCommits.get(midIndex);

        if(!projectInstance.isRunRequired(currentCommit, previousCommit)){
            if(lastIndex > midIndex)
                bisectForCommitsAndTest(projectCommits, startIndex, midIndex, midIndex, testIdentifiers);
            else
                bisectForCommitsAndTest(projectCommits, midIndex + 1, endIndex, midIndex, testIdentifiers);
            return;
        }

        ArrayList<TestResult> testResults = this.projectInstance.runTestsForCommit(testIdentifiers, projectCommits.get(midIndex),projectCommits.get(lastIndex));

        ArrayList<TestIdentifier> failedTests = new ArrayList<>(TestResult.extractFailingTests(testResults));
        ArrayList<TestIdentifier> passedTests = new ArrayList<>(TestResult.extractNotFailingTests(testResults,testIdentifiers));
        
        if(!failedTests.isEmpty())
            bisectForCommitsAndTest(projectCommits, startIndex, midIndex, midIndex, failedTests);

        if(!passedTests.isEmpty())
            bisectForCommitsAndTest(projectCommits, midIndex + 1, endIndex, midIndex, passedTests);
    }

}
