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

    private TargetProject(GitWorker gitWorker,GradleWriter gradleWriter,SubProject[] subProjects) {
        this.gitWorker = gitWorker;
        this.subProjects = subProjects;
    }

    public static TargetProject initializeProject(String rootPath,String projectName,String username,String email,String token,int numOfSubProjects,int numOfModules,int numOfClasses,int numOfMethods,int randomCeiling) throws IOException, GitAPIException{
        GitWorker gitWorker = GitWorker.mountGitWorker(rootPath, username, email, token);

        GradleWriter gradleWriter = GradleWriter.initialize(rootPath, projectName);
        
        SubProject[] subProjects = new SubProject[numOfSubProjects];
        
        for(int i=0;i<numOfSubProjects;i++){
            subProjects[i] = SubProject.createSubProject(rootPath, i, numOfModules, numOfClasses, numOfMethods, randomCeiling);
            subProjects[i].writeSubProject(gradleWriter);
        }

        return new TargetProject(gitWorker, gradleWriter,subProjects);
    }
    
    public void modifyProject(int subProjectNum,int moduleNum,int classNum,int methodNum,int x, int y) throws IOException, GitAPIException {
        subProjects[subProjectNum].modifySubProject(moduleNum, classNum, methodNum, x, y);
        String commitMessage = "Modified " + Helper.getSubProjectName(subProjectNum) + "." + Helper.getModuleName(moduleNum) + "." + Helper.getTestClassName(classNum) + "." + Helper.getTestMethodName(methodNum)+ "to x: " + x + ", y: " + y;
        gitWorker.postCommit(commitMessage);
    }

    public void pushChanges() throws InvalidRemoteException, GitAPIException{
        gitWorker.pushCommit();
    }

}
