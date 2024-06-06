# Project Documentation: Regression Finder Tool


## Project Description
The Regression Finder Tool is designed to identify the specific commit that caused a regression in a codebase. It takes a list of failed tests from a suite and maps each failure to the corresponding commit and author responsible. The tool efficiently handles large repositories and exports the results to an Excel file.

## Features
- **Input Handling**: Accepts a list of failed tests in a suite.
- **Data Export**: Exports details of unit tests, commits, and authors to an Excel file.
- **Performance Optimization**: Optimized to work efficiently with large repositories.
- **Result Storage**: Stores the results in a CSV file located at `./results/blame-tests.csv`.
- **Future Enhancements**:
  - Integration with Microsoft Teams for alert notifications.
  - Integration with Jira API for issue tracking and management.

## Requirements
- **OpenJDK**: Ensure `openjdk@17` is installed.
- **Environment Variable**: Set `JAVA_HOME` to the OpenJDK installation path.
- **Gradle**: Ensure Gradle is installed and properly configured.

## Installation and Setup
1. **Cloning the Repository**:  
   To start using the Regression Finder Tool, clone the repository from GitHub:
   ```sh
   git clone https://github.com/sohamviradiya-work/summer-internship-project
   ```

2. **Setting Up the Environment**:  
   Navigate to the project directory and set up the Gradle wrapper:
   ```sh
   cd summer-internship-project
   ```
   ```sh
   gradle wrapper
   ```

3. **Running the Tool**:  
   Execute the tool using the following command:
   ```sh
   ./gradlew run
   ```

4. **Output**:  
   The results will be generated and stored in a CSV file located at `./results/blame-tests.csv`.

## Algorithm

1. **Run All Tests for HEAD Commit:**
    - Execute the full test suite on the latest commit (`HEAD`) in the repository.

2. **Store Failed Tests:**
    - Identify and store any failed tests in a list called `failedTests`.
    - Initialize an empty list called `blameList` to keep track of the commits responsible for test failures.
    - Initialize a variable `previousCommit` and set it to `HEAD`.

3. **Check for Test Failures:**
    - If `failedTests` is empty, the process ends here as there are no test failures to analyze further.

4. **Revert to Previous Commit:**
    - Revert the repository to the previous commit.
    - Initialize a new empty list called `newFailedTests`.

5. **Run Specific Tests:**
    - Run only the tests that are listed in `failedTests` on the reverted commit.

6. **Store New Failed Tests:**
    - Identify and store any failed tests in `newFailedTests`.

7. **Analyze Test Results:**
    - Compare `failedTests` and `newFailedTests`:
        - If a test is present in `failedTests` but not in `newFailedTests`, it indicates that this test passed in the previous commit and only failed after the reverted commit.
        - Write this test to `blameList` along with `previousCommit`

8. **Update For Next Iteration:**
    - Set `failedTests` to `newFailedTests`.
    - Set `previousCommit` to the current commit.

9. **Repeat the Process:**
    - Go back to Step 3.
