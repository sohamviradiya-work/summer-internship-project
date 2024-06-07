package com.tool.finders.interfaces;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.items.ProjectCommit;
import com.items.TestIdentifier;

public interface RegressionFinder {
    void runForCommitsAndTests(ArrayList<ProjectCommit> gitCommits, ArrayList<TestIdentifier> testIdentifiers,ProjectCommit lastCommit) throws GitAPIException, IOException;
    void close() throws IOException;
}