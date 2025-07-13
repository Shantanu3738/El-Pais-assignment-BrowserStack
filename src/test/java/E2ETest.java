import ElPais.TestComponents.BaseTestComponents;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class E2ETest extends BaseTestComponents {

    @Test
    public void AnalyseNewsHeadings() throws InterruptedException {

        //    System.out.println(isMobile);
        driver.get("https://elpais.com/");

        //wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath("//button[contains(@id,'agree')]"))));
        Thread.sleep(5000);


//  1. Ensure that the website's text is displayed in Spanish.

        if(isMobile)
        {
            //driver.findElement(By.xpath("//*[text()='ACCEPT AND CONTINUE']")).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn_open_hamburger")));
            driver.findElement(By.id("btn_open_hamburger")).click();
            Assert.assertTrue(driver.findElement(By.xpath("(//android.view.View[@content-desc='ESPAÑA'])[1]"))
                    .isSelected());
        }
        else{
            //code to check Spanish language when using computer or laptop browser

            driver.findElement(By.xpath("//button[contains(@id,'agree')] | //*[contains(text(), 'Accept')]")).click();
            String labguageDisplay = driver.findElement(By.xpath("//a/span[text()='España']")).getText();
            Assert.assertEquals(labguageDisplay,"ESPAÑA");
        }

//  2. Scrape Articles from the Opinion Section

        driver.findElement(By.xpath("//a[text()='Opinión']")).click();

        //list of all article headers to choose the top 5
        List<WebElement> articleHeaders = driver.findElements(By.xpath("//h2[@class='c_t c_t-i ']"));

        WebElement articlePic;   //web element to store article header pic
        JavascriptExecutor js = (JavascriptExecutor) driver;
        List<String> articleNames = new ArrayList<String>();

        System.out.println("5 top article headers:");

        for(int i=0; i<Math.min(5, articleHeaders.size());i++)  //work loop for not more than 5 times
        {
            try {
                String articleHeader = articleHeaders.get(i).getText();  //Get individual article header from the list
                System.out.println(articleHeader);
                articleNames.add(articleHeader);  //storing top 5 article headers into a list

                //Taking screenshot of article image and copying it to code folder
                articlePic = driver.findElement(By.xpath("//img[@alt='"+articleHeader+"']"));
                js.executeScript("arguments[0].scrollIntoView(true);", articlePic);
                TakesScreenshot ts = (TakesScreenshot) articlePic;
                File source = ts.getScreenshotAs(OutputType.FILE);
                File destination = new File(System.getProperty("user.dir")+"//Screenshots//"+articleHeader+".jpg");
                FileHandler.copy(source, destination);
            }
            catch(Exception e)
            {
                //to catch NotFoundException if an image for article header is not found
            }
        }

//  3. Translate Article Header

        //using API to translate all Spanish article headers to English
        RestAssured.baseURI ="https://api-free.deepl.com";
        List<String> translatedHeaderList = new ArrayList<String>();

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Translated article header from Spain to English:");

        for(String originalText : articleNames) {
            String response = given()
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .formParam("auth_key", "2db6f586-10d8-4100-a136-f97d9d393bea:fx")
                    .formParam("text", originalText)
                    .formParam("source_lang", "ES")
                    .formParam("target_lang", "EN")
                    .when().get("/v2/translate")
                    .then().statusCode(200).extract().response().asString();

            JsonPath jsp = new JsonPath(response);
            String tranlatedString = jsp.getString("translations.text");
            System.out.println(tranlatedString.substring(1,tranlatedString.length()-1));
            translatedHeaderList.add(tranlatedString.substring(1,tranlatedString.length()-1));
        }

//  4. Count frequent occurances of words more than 2

        Map<String, Integer> countWords = new HashMap<String, Integer>();
        int count = 0;

        System.out.println("-----------------------------------------------------------------------------");
        System.out.println("Frequency of words more than 2 in the headers-");

        for(String translatedHeader : translatedHeaderList) {
            String[] words = translatedHeader.toLowerCase().replaceAll("[\\p{P}\\p{S}]", " ")
                    .split(" ");        //replacing all parameters and spaces to blanks and splitting using blanks


            for (String word : words) {       //counting all words and their frequencies
                countWords.put(word, countWords.getOrDefault(word, 0) + 1);
            }
        }


        //checking if word frequencies is more than 2, then print them
        for(Map.Entry<String, Integer> entry : countWords.entrySet())
        {
            String countWordKey = entry.getKey();
            int countWordFrequency = entry.getValue();
            if(countWordFrequency>=2)
            {
                System.out.println(countWordKey+": "+countWordFrequency);
                count++;
            }
        }
        if(count == 0)      // if no words are found to appear more than twice, display this message
        {
            System.out.println("No words are repeated more than twice");
        }

        System.out.println("-----------------------------------------------------------------------------");
    }


}
