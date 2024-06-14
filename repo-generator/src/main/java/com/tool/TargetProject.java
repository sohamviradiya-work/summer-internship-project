package com.tool;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;

import com.git.GitWorker;
import com.initializers.GradleWriter;
import com.initializers.SubProject;

public class TargetProject {

    private GitWorker gitWorker;
    private ArrayList<SubProject> subProjects;
    private GradleWriter gradleWriter;
    private String rootPath;

    private TargetProject(GitWorker gitWorker, GradleWriter gradleWriter, String rootPath) {
        this.gitWorker = gitWorker;
        this.gradleWriter = gradleWriter;
        this.rootPath = rootPath;
        this.subProjects = new ArrayList<>();
    }

    public int getNumOfSubprojects() {
        return subProjects.size();
    }

    public void populate() throws GitAPIException, IOException {
        gradleWriter.populate();
        gitWorker.postCommit("init project");
    }

    public void read() throws GitAPIException, IOException {
        File rootDirectory = new File(rootPath);

        File[] subProjectDirs = rootDirectory
                .listFiles(file -> file.isDirectory() && file.getName().startsWith("project"));

        for (File subProjectDir : subProjectDirs) {
            String subProjectName = subProjectDir.getName();
            int subProjectNum = Integer.parseInt(subProjectName.substring(7));
            SubProject subProject = SubProject.readSubProject(rootPath, subProjectNum);
            subProjects.add(subProject);
        }
    }

    public void addSubProject(int numOfModules, int numOfClasses, int numOfMethods, int randomCeiling)
            throws IOException, GitAPIException {
        int index = subProjects.size();
        SubProject subProject = SubProject.createSubProject(rootPath, index, numOfModules, numOfClasses, numOfMethods,
                randomCeiling);
        subProject.writeSubProject(gradleWriter);
        subProjects.add(subProject);
        gitWorker.postCommit("added sub project " + index);
    }

    public void modifyProject(int subProjectNum, int moduleNum, int classNum, int methodNum,double failProb,int randomCeiling)
            throws IOException, GitAPIException {
        String commitMessage = "Modification: " + subProjects.get(subProjectNum).modifySubProject(moduleNum, classNum, methodNum, failProb,randomCeiling);
        gitWorker.postCommit(commitMessage);
    }

    public void pushChanges() throws InvalidRemoteException, GitAPIException {
        gitWorker.pushCommit();
    }

    public static TargetProject initializeProject(String rootPath, String projectName, String username, String email,
            String token, String remote, Boolean reset) throws IOException, GitAPIException, URISyntaxException {

        setUpDirectory(rootPath);
        GitWorker gitWorker = reset == true ? GitWorker.mountNewGitWorker(rootPath, username, email, token, remote)
                : GitWorker.cloneNewGitWorker(rootPath, username, email, token, remote);

        GradleWriter gradleWriter = GradleWriter.initialize(rootPath, projectName);

        TargetProject targetProject = new TargetProject(gitWorker, gradleWriter, rootPath);

        if (reset)
            targetProject.populate();
        else
            targetProject.read();

        return targetProject;
    }

    private static void setUpDirectory(String rootPath) throws IOException {
        File directory = new File(rootPath);
        if (directory.exists()) {
            deleteDirectory(directory);
        }
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    private static void deleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (!directory.delete()) {
            throw new IOException("Failed to delete file or directory: " + directory.getAbsolutePath());
        }
    }
}
