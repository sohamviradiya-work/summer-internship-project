package com.tool;

import com.items.GitCommit;
import com.items.RegressionBlame;
import com.tool.git.GitWorker;
import com.tool.runner.GradleWorker;
import com.tool.writers.interfaces.ItemWriter;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class TargetProjectTest {

    private GradleWorker gradleWorker;
    private GitWorker gitWorker;
    private TargetProject targetProject;
    private ItemWriter<RegressionBlame> regressionBlameWriter;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws IOException {
        gradleWorker = mock(GradleWorker.class);
        gitWorker = mock(GitWorker.class);
        regressionBlameWriter = mock(ItemWriter.class);
        targetProject = new TargetProject(gradleWorker,gitWorker);
    }

    @Test
    void testRunFailedTestsBranchWise() throws IOException, NoHeadException, GitAPIException {
        HashMap<String, ArrayList<GitCommit>> branchCommitMap = new HashMap<>();
        ArrayList<GitCommit> commits = new ArrayList<>();
        
        
        commits.add(new GitCommit("author1@example.com", "commit1", "parent1", "branch1", Date.from(Instant.now()), "Commit message 1"));
        commits.add(new GitCommit("author2@example.com", "commit2", "commit1", "branch1", Date.from(Instant.now()), "Commit message 2"));
        branchCommitMap.put("branch1", commits);

        when(gitWorker.listCommitsByBranch()).thenReturn(branchCommitMap);

        targetProject.runFailedTestsBranchWise(regressionBlameWriter);

        verify(gitWorker, times(1)).listCommitsByBranch();
        verify(gitWorker, times(2)).checkoutToCommit("commit1"); // once at the beginning, one at the end
        verify(gitWorker, times(0)).checkoutToCommit("commit2"); // not invoked for no fail
        verify(gradleWorker, times(1)).getFailingTests();
        verify(gradleWorker, times(0)).runTests(anyList(), any());
    }
}