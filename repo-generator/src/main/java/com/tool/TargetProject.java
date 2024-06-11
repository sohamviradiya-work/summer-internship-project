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
    private ArrayList<SubProject> subProjects;
    private GradleWriter gradleWriter;
    private String rootPath;

    private TargetProject(GitWorker gitWorker,GradleWriter gradleWriter,String rootPath) {
        this.gitWorker = gitWorker;
        this.gradleWriter = gradleWriter;
        this.rootPath = rootPath;
        this.subProjects = new ArrayList<>();
    }

    public int getNumOfSubprojects(){
        return subProjects.size();
    }

    public static TargetProject initializeProject(String rootPath,String projectName,String username,String email,String token) throws IOException, GitAPIException{
        GitWorker gitWorker = GitWorker.mountGitWorker(rootPath, username, email, token);
        gitWorker.resetRepository();
        GradleWriter gradleWriter = GradleWriter.initialize(rootPath, projectName);
        
        return new TargetProject(gitWorker, gradleWriter,rootPath);
    }

    public void populate() throws  GitAPIException, IOException {
        gradleWriter.populate();
        gitWorker.postCommit("init project");
    }

    public void addSubProject(int numOfModules,int numOfClasses,int numOfMethods,int randomCeiling) throws IOException, GitAPIException{
            int index = subProjects.size();
            SubProject subProject = SubProject.createSubProject(rootPath, index, numOfModules, numOfClasses, numOfMethods, randomCeiling);
            subProject.writeSubProject(gradleWriter);
            subProjects.add(subProject);
            gitWorker.postCommit("added sub project "+ index);
    }
    
    public void modifyProject(int subProjectNum,int moduleNum,int classNum,int methodNum,int x, int y) throws IOException, GitAPIException {
        subProjects.get(subProjectNum).modifySubProject(moduleNum, classNum, methodNum, x, y);
        String commitMessage = "Modified " + Helper.getSubProjectName(subProjectNum) + "." + Helper.getModuleName(moduleNum) + "." + Helper.getTestClassName(classNum) + "." + Helper.getTestMethodName(methodNum)+ " to x: " + x + ", y: " + y;
        gitWorker.postCommit(commitMessage);
    }

    public void pushChanges() throws InvalidRemoteException, GitAPIException{
        gitWorker.pushCommit();
    }

}
