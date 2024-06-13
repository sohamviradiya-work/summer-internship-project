package com.tool;

import java.io.IOException;
import java.util.List;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.items.BenchMark;
import com.tool.runners.RegressionTool;
import com.tool.writers.CSVWriter;

public class BenchMarker {

    public static void benchmark(String repositoryPath, String resultsPath, String gradleVersion)
            throws NoHeadException, IOException, GitAPIException {

        CSVWriter<BenchMark> benchMarkWriter = CSVWriter.create(resultsPath + "/bench-mark.csv");
        
        List<String> methods = List.of("Linear", "Batch 5", "Batch 20", "Batch 50", "Bisect");
        for (String method : methods) {
            long time = RegressionTool.run(repositoryPath,"/src/test/java" ,gradleVersion, method, resultsPath,false);
            benchMarkWriter.write(new BenchMark(time, method));
        }
        benchMarkWriter.close();
    }
}
