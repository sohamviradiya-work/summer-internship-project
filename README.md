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
   ./gradlew :regression-finder:run
   ```

4. **Output**:  
   The results will be generated and stored in a CSV file located at `./resources/results/`.
