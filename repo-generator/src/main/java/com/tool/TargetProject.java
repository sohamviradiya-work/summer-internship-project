package com.tool;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;

import com.git.GitWorker;
import com.initializers.GradleWriter;
import com.initializers.SubProject;
import com.utils.Helper;

public class TargetProject {
    

    private GitWorker gitWorker;
    private SubProject[] subProjects;
    private GradleWriter gradleWriter;
    private String rootPath;

    private TargetProject(GitWorker gitWorker,GradleWriter gradleWriter,String rootPath) {
        this.gitWorker = gitWorker;
        this.gradleWriter = gradleWriter;
        this.rootPath = rootPath;
        this.subProjects = new SubProject[0];
    }

    public static TargetProject initializeProject(String rootPath,String projectName,String username,String email,String token) throws IOException, GitAPIException{
        GitWorker gitWorker = GitWorker.mountGitWorker(rootPath, username, email, token);

        GradleWriter gradleWriter = GradleWriter.initialize(rootPath, projectName);
        
        gitWorker.postCommit("init project");

        return new TargetProject(gitWorker, gradleWriter,rootPath);
    }

    public void populate(int numOfSubProjects,int numOfModules,int numOfClasses,int numOfMethods,int randomCeiling) throws IOException {
        gradleWriter.populate();
        subProjects = new SubProject[numOfSubProjects];
        for(int i=0;i<numOfSubProjects;i++){
            subProjects[i] = SubProject.createSubProject(rootPath, i, numOfModules, numOfClasses, numOfMethods, randomCeiling);
            subProjects[i].writeSubProject(gradleWriter);
        }
    }
    
    public void modifyProject(int subProjectNum,int moduleNum,int classNum,int methodNum,int x, int y) throws IOException, GitAPIException {
        subProjects[subProjectNum].modifySubProject(moduleNum, classNum, methodNum, x, y);
        String commitMessage = "Modified " + Helper.getSubProjectName(subProjectNum) + "." + Helper.getModuleName(moduleNum) + "." + Helper.getTestClassName(classNum) + "." + Helper.getTestMethodName(methodNum)+ " to x: " + x + ", y: " + y;
        gitWorker.postCommit(commitMessage);
    }

    public void pushChanges() throws InvalidRemoteException, GitAPIException{
        gitWorker.pushCommit();
    }

}
