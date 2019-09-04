package io.dallen.integration;

import static org.junit.Assert.assertEquals;

public class BasicExpressionTest {

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
                2, "while\nwhile\nloop\nloop\nloop\nloop\nloop\n", ""
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
                0, "Oh no!\nError message\nWe resume here!\n", ""
        );
        assertEquals(expected, actual);
    }
}
