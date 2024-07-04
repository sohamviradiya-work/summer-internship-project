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

    public ProjectInstance projectInstance;
    public ItemWriter<RegressionBlame> blameWriter;
    private boolean reportLastPhase;

    public LinearRegressionFinder(ProjectInstance projectInstance, ItemWriter<RegressionBlame> blameWriter,boolean reportLastPhase) {
        this.projectInstance = projectInstance;
        this.blameWriter = blameWriter;
        this.reportLastPhase = reportLastPhase;
    }

    @Override
    public void runForCommitsAndTests(ArrayList<ProjectCommit> projectCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException {
        ArrayList<TestIdentifier> failedTests = testIdentifiers;

        for (int i = endIndex; i >= startIndex; i--) {
            if (failedTests.isEmpty())
                break;

            ProjectCommit currentCommit = projectCommits.get(i);
            ProjectCommit previousCommit = projectCommits.get(Math.min(i + 1, projectCommits.size() - 1));

            if (currentCommit.getCommitId().compareTo(previousCommit.getCommitId()) != 0
                    && !projectInstance.isRunRequired(currentCommit, previousCommit))
                continue;

            ArrayList<TestIdentifier> newFailedTests = new ArrayList<>();

            HashSet<TestIdentifier> newFailedTestsSet = TestResult.extractFailingTests(
                    this.projectInstance.runTestsForCommit(failedTests, currentCommit, previousCommit));

            for (TestIdentifier testIdentifier : failedTests) {
                if (newFailedTestsSet.contains(testIdentifier))
                    newFailedTests.add(testIdentifier);
                else if (currentCommit.getCommitId().compareTo(previousCommit.getCommitId()) != 0)
                    projectInstance.putBlame(blameWriter, testIdentifier, previousCommit);
            }
            failedTests = newFailedTests;
        }

        if (startIndex > 0) {
            for (TestIdentifier testIdentifier : failedTests)
                projectInstance.putBlame(blameWriter, testIdentifier, projectCommits.get(startIndex));
            return;
        }
        if(!reportLastPhase)
            return;
            
        if (failedTests.size() > 0)
            putBlameOnAuthor(failedTests, projectCommits);
    }

    @Override
    public void runForTests(ArrayList<ProjectCommit> projectCommits, ArrayList<TestIdentifier> testIdentifiers)
            throws GitAPIException, IOException {
        runForCommitsAndTests(projectCommits, 0, projectCommits.size() - 1, testIdentifiers);
    }

    public void putBlameOnAuthor(ArrayList<TestIdentifier> testIdentifiers, ArrayList<ProjectCommit> projectCommits)
            throws IOException, GitAPIException {
        blameWriter.writeAll(projectInstance.blameTestsOnAuthor(testIdentifiers, projectCommits));
    }

    @Override
    public void close() throws IOException {
        this.blameWriter.close();
    }

}
