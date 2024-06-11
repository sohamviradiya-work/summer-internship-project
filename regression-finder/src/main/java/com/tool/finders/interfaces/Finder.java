package com.tool.finders.interfaces;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.TestIdentifier;

public interface Finder {
    void runForCommitsAndTests(ArrayList<ProjectCommit> gitCommits, int startIndex, int endIndex,
            ArrayList<TestIdentifier> testIdentifiers) throws GitAPIException, IOException;

    void close() throws IOException;

    void setTotalTests(int totalTests);
}