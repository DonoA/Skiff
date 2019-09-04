package io.dallen.integration;

import io.dallen.SkiffC;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

class IntegrationTestHarness {
    private static final String workingDir = "working";

    private final String testName;

    IntegrationTestHarness(String testName) {
        this.testName = testName;
    }

    TestResult run() {
        try {
            Files.createDirectories(new File("working/" + testName).toPath());
            boolean passed = SkiffC.compile("src/test/resources/" + testName + "/" + testName + ".skiff",
                    "working/" + testName + "/" + testName + ".c", false);
            if(!passed) {
                throw new RuntimeException("SkiffC failed!");
            }

            TestResult gccResult = exec("gcc -Wall -Wno-pointer-to-int-cast " +
                    "-o working/" + testName + "/" + testName +
                    " working/" + testName + "/" + testName + ".c");
            if(gccResult.returnCode != 0 || !gccResult.stdOut.isEmpty() || !gccResult.stdErr.isEmpty()) {
                throw new RuntimeException("Gcc failed " + gccResult.toString());
            }

            return exec("./working/" + testName + "/" + testName);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private TestResult exec(String command) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(command);

        p.waitFor(600, TimeUnit.SECONDS);

        BufferedReader stdOutput = new BufferedReader(new InputStreamReader(p.getInputStream()));
        BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

        StringBuilder stdout = new StringBuilder();
        String s;
        while ((s = stdOutput.readLine()) != null) {
            stdout.append(s).append("\n");
        }

        StringBuilder stderr = new StringBuilder();
        while ((s = stdError.readLine()) != null) {
            stderr.append(s).append("\n");
        }
        return new TestResult(p.exitValue(), stdout.toString(), stderr.toString());
    }

    public static class TestResult {
        private final int returnCode;
        private final String stdOut;
        private final String stdErr;

        TestResult(int returnCode, String stdOut, String stdErr) {
            this.returnCode = returnCode;
            this.stdOut = stdOut;
            this.stdErr = stdErr;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TestResult)) return false;
            TestResult that = (TestResult) o;
            return returnCode == that.returnCode &&
                    Objects.equals(stdOut, that.stdOut) &&
                    Objects.equals(stdErr, that.stdErr);
        }

        @Override
        public int hashCode() {
            return Objects.hash(returnCode, stdOut, stdErr);
        }

        @Override
        public String toString() {
            return "TestResult{" +
                    "returnCode=" + returnCode +
                    ", stdOut='" + stdOut + '\'' +
                    ", stdErr='" + stdErr + '\'' +
                    '}';
        }
    }
}
