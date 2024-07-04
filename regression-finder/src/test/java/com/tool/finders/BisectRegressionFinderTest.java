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

public class BisectRegressionFinderTest {
    private ProjectInstance mockProjectInstance;
    private ItemWriter<RegressionBlame> mockBlameWriter;
    private BisectRegressionFinder finder;

    @BeforeEach
    void setUp() {
        mockProjectInstance = mock(ProjectInstance.class);
        mockBlameWriter = mock(ItemWriter.class);
        finder = new BisectRegressionFinder(mockProjectInstance, mockBlameWriter,true);
    }

    @Test
    void testRunForCommitsAndTests() throws GitAPIException, IOException {
        List<ProjectCommit> projectCommits = TestHelper.createMockCommits(100);
        List<TestIdentifier> testIdentifiers = TestHelper.createMockTestIdentifiers(4);
        ArrayList<TestResult> mockTestResults = TestHelper.createMockTestResults(4);

        when(mockProjectInstance.runTestsForCommit(any(), any(), any())).thenReturn(mockTestResults);
        when(mockProjectInstance.isRunRequired(any(), any())).thenReturn(true);

        finder.runForCommitsAndTests(new ArrayList<>(projectCommits), 0, 99, new ArrayList<>(testIdentifiers));

        ArgumentCaptor<ArrayList<RegressionBlame>> blameCaptor = ArgumentCaptor.forClass(ArrayList.class);
        verify(mockBlameWriter, atLeastOnce()).writeAll(blameCaptor.capture());
        
        ArgumentCaptor<ProjectCommit> commitCaptor = ArgumentCaptor.forClass(ProjectCommit.class);
        verify(mockProjectInstance, times(7)).runTestsForCommit(any(),commitCaptor.capture(), any());
        List<ProjectCommit> capturedProjectCommits = commitCaptor.getAllValues();

        int i = 99;
        
        for(ProjectCommit projectCommit:capturedProjectCommits){
            i/=2;
            assertEquals(projectCommit.toCSVString(), projectCommits.get(i).toCSVString());
        } 
    }
}
