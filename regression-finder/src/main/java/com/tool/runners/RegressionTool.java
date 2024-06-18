package com.tool.runners;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.tool.finders.BatchRegressionFinder;
import com.tool.finders.BisectRegressionFinder;
import com.tool.finders.LinearRegressionFinder;
import com.tool.finders.interfaces.Finder;
import com.tool.runners.git.GitWorker;
import com.tool.writers.CSVWriter;
import com.tool.writers.JiraTicketWriter;
import com.tool.writers.interfaces.ItemWriter;

public class RegressionTool {

    public static long run(String repositoryPath, String testSrcPath, String gradleVersion, String method,
            String resultPath, boolean logCommits)
            throws IOException, NoHeadException, GitAPIException {

        CSVWriter<RegressionBlame> blameWriter = CSVWriter.create(resultPath + "/blame.csv");
        // JiraTicketWriter<RegressionBlame> blameWriter = JiraTicketWriter.create();

        ProjectInstance projectInstance = ProjectInstance.mountLocalProject(repositoryPath, testSrcPath, gradleVersion);

        Finder finder = createFinder(method, blameWriter, projectInstance);

        GitWorker gitWorker = projectInstance.getGitWorker();

        HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList = gitWorker.listCommitsByBranch();

        if (logCommits)
            log(resultPath, branchWiseCommitList);

        long start = System.currentTimeMillis();

        for (String branch : branchWiseCommitList.keySet()) {
            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);

            ProjectCommit firstCommit = projectCommits.get(0);
            ProjectCommit lastCommit = projectCommits.get(projectCommits.size() - 1);
            ArrayList<TestIdentifier> testsTorun = projectInstance.extractTestsToRun(firstCommit, lastCommit,blameWriter);

            finder.runForTests(projectCommits, testsTorun);
        }

        long end = System.currentTimeMillis();

        projectInstance.close();
        finder.close();
        blameWriter.close();

        return end - start;
    }

    private static void log(String resultPath, HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList)
            throws IOException {
        CSVWriter<ProjectCommit> commitsWriter = CSVWriter.create(resultPath + "/" + "commits.csv");
        for (String branch : branchWiseCommitList.keySet()) {
            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);
            commitsWriter.writeAll(projectCommits);
        }
    }

    private static Finder createFinder(String method, ItemWriter<RegressionBlame> blameWriter,
            ProjectInstance projectInstance) {
        if (method.startsWith("Linear"))
            return new LinearRegressionFinder(projectInstance, blameWriter);
        else if (method.startsWith("Bisect"))
            return new BisectRegressionFinder(projectInstance, blameWriter);
        else if (method.startsWith("Batch")) {
            int batchSize = Integer.parseInt(method.substring(6));
            return new BatchRegressionFinder(projectInstance, blameWriter, batchSize);
        } else
            throw new IllegalArgumentException("Method must be one of Linear, Bisect or Batch XX received: " + method);
    }
}
