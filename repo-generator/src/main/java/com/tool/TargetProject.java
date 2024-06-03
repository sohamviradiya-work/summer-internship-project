package com.tool;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.git.GitWorker;
import com.initializers.GradleWriter;

public class TargetProject {
    

    private GitWorker gitWorker;
    private GradleWriter gradleWriter;

    private TargetProject(GitWorker gitWorker,GradleWriter gradleWriter) {
        this.gitWorker = gitWorker;
        this.gradleWriter = gradleWriter;
    }

    public static TargetProject mountProject(String path,String projectName,String username,String email,String token) throws IOException, GitAPIException{
        GitWorker gitWorker = GitWorker.mountGitWorker(path, username, email, token);
        GradleWriter gradleWriter = GradleWriter.initialize(path, projectName);
        return new TargetProject(gitWorker, gradleWriter);
    } 

}
