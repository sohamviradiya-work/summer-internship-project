package com.tool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.finders.BatchRegressionFinder;
import com.tool.finders.BisectRegressionFinder;
import com.tool.finders.LinearRegressionFinder;
import com.tool.finders.interfaces.Finder;
import com.tool.git.GitWorker;
import com.tool.writers.CSVWriter;

public class RegressionTool {

    public static void run(String path, String gradleVersion, String method, String resultPath)
            throws IOException, NoHeadException, GitAPIException {
        ProjectInstance projectInstance = ProjectInstance.mountLocalProject(path, gradleVersion);

        GitWorker gitWorker = projectInstance.getGitWorker();

        HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList = gitWorker.listCommitsByBranch();

        Finder finder;

        CSVWriter<RegressionBlame> blameWriter = CSVWriter.create(resultPath + "/" + method + ".csv");
        CSVWriter<ProjectCommit> commitsWriter = CSVWriter.create(resultPath + "/" + "commits.csv");

        if (method == "Linear")
            finder = new LinearRegressionFinder(projectInstance, blameWriter);
        else if (method == "Bisect")
            finder = new BisectRegressionFinder(projectInstance, blameWriter);
        else if (method.startsWith("Batch")) {
            int batchSize = Integer.parseInt(method.substring(6));
            finder = new BatchRegressionFinder(projectInstance, blameWriter, batchSize);
        } else
            throw new IllegalArgumentException("Method must be one of Linear, Bisect or Batch XX");

        for (String branch : branchWiseCommitList.keySet()) {

            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);
            commitsWriter.writeAll(projectCommits);

            ArrayList<TestResult> testResults = projectInstance.runAllTestsForCommit(projectCommits.get(0));
            ArrayList<TestIdentifier> failingTests = new ArrayList<>(TestResult.extractFailingTests(testResults));
            Collections.reverse(projectCommits);
            finder.runForCommitsAndTests(projectCommits, 0, projectCommits.size() - 2, failingTests);
        }
        projectInstance.close();
        finder.close();
        commitsWriter.close();
    }
}
