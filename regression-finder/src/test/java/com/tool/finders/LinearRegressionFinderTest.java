package com.tool.finders;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.tool.runners.ProjectInstance;
import com.tool.writers.interfaces.ItemWriter;

class LinearRegressionFinderTest {

    private ProjectInstance mockProjectInstance;
    private ItemWriter<RegressionBlame> mockBlameWriter;
    private LinearRegressionFinder finder;

    @BeforeEach
    void setUp() {
        mockProjectInstance = mock(ProjectInstance.class);
        mockBlameWriter = mock(ItemWriter.class);
        finder = new LinearRegressionFinder(mockProjectInstance, mockBlameWriter, true);
    }

    @Test
    void testRunForCommitsAndTests() throws GitAPIException, IOException {

        List<ProjectCommit> projectCommits = TestHelper.createMockCommits(6);
        List<TestIdentifier> testIdentifiers = TestHelper.createMockTestIdentifiers(5);
        ArrayList<TestResult> mockTestResults = TestHelper.createMockTestResults(5);

        when(mockProjectInstance.runTestsForCommit(any(), any(), any())).thenReturn(mockTestResults);
        when(mockProjectInstance.isRunRequired(any(), any())).thenReturn(true);

        finder.runForCommitsAndTests(new ArrayList<>(projectCommits), 0, 4, new ArrayList<>(testIdentifiers));

        ArgumentCaptor<ArrayList<RegressionBlame>> blameCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockBlameWriter, atLeastOnce()).writeAll(blameCaptor.capture());

        ArgumentCaptor<ProjectCommit> commitCaptor = ArgumentCaptor.forClass(ProjectCommit.class);
        verify(mockProjectInstance, times(5)).runTestsForCommit(any(), commitCaptor.capture(), any());
        List<ProjectCommit> capturedProjectCommits = commitCaptor.getAllValues();

        int i = 5;
        for (ProjectCommit projectCommit : capturedProjectCommits) {
            i--;
            assertEquals(projectCommit.toCSVString(), projectCommits.get(i).toCSVString());
        }
    }
}
