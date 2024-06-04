package com.initializers;

import java.io.IOException;
import java.nio.file.*;

import java.util.List;

public class GradleWriter {

    private Path rootPath;
    private String projectName;

    private GradleWriter(Path rootPath, String projectName) {
        this.rootPath = rootPath;
        this.projectName = projectName;
    }

    public static GradleWriter initialize(String rootPath, String projectName) throws IOException {
        GradleWriter gradleWriter = new GradleWriter(Path.of(rootPath), projectName);
        return gradleWriter;
    }

    public void populate() throws IOException {
        Files.createDirectories(rootPath);
        createRootBuildGradle();
        createSettingsGradle(projectName);
    }

    public void initSubProjectGradle(String subProjectName) throws IOException {
        this.addSubProjectToSettings(subProjectName);
        this.createSubProjectBuildGradle(subProjectName);
    }

    private void createRootBuildGradle() throws IOException {
        List<String> lines = List.of(
                "plugins {",
                "    id 'java'",
                "}",
                "",
                "subprojects {",
                "    apply plugin: 'java'",
                "}",
                "");
        Files.write(rootPath.resolve("build.gradle"), lines, StandardOpenOption.CREATE);
    }

    private void createSettingsGradle(String projectName) throws IOException {
        String settingsContent = "rootProject.name = '" + projectName + "';\n";
        Files.write(rootPath.resolve("settings.gradle"), settingsContent.getBytes(), StandardOpenOption.CREATE);
    }

    public void addSubProjectToSettings(String subProjectName) throws IOException {
        String settingsLine = "include '" + subProjectName + "'\n";
        Files.write(rootPath.resolve("settings.gradle"), settingsLine.getBytes(), StandardOpenOption.APPEND);
    }

    public void createSubProjectBuildGradle(String subProjectName) throws IOException {
        Path subProjectPath = Path.of(rootPath.toString(), subProjectName);

        List<String> lines = List.of(
                "plugins {",
                "    id 'java'",
                "}",
                "",
                "repositories {",
                "mavenCentral()",
                "}",
                "",
                "dependencies {",
                "    testImplementation platform('org.junit:junit-bom:5.10.0')",
                "    testImplementation 'org.junit.jupiter:junit-jupiter'",
                "}",
                "",
                "test {",
                "    useJUnitPlatform()",
                "}",
                "");
        Files.write(subProjectPath.resolve("build.gradle"), lines, StandardOpenOption.CREATE);
    }
}
