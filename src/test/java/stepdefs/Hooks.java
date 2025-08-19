package stepdefs;

import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import io.cucumber.java.*;
import utils.TokenManager;

public class Hooks {

    /**
     * Logs a message to the Extent report.
     * If the ExtentCucumberAdapter is not initialized, it will quietly ignore the error.
     *
     * @param message The message to log
     */
    private static void logToExtent(String message) {
        try {
            ExtentCucumberAdapter.addTestStepLog(message);
        } catch (Throwable ignored) {
            // Adapter may not be initialized at the very first hook; ignore quietly
        }
    }

    /**
     * This method is called once before all scenarios in the test suite.
     * It prewarms the TokenManager to ensure a valid token is available for all tests.
     */
    @BeforeAll
    public static void beforeAll() {
        TokenManager.prewarm();
    }

    /**
     * This method is called before any scenario starts.
     * It initializes the Extent report and logs the start of the test suite.
     */
    @Before
    public void beforeScenario(Scenario scenario) {
        logToExtent("🚀 Starting scenario: " + scenario.getName());
    }

    /**
     * This method is called after each scenario finishes.
     * It logs the status of the scenario to the Extent report.
     */
    @After
    public void afterScenario(Scenario scenario) {
        String status = scenario.isFailed() ? "❌ FAILED" : "✅ PASSED";
        logToExtent("🏁 Finished scenario: " + scenario.getName() + " - Status: " + status);
    }

    /**
     * This method is called after all scenarios in the test suite have finished.
     * It finalizes the Extent report.
     */
    @BeforeStep
    public void beforeStep() {
        logToExtent("➡️ Starting step...");
    }

    /**
     * This method is called after each step finishes.
     * It logs the completion of the step to the Extent report.
     */
    @AfterStep
    public void afterStep() {
        logToExtent("✔️ Step finished.");
    }
}
