package com.tool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.items.TestIdentifier;
import com.tool.runners.git.RepositoryCloner;

public class Config {

    public String repositoryPath;
    public String repositoryLink;
    public String method;
    public long days;
    public String resultsPath;
    public List<String> branches;
    public ArrayList<TestIdentifier> tests;
    public String testInputFile;
    public String testSrcPath;
    public String firstCommit;
    public boolean logToConsole;
    public boolean jiraTickets;
    public boolean teamsNotifications;
    public boolean reportLastPhase;

    public static final long MILLISECONDS_PER_SECOND = 1000;

    private static final String DEFAULT_CONFIG = "./config.json";

    public static final long MILLISECONDS_PER_DAY = 86400000;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";

    public Config() {
    }

    public Config(String repositoryPath, String resultsPath, String testSrcPath, String method,
            ArrayList<TestIdentifier> testIdentifiers, String firstCommit, List<String> branches, long days,
            boolean logToConsole, boolean jiraTickets, boolean teamsNotifications,boolean reportLastPhase) {
        this.repositoryPath = repositoryPath;
        this.resultsPath = resultsPath;
        this.testSrcPath = testSrcPath;
        this.tests = testIdentifiers;
        this.method = method;
        this.firstCommit = firstCommit;
        this.branches = branches;
        this.days = days;
        this.logToConsole = logToConsole;
        this.jiraTickets = jiraTickets;
        this.teamsNotifications = teamsNotifications;
        this.reportLastPhase = reportLastPhase;
    }

    public static Config mountConfig()
            throws StreamReadException, DatabindException, IOException, GitAPIException, URISyntaxException {

        final String BASE_DIRECTORY = getProjectRoot();

        FileReader jsonReader = new FileReader(BASE_DIRECTORY + DEFAULT_CONFIG);
        ObjectMapper objectMapper = new ObjectMapper();
        Config dryConfig = objectMapper.readValue(jsonReader, Config.class);

        ArrayList<TestIdentifier> testIdentifiers;

        if (dryConfig.testInputFile != null) {
            testIdentifiers = getTestInputFromFile(BASE_DIRECTORY + dryConfig.testInputFile);
        } else {
            testIdentifiers = dryConfig.tests;
            testIdentifiers.replaceAll(testIdentifier -> new TestIdentifier(":" + testIdentifier.testProject,
                    testIdentifier.testClass, testIdentifier.testMethod));
        }

        dryConfig.repositoryPath = BASE_DIRECTORY + dryConfig.repositoryPath;
        dryConfig.resultsPath = BASE_DIRECTORY + dryConfig.resultsPath;

        if (dryConfig.repositoryLink != null) {
            create(dryConfig.repositoryPath);
            clean(dryConfig.repositoryPath);
            RepositoryCloner.getRemoteRepository(dryConfig.repositoryPath, dryConfig.repositoryLink, dryConfig.days,
                    dryConfig.branches);
        }

        create(dryConfig.resultsPath);
        clean(dryConfig.resultsPath);

        return new Config(dryConfig.repositoryPath, dryConfig.resultsPath, dryConfig.testSrcPath, dryConfig.method,
                testIdentifiers, dryConfig.firstCommit, dryConfig.branches, dryConfig.days, dryConfig.logToConsole,
                dryConfig.jiraTickets, dryConfig.teamsNotifications, dryConfig.reportLastPhase);
    }

    public static String getProjectRoot() throws URISyntaxException {
        String classPath = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        File file = new File(classPath);
        final String BASE_DIRECTORY = file.getParentFile().getParentFile().getParentFile().getParentFile().getParent()
                + "/";
        return BASE_DIRECTORY;
    }

    private static ArrayList<TestIdentifier> getTestInputFromFile(String filePath) throws IOException {
        ArrayList<TestIdentifier> testIdentifiers = new ArrayList<>();
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path);

        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                String testProject = ":" + parts[0].trim();
                if (testProject.length() == 1)
                    testProject = "";
                String testClass = parts[1].trim();
                String testMethod = parts[2].trim();
                testIdentifiers.add(new TestIdentifier(testProject, testClass, testMethod));
            } else {
                System.out.println("Invalid format in line: " + line);
            }
        }
        return testIdentifiers;
    }

    private static void create(String path) throws IOException {
        Path directoryPath = Paths.get(path);
        Files.createDirectories(directoryPath);
    }

    public static void clean(String path) {
        Config.cleanDirectory(new File(path));
    }

    private static void cleanDirectory(File directory) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanDirectory(file);
                }
                file.delete();
            }
        }
    }

}
