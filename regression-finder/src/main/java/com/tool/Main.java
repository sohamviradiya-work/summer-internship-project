package com.tool;

import com.tool.runners.RegressionTool;

public class Main {
    public static void main(String[] args) throws Exception {
        try {
            Config config = Config.mountConfig();

            long time = RegressionTool.runWithTests(config,"7.6.4");

            System.out.println("time: " + time + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
