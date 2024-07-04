package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class BisectRegressionFinder extends LinearRegressionFinder {

    public BisectRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter, boolean reportLastPhase) {
        super(projectInstance, blameWriter, reportLastPhase);
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        bisectForCommitsAndTest(projectCommits, startIndex, endIndex, endIndex, testIdentifiers, true);
    }

    private void bisectForCommitsAndTest(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            int lastIndex, ArrayList<TestIdentifier> testIdentifiers, boolean left)
            throws GitAPIException, IOException {

        if (testIdentifiers.isEmpty())
            return;

        if (startIndex >= endIndex - 1) {
            if (startIndex == endIndex && endIndex != projectCommits.size() - 1) {
                for (TestIdentifier testIdentifier : testIdentifiers) {
                    projectInstance.putBlame(blameWriter, testIdentifier, projectCommits.get(startIndex));
                }
            } else
                super.runForCommitsAndTests(projectCommits, startIndex, startIndex, testIdentifiers);
            return;
        }

        int midIndex = (startIndex + endIndex) / 2;

        ProjectCommit previousCommit = projectCommits.get(lastIndex);
        ProjectCommit currentCommit = projectCommits.get(midIndex);

        if (!projectInstance.isRunRequired(currentCommit, previousCommit)) {
            if (left)
                bisectForCommitsAndTest(projectCommits, startIndex, midIndex, midIndex, testIdentifiers, true);
            else
                bisectForCommitsAndTest(projectCommits, midIndex + 1, endIndex, midIndex, testIdentifiers, false);
            return;
        }

        List<TestResult> testResults = this.projectInstance.runTestsForCommit(testIdentifiers,
                projectCommits.get(midIndex), projectCommits.get(lastIndex));

        ArrayList<TestIdentifier> failedTests = new ArrayList<>(TestResult.extractFailingTests(testResults));
        ArrayList<TestIdentifier> passedTests = new ArrayList<>(
                TestResult.extractNotFailingTests(testResults, testIdentifiers));

        if (!failedTests.isEmpty())
            bisectForCommitsAndTest(projectCommits, startIndex, midIndex, midIndex, failedTests, true);

        if (!passedTests.isEmpty())
            bisectForCommitsAndTest(projectCommits, midIndex + 1, endIndex, midIndex, passedTests, false);
    }
}
