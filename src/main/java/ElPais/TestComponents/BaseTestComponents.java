package ElPais.TestComponents;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;

import java.net.MalformedURLException;
import java.time.Duration;

public class BaseTestComponents {

    public WebDriver driver;
    public Boolean isMobile;
    public WebDriverWait wait;

    @BeforeMethod
    public void setUp() throws MalformedURLException {
        //driver instantiation

        driver  = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().window().maximize();

        if (driver instanceof RemoteWebDriver) {
            Capabilities caps = ((RemoteWebDriver)driver).getCapabilities();

            isMobile = caps.getCapability("deviceName") != null;
        } else {
            isMobile = false;
        }
    }

    @AfterTest
    public void TearDown()
    {
        driver.close();
    }
}
