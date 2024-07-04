# Project Documentation: Regression Finder Tool

## Project Description

The Regression Finder Tool is designed to identify the specific commit that caused a regression in a codebase. It takes a list of failed tests from a suite and maps each failure to the corresponding commit and author responsible.

## Features

- **Input Handling**: Accepts a list of failed tests in a suite.
- **Data Export**: Exports details of unit tests, commits, and authors.
- **Performance Optimization**: Optimized to work efficiently with large repositories.

## Requirements

- **OpenJDK**: Ensure a JDK is installed.
- **Environment Variable**: Set `JAVA_HOME` to the JDK installation path.
- **Gradle**: Ensure Gradle is installed and properly configured.

## Installation and Setup

### 1. Cloning the Repository:

To start using the Regression Finder Tool, clone the repository from GitHub:

```sh
git clone https://github.com/sohamviradiya-work/summer-internship-project
```

<hr>

### 2. Setting Up the Environment:

Navigate to the project directory and set up the Gradle wrapper:

```sh
cd summer-internship-project
```

```sh
gradle wrapper
```

<hr>

### 3. Instructions for Writing the config.json:

To create the JSON configuration file for the project, follow the structure and instructions below. Each key-value pair is explained for clarity:

```json
{
    "repositoryLink" : null,
    "repositoryPath" : "../resources/large-repo",
    "branches" : ["master"],
    "days" : 20,
    "firstCommit" : "0d12dba93927a2e3b480a981ceb9d6c7415eda09",
    "testInputFile" : "./tests.csv",
    "tests": [
        {
            "testProject": "calculator",
            "testClass" : "org.advanced.AdvancedCalculatorTest",
            "testMethod" : "testCosNegative"
        },
        {
            "testProject": "statistics",
            "testClass" : "org.advanced.AdvancedStatisticsTest",
            "testMethod" : "testCorrelationSimple"
        }
    ],
    "testSrcPath" : "/src/test/java/",
    "logToConsole": true,
    "jiraTickets" : true,
    "teamsNotifications" : true,
    "reportLastPhase": true,
    "resultsPath" : "./results",
    "method" : "Bisect"
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
- **logToConsole**: Set to `true` to print logs to the terminal, or `false` to send them to a file at `./results/.log`.
- **resultsPath**: Specify where to store the results, folder will be cleaned before execution and created if not exists.
- **jiraTickets**: Boolean flag to create JIRA tickets.
- **reportLastPhase**: Boolean flag to report tests which are failing since the first commit.
- **teamsNotifications**: Boolean flag to send notifications to Microsoft Teams.
- **method**: Choose the method to use; options include 'Bisect', 'Linear', or 'Batch XX'.

<hr>

### 4. Enviroment Variables:

1. **JIRA Configuration:**
   - **JIRA_MAIL:** The email address associated with your JIRA account. This email will be used to report issues.
   - **JIRA_TOKEN:** The API token generated from your JIRA account for authentication.
   - **JIRA_SERVER:** The base URL of your JIRA server, where your JIRA instance is hosted (e.g., `https://your-domain.atlassian.net`).
   - **JIRA_PROJECT_KEY:** The key of the JIRA project you're working with (e.g., `PROJ`).
   - **JIRA_ISSUE_TYPE:** The ID of the issue type in JIRA that you want to create (e.g., `Bug` or `Task`).
   - **JIRA_ISSUE_TRANSITION:** The ID of the transition action you want to perform on a JIRA issue (e.g., `31` for "Start Progress").

2. **Teams Bot Configuration:**
   - **TEAMS_BOT_API_URL:** The endpoint URL of your Microsoft Teams bot. This is where your bot's API is hosted (e.g., `https://your-bot.azurewebsites.net/`).

- **Create a `.env` file:** This file should be placed in the root directory of the project.
- **Format:** Use the format `KEY=VALUE` for each environment variable.

### Example `.env` File:

```plaintext
JIRA_MAIL=your-email@example.com
JIRA_TOKEN=your-jira-api-token
JIRA_SERVER=https://your-domain.atlassian.net
JIRA_PROJECT_KEY=PROJ
JIRA_ISSUE_TYPE=Bug
JIRA_ISSUE_TRANSITION=31
TEAMS_BOT_API_URL=https://your-bot.azurewebsites.net/
```

### 5. Running the Tool:

Execute the tool using the following command:

```sh
./gradlew :regression-finder:run
```

### 6. Output:

The results will be generated and stored in a CSV file located at `./results/blame.csv`.

## Architecture

![architecture image](https://github.com/sohamviradiya-work/summer-internship-project/blob/main/documentation/architecture.png?raw=true)

<hr>

### **Components and Responsibilities:**

#### **1. GitWorker:**
- **Role:** Manages Git-related operations.
- **Responsibilities:**
  - Checking out specific commits.
  - Checking if a file has changed between commits.

#### **2. GradleWorker:**
- **Role:** Manages Gradle-related operations.
- **Responsibilities:**
  - Syncing project dependencies.
  - Running tests.
  - Building the project.

#### **3. ProjectInstance:**
- **Role:** Acts as a coordinator between GitWorker and GradleWorker.
- **Responsibilities:**
  - Commands GradleWorker to sync dependencies if `build.gradle` has changed.
  - Distributes commands from RegressionFinder to GitWorker and GradleWorker.

#### **4. RegressionFinder:**
- **Role:** Implements the core algorithm for finding regressions.
- **Subtypes:**
  - **BisectRegressionFinder**
  - **BatchRegressionFinder**
  - **LinearRegressionFinder**
- **Responsibilities:**
  - Provides commands to run specific tests for specific commits.
  - Processes test results.
  - If a regression is found, passes findings to BlameWriter with test and commit information.
  - Repeats the process for another commit with a reduced test set according to the algorithm.

#### **5. BlameWriter:**
- **Role:** Handles the output of regression findings.
- **Responsibilities:**
  - Passes results to configured output streams like Jira Ticket Writer, CSV File Writer, and MS Teams Client.

#### **6. CSV Writer:**
- **Role:** Writes results to a CSV file.
- **Responsibilities:**
  - Converts results to CSV row format.
  - Writes results to a file.

#### **7. Jira Client:**
- **Role:** Manages Jira-related operations.
- **Responsibilities:**
  - Converts results to Jira tickets.
  - Assigns the ticket to the commit author using Jira APIs.

#### **8. MS Teams Client:**
- **Role:** Manages Microsoft Teams notifications.
- **Responsibilities:**
  - Converts results to MS Teams messages.
  - Sends messages to the commit author using Teams APIs.

#### **9. Main Invoker:**
- **Role:** Initializes and orchestrates the regression finding process.
- **Responsibilities:**
  - Fetching tests from the configuration.   
  - Fetching commits that satisfy given criteria.
  - Providing tests and commits to RegressionFinder.

<hr>

### **Regression Finding Algorithms:**

#### **1. Linear Regression Finding**
- **Description:**
  - Tests each commit one by one in a linear sequence to find the commit that introduced the regression.
- **Process:**
  1. Check out and test each commit sequentially from the newest to the oldest.
  2. Stop when the first commit that passes the test is found.
  3. The next commit after the passing one is identified as the one causing the regression.
- **Advantages:**
  - Guarantees finding the exact commit.
- **Disadvantages:**
  - Can be very time-consuming for a large number of commits, as every commit must be tested.

#### **2. Batch Regression Finding**
- **Description:**
  - Tests commits in groups (batches) to narrow down a smaller set that may contain the regression. Once a problematic batch is identified, individual commits within that batch are tested.
- **Process:**
  1. Divide the range of commits into smaller batches.
  2. Check out and test the first commit of each batch, starting from the newest to the oldest.
  3. Identify the batch containing the regression (where the first commit starts passing).
  4. Within the identified batch, run the linear algorithm to find the exact commit.
- **Time Complexity:**
  - **O(T(B + N/B))**, where **B** is the batch size, and **N** is the number of commits. Best results are obtained for **B ~ √N** i.e. **O(√N)**.
- **Advantages:**
  - Reduces the number of initial tests by testing in batches.
- **Disadvantages:**
  - Can be slow if the regression-causing commit is far in the past.
  - May miss the regression if some failing commits temporarily pass the tests.

#### **3. Bisect Regression Finding**
- **Description:**
  - Uses a binary search approach to identify the regression commit, halving the search space each time based on the test results.
- **Process:**
  1. Identify the range of commits to be tested (from a known good commit to a known bad commit).
  2. Check out and test the midpoint commit.
  3. If the midpoint commit passes, discard the past half (good commits) and continue searching in the future half.
  4. If the midpoint commit fails, discard the future half (bad commits) and continue searching in the past half.
  5. Repeat until the exact commit causing the regression is found.
- **Time Complexity:**
  - **O(T log(N))**, where **T** is the number of tests and **N** is the number of commits.
- **Advantages:**
  - Efficiently narrows down the range of commits.
- **Disadvantages:**
  - Can produce large commit jumps, leading to longer build and dependency syncing times.
  - May miss the regression if some failing commits temporarily pass the tests.

<hr>

### **Choosing the Right Algorithm**
- **Bisect Regression Finding:** Best for large ranges of commits when quick narrowing down is needed.
- **Linear Regression Finding:** Suitable for smaller ranges of commits or when accuracy is more important than efficiency.
- **Batch Regression Finding:** Better speed than Linear Regression Finding, with smaller commit jumps and higher accuracy than Bisect Regression Finding.
