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

class LinearRegressionFinderTest {

    private ProjectInstance mockProjectInstance;
    private ItemWriter<RegressionBlame> mockBlameWriter;
    private LinearRegressionFinder finder;

    @BeforeEach
    void setUp() {
        mockProjectInstance = mock(ProjectInstance.class);
        mockBlameWriter = mock(ItemWriter.class);
        finder = new LinearRegressionFinder(mockProjectInstance, mockBlameWriter);
    }

    @Test
    void testRunForCommitsAndTests() throws GitAPIException, IOException {

        List<ProjectCommit> projectCommits = TestHelper.createMockCommits(4);
        List<TestIdentifier> testIdentifiers = TestHelper.createMockTestIdentifiers(5);
        ArrayList<TestResult> mockTestResults = TestHelper.createMockTestResults(5);

        when(mockProjectInstance.runTestsForCommit(any(), any(), any())).thenReturn(mockTestResults);

        finder.runForCommitsAndTests(new ArrayList<>(projectCommits), 0, 2, new ArrayList<>(testIdentifiers));

        ArgumentCaptor<RegressionBlame> blameCaptor = ArgumentCaptor.forClass(RegressionBlame.class);
        verify(mockBlameWriter, atLeastOnce()).write(blameCaptor.capture());

        List<RegressionBlame> capturedBlames = blameCaptor.getAllValues();
        TestHelper.assertCapturedBlames(capturedBlames, projectCommits, 1,5);

        verify(mockProjectInstance, times(3)).runTestsForCommit(any(), any(), any()); // 2, 1, 0
        verify(mockBlameWriter, atLeastOnce()).write(any(RegressionBlame.class));
    }
}
