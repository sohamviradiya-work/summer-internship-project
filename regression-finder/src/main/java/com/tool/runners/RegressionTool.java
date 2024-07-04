package com.tool.runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.tool.Config;
import com.tool.finders.BatchRegressionFinder;
import com.tool.finders.BisectRegressionFinder;
import com.tool.finders.LinearRegressionFinder;
import com.tool.finders.interfaces.Finder;
import com.tool.runners.git.GitWorker;
import com.tool.writers.CSVWriter;
import com.tool.writers.JiraTicketWriter;
import com.tool.writers.JointWriter;
import com.tool.writers.TeamsNotificationWriter;
import com.tool.writers.interfaces.ItemWriter;

public class RegressionTool {

    public static long runWithTests(Config config, String gradleVersion)
            throws IOException, NoHeadException, GitAPIException, URISyntaxException {

        OutputStream logStream = config.logToConsole ? System.out : new FileOutputStream(new File(config.resultsPath + "/.log"));
        
        JointWriter<RegressionBlame> blameWriter = JointWriter.create();
        blameWriter.addWriter(CSVWriter.create(config.resultsPath + "/blame.csv"));

        if(config.jiraTickets)
            blameWriter.addWriter(JiraTicketWriter.create());
        
        if(config.teamsNotifications)
            blameWriter.addWriter(TeamsNotificationWriter.create());

        ProjectInstance projectInstance = ProjectInstance.mountLocalProject(config.repositoryPath, config.testSrcPath,
                gradleVersion, logStream);

        Finder finder = createFinder(config.method, blameWriter, projectInstance, config.reportLastPhase);

        GitWorker gitWorker = projectInstance.getGitWorker();

        HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList = gitWorker
                .listCommitsByBranch(config.firstCommit, config.branches, config.days);

        log(config.resultsPath, branchWiseCommitList);

        long start = System.currentTimeMillis();

        String initialBranch = null;
        for (String branch : branchWiseCommitList.keySet()) {
            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);
            ProjectCommit lastCommit = projectCommits.get(projectCommits.size() - 1);

            finder.runForTests(projectCommits, config.tests);
            gitWorker.checkoutToCommit(lastCommit);
            initialBranch = branch;
        }

        if(initialBranch==null){
            System.out.println("No commits meet the criteria, fix days and first commit parameter");
            return 0;
        }


        long end = System.currentTimeMillis();

        projectInstance.close(initialBranch);
        finder.close();

        return end - start;
    }

    private static Finder createFinder(String method, ItemWriter<RegressionBlame> blameWriter,
            ProjectInstance projectInstance, boolean reportLastPhase) {
        if (method.startsWith("Linear"))
            return new LinearRegressionFinder(projectInstance, blameWriter, reportLastPhase);
        else if (method.startsWith("Bisect"))
            return new BisectRegressionFinder(projectInstance, blameWriter, reportLastPhase);
        else if (method.startsWith("Batch")) {
            int batchSize = Integer.parseInt(method.substring(6));
            return new BatchRegressionFinder(projectInstance, blameWriter, batchSize, reportLastPhase);
        } else
            throw new IllegalArgumentException("Method must be one of Linear, Bisect or Batch XX received: " + method);
    }

    private static void log(String resultPath, HashMap<String, ArrayList<ProjectCommit>> branchWiseCommitList)
            throws IOException {
        CSVWriter<ProjectCommit> commitsWriter = CSVWriter.create(resultPath + "/" + "commits.csv");
        for (String branch : branchWiseCommitList.keySet()) {
            ArrayList<ProjectCommit> projectCommits = branchWiseCommitList.get(branch);
            commitsWriter.writeAll(projectCommits);
        }
    }
}
