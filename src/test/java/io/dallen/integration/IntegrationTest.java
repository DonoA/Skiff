package io.dallen.integration;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {

    @org.junit.Test
    public void helloWorld() {
        IntegrationTestHarness harness = new IntegrationTestHarness("HelloWorld");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                0, "Hello World!\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void loops() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Loops");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                2, "while\nwhile\nloop\nloop\nloop\nloop\nloop\nbonk\nbonk\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void classes() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Classes");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                16, "Dave\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void generics() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Generics");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                5, "My name Dave!\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void tryCatch() {
        IntegrationTestHarness harness = new IntegrationTestHarness("TryCatch");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                1, "Oh no!\nError message\nWe resume here!\nTop Level Error Caught! Message: Invalid access to storage!\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void optional() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Optional");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                5, "", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void imports() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Import");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                5, "", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void deconstruction() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Deconstruction");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                15, "Name:James\nJames\n10th\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void flowControl() {
        IntegrationTestHarness harness = new IntegrationTestHarness("FlowControl");

            IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                0, "i is gt 5!\ni is lt 5!\ni is 10!\ni is 15 or 14\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void functions() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Functions");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                2, "Hello World!\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void math() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Math");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                15, "", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void objectPrimitives() {
        IntegrationTestHarness harness = new IntegrationTestHarness("ObjectPrimitives");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                0, "10\n15\n10.650000\n42.419998\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void subscript() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Subscript");

        IntegrationTestHarness.TestResult actual = harness.run();
        String absoluteBinary = new File(harness.getBinaryPath()).getAbsolutePath();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                1, absoluteBinary + "\nNew String\nPlacing 15 at index 5\n" +
                "Getting item at 1\n", ""
        );
        assertEquals(expected, actual);
    }

    @org.junit.Test
    public void files() {
        IntegrationTestHarness harness = new IntegrationTestHarness("Files");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                0, "This is my test file\nit has some lines\nya\n", ""
        );
        assertEquals(expected, actual);
    }
}
