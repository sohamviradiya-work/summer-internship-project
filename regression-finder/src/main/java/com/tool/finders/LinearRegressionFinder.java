package com.tool.finders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.bouncycastle.util.test.Test;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.Config;
import com.tool.finders.interfaces.Finder;
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

public class LinearRegressionFinder implements Finder {

    protected ProjectInstance projectInstance;
    private ItemWriter<RegressionBlame> blameWriter;

    public LinearRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter) {
        this.projectInstance = projectInstance;
        this.blameWriter = blameWriter;
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        ArrayList<TestIdentifier> failedTests = testIdentifiers;

        for (int i = endIndex; i >= startIndex; i--) {
            if (failedTests.isEmpty())
                break;

            ProjectCommit currentCommit = projectCommits.get(i);
            ProjectCommit previousCommit = projectCommits.get(i + 1);

            if (!projectInstance.isRunRequired(currentCommit, previousCommit))
                continue;

            ArrayList<TestIdentifier> newFailedTests = new ArrayList<>();

            HashSet<TestIdentifier> newFailedTestsSet = TestResult.extractFailingTests(
                    this.projectInstance.runTestsForCommit(failedTests, currentCommit, previousCommit));

            for (TestIdentifier testIdentifier : failedTests) {
                if (newFailedTestsSet.contains(testIdentifier))
                    newFailedTests.add(testIdentifier);
                else
                    putBlame(testIdentifier, previousCommit, true);
            }
            failedTests = newFailedTests;
        }

        for (TestIdentifier testIdentifier : failedTests) {
            putBlame(testIdentifier, projectCommits.get(startIndex), (startIndex > 0));

        }
    }

    @Override
    public void runForTests(ArrayList<ProjectCommit> projectCommits, ArrayList<TestIdentifier> testIdentifiers)
            throws GitAPIException, IOException {
        runForCommitsAndTests(projectCommits, 0, projectCommits.size() - 2, testIdentifiers);
    }

    public void putBlame(TestIdentifier testIdentifier, ProjectCommit projectCommit, boolean isTestFail)
            throws IOException, GitAPIException {
        if (!isTestFail)
            blameWriter.writeAll(projectInstance.blameTestOnAuthor(testIdentifier, projectCommit));
        else
            blameWriter.write(RegressionBlame.constructBlame(testIdentifier, projectCommit, true));
    }

    @Override
    public void close() throws IOException {
        this.blameWriter.close();
    }

}
