# Project Documentation: Regression Finder Tool


## Project Description
The Regression Finder Tool is designed to identify the specific commit that caused a regression in a codebase. It takes a list of failed tests from a suite and maps each failure to the corresponding commit and author responsible. The tool efficiently handles large repositories and exports the results to an Excel file.

## Features
- **Input Handling**: Accepts a list of failed tests in a suite.
- **Data Export**: Exports details of unit tests, commits, and authors to an Excel file.
- **Performance Optimization**: Optimized to work efficiently with large repositories.
- **Result Storage**: Stores the results in a CSV file.
- **Future Enhancements**:
  - Integration with Microsoft Teams for alert notifications.
  - Integration with Jira API for issue tracking and management.

## Requirements
- **OpenJDK**: Ensure `openjdk@17` is installed.
- **Environment Variable**: Set `JAVA_HOME` to the OpenJDK installation path.
- **Gradle**: Ensure Gradle is installed and properly configured.

## Installation and Setup

### 1. Cloning the Repository:  
   To start using the Regression Finder Tool, clone the repository from GitHub:
   ```sh
   git clone https://github.com/sohamviradiya-work/summer-internship-project
   ```

### 2. Setting Up the Environment:  
   Navigate to the project directory and set up the Gradle wrapper:
   ```sh
   cd summer-internship-project
   ```
   ```sh
   gradle wrapper
   ```

### 3. Instructions for Writing the JSON Configuration:    

To create the JSON configuration file for the project, follow the structure and instructions below. Each key-value pair is explained for clarity:

```json
{
    "repositoryLink": "git@github.com:sohamviradiya-work/small-test-repo.git", 
    "repositoryPath": "./resources/repository", 
    "branches": ["branch1"],  
    "days": 20,  
    "firstCommit": "6eb82d29421fba8e8df76d06d7517a9152fd8690", 
    "testInputFile": "./tests.csv", 
    "testSrcPath": "/src/test/java/", 
    "tests": [ 
        {
            "testProject": "calculator",
            "testClass": "org.advanced.AdvancedCalculatorTest",
            "testMethod": "testCosNegative"
        },
        {
            "testProject": "statistics",
            "testClass": "org.advanced.AdvancedStatisticsTest",
            "testMethod": "testCorrelationSimple"
        }
    ],
    "logToConsole": false,  
    "resultsPath": "./results", 
    "method": "Bisect"  
   }
```

#### Key Descriptions

- **repositoryLink**: Set this only if you need to clone the repository first. Provide the repository's SSH or HTTPS link.
- **repositoryPath**: Specify the path where the repository is or will be cloned. will be created if not exists.
- **branches**: List the branches you want to consider. Use an array to include multiple branches.
- **days**: Indicate the number of days for which commits should be considered.
- **firstCommit**: Specify the commit hash from which to start considering subsequent commits.
- **testInputFile**: Path to the CSV file containing tests. If this is null, the tests defined in the 'tests' array will be used.
- **testSrcPath**: Path to the test source folder relative to the subproject or project root.
- **tests**: Provide details of tests if 'testInputFile' is null. Each test should have the project, class, and method specified.
- **logToConsole**: Set to `true` to print logs to the terminal, or `false` to send them to a file.
- **resultsPath**: Specify where to store the results, folder will be cleaned before execution and created if not exists.
- **method**: Choose the method to use; options include 'Bisect', 'Linear', or 'Batch XX'.


### 4. Running the Tool:  
   Execute the tool using the following command:
   ```sh
   ./gradlew :regression-finder:run
   ```

### 5. Output:  
   The results will be generated and stored in a CSV file located at `./results/`.
