package io.dallen.integration;

import static org.junit.Assert.assertEquals;

public class BasicExpression {

    @org.junit.Test
    public void variable() {
        IntegrationTestHarness harness = new IntegrationTestHarness("HelloWorld");

        IntegrationTestHarness.TestResult actual = harness.run();
        IntegrationTestHarness.TestResult expected = new IntegrationTestHarness.TestResult(
                0, "Hello World!\n", ""
        );
        assertEquals(expected, actual);
    }
}
