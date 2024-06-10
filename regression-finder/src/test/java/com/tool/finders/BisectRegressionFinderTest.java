package com.tool.finders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.ArrayList;
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

public class BisectRegressionFinderTest {
    private ProjectInstance mockProjectInstance;
    private ItemWriter<RegressionBlame> mockBlameWriter;
    private BisectRegressionFinder finder;

    @BeforeEach
    void setUp() {
        mockProjectInstance = mock(ProjectInstance.class);
        mockBlameWriter = mock(ItemWriter.class);
        finder = new BisectRegressionFinder(mockProjectInstance, mockBlameWriter);
    }

    @Test
    void testRunForCommitsAndTests() throws GitAPIException, IOException {
        List<ProjectCommit> projectCommits = TestHelper.createMockCommits(100);
        List<TestIdentifier> testIdentifiers = TestHelper.createMockTestIdentifiers(4);
        ArrayList<TestResult> mockTestResults = TestHelper.createMockTestResults(4);

        when(mockProjectInstance.runTestsForCommit(any(), any(), any())).thenReturn(mockTestResults);

        finder.runForCommitsAndTests(new ArrayList<>(projectCommits), 0, 99, new ArrayList<>(testIdentifiers));

        ArgumentCaptor<RegressionBlame> blameCaptor = ArgumentCaptor.forClass(RegressionBlame.class);
        verify(mockBlameWriter, atLeastOnce()).write(blameCaptor.capture());

        List<RegressionBlame> capturedBlames = blameCaptor.getAllValues();
        TestHelper.assertCapturedBlames(capturedBlames, projectCommits, 0, 4);

        // 0 99
        // 0 49
        // 0 24
        // 0 12
        // 0 6
        // 0 3
        // 0 1
        verify(mockProjectInstance, times(7)).runTestsForCommit(any(), any(), any());
        verify(mockBlameWriter, atLeastOnce()).write(any(RegressionBlame.class));
    }
}
