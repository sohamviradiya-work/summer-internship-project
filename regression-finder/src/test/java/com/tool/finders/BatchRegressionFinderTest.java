package com.tool.finders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.items.ProjectCommit;
import com.items.RegressionBlame;
import com.items.TestIdentifier;
import com.items.TestResult;
import com.tool.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

class BatchRegressionFinderTest {

    private ProjectInstance mockProjectInstance;
    private ItemWriter<RegressionBlame> mockBlameWriter;
    private BatchRegressionFinder finder;

    @BeforeEach
    void setUp() {
        mockProjectInstance = mock(ProjectInstance.class);
        mockBlameWriter = mock(ItemWriter.class);
        finder = new BatchRegressionFinder(mockProjectInstance, mockBlameWriter, 3); 
    }

    @Test
    void testRunForCommitsAndTests() throws GitAPIException, IOException {
        List<ProjectCommit> projectCommits = TestHelper.createMockCommits(12);
        List<TestIdentifier> testIdentifiers = TestHelper.createMockTestIdentifiers(4);
        ArrayList<TestResult> mockTestResults = TestHelper.createMockTestResults(4);

        when(mockProjectInstance.runTestsForCommit(any(), any(), any())).thenReturn(mockTestResults);

        finder.runForCommitsAndTests(new ArrayList<>(projectCommits), 0, 11, new ArrayList<>(testIdentifiers));

        ArgumentCaptor<RegressionBlame> blameCaptor = ArgumentCaptor.forClass(RegressionBlame.class);
        verify(mockBlameWriter, atLeastOnce()).write(blameCaptor.capture());

        List<RegressionBlame> capturedBlames = blameCaptor.getAllValues();
        TestHelper.assertCapturedBlames(capturedBlames, projectCommits,3,4);

        verify(mockProjectInstance, times(6)).runTestsForCommit(any(), any(), any()); // 11, 8, 5, 2, 1, 0
        verify(mockBlameWriter, atLeastOnce()).write(any(RegressionBlame.class));
    }

}
