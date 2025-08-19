package runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "stepdefs")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value =
        "pretty," +
                "html:target/cucumber-html-report.html," +
                "json:target/cucumber.json," +
                // Force ExtentCucumberAdapter to load first
                "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:")
@ConfigurationParameter(key = PLUGIN_PUBLISH_QUIET_PROPERTY_NAME, value = "true")
public class TestRunner {

    static {
        // Ensures extent.properties is loaded from classpath
        String extentConfig = Thread.currentThread()
                .getContextClassLoader()
                .getResource("extent.properties") != null
                ? "Found extent.properties in classpath"
                : "extent.properties NOT found in classpath - Extent will NOT initialize!";
        System.out.println("[Extent Init Check] " + extentConfig);
    }
}
