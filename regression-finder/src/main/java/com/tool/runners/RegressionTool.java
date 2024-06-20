package com.tool.runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    public static long runWithTests(String repositoryPath, String testSrcPath, String gradleVersion, String method,
            String resultPath, ArrayList<TestIdentifier> tests, String initialCommit, List<String> branches)
            throws IOException, NoHeadException, GitAPIException {

        CSVWriter<RegressionBlame> blameWriter = CSVWriter.create(resultPath + "/blame.csv");
        OutputStream logStream = new FileOutputStream(new File(resultPath + "/.log"));
        // JiraTicketWriter<RegressionBlame> blameWriter = JiraTicketWriter.create();

        ProjectInstance projectInstance = ProjectInstance.mountLocalProject(repositoryPath, testSrcPath, gradleVersion, logStream);

        Finder finder = createFinder(method, blameWriter, projectInstance);

        GitWorker gitWorker = projectInstance.getGitWorker();

        HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList = gitWorker.listCommitsByBranch(initialCommit,branches);

        long start = System.currentTimeMillis();

        String initialBranch = "";
        for (String branch : branchWiseCommitList.keySet()) {
            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);

            ProjectCommit firstCommit = projectCommits.get(0);
            ProjectCommit lastCommit = projectCommits.get(projectCommits.size() - 1);

            ArrayList<TestIdentifier> testsTorun = projectInstance.extractTestsToRun(firstCommit, lastCommit, blameWriter, tests);
            finder.runForTests(projectCommits, testsTorun);
            gitWorker.checkoutToCommit(lastCommit);
            initialBranch = branch;
        }

        long end = System.currentTimeMillis();

        projectInstance.close(initialBranch);
        finder.close();
        blameWriter.close();

        return end - start;
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
