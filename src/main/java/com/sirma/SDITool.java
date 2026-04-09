package com.sirma;

import dev.langchain4j.agent.tool.Tool;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SDITool {

    private static WebDriver driver;

    @Tool
    public String openAndFillInsuranceForm() {
        try {
            if (driver == null) {
                ChromeOptions options = new ChromeOptions();
                options.setBinary("C:\\Program Files\\BraveSoftware\\Brave-Browser\\Application\\brave.exe");
                WebDriverManager.chromedriver().setup();
                driver = new ChromeDriver(options);
                driver.manage().window().maximize();
            }

            driver.get("https://www.sdi.bg/onlineinsurance/showQuestionnaire.php");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(25));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            // --- COOKIE REMOVAL ---
            try {
                Thread.sleep(2000);
                js.executeScript("var e = document.getElementById('thinkconsent-notice-content-wrapper'); if(e) e.remove();");
                js.executeScript("var b = document.querySelector('.thinkconsent-backdrop'); if(b) b.remove();");
            } catch (Exception e) {}

            // --- STEP 1 ---
            System.out.println("Filling Step 1...");
            WebElement typeEl = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("typeSelect")));
            new Select(typeEl).selectByValue("1");
            js.executeScript("arguments[0].dispatchEvent(new Event('change', {bubbles:true}));", typeEl);

            wait.until(d -> new Select(d.findElement(By.id("dvigatelSelect"))).getOptions().size() > 1);
            new Select(driver.findElement(By.id("dvigatelSelect"))).selectByValue("1600");
            Thread.sleep(600);
            new Select(driver.findElement(By.id("dvigatelType"))).selectByValue("1");
            new Select(driver.findElement(By.id("ksiliSelect"))).selectByValue("74");
            new Select(driver.findElement(By.id("firstRegistrationYear"))).selectByValue("2001");
            new Select(driver.findElement(By.id("reg_no"))).selectByValue("KH");

            js.executeScript("arguments[0].click();", driver.findElement(By.id("continue")));

            // --- STEP 2 ---
            System.out.println("Filling Step 2...");
            WebElement expEl = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("driverExperience")));
            Thread.sleep(1200);
            new Select(expEl).selectByValue("10");
            new Select(driver.findElement(By.id("where_go"))).selectByValue("druga");
            new Select(driver.findElement(By.id("has_kasko"))).selectByValue("no");

            // --- CALCULATE ---
            Thread.sleep(1000);
            try {
                WebElement calcBtn = driver.findElement(By.xpath("//button[contains(., 'ИЗЧИСЛИ')]"));
                js.executeScript("arguments[0].click();", calcBtn);
            } catch (Exception e) {
                js.executeScript("document.forms[0].submit();");
            }

            // --- WAIT FOR RESULTS ---
            System.out.println("Waiting for offers (20 sec)...");
            Thread.sleep(20000);

            List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
            boolean foundData = false;

            if (extractViaJS(js)) {
                foundData = true;
            } else {
                for (int i = 0; i < iframes.size(); i++) {
                    try {
                        driver.switchTo().frame(i);
                        if (extractViaJS(js)) {
                            foundData = true;
                            driver.switchTo().defaultContent();
                            break;
                        }
                    } catch (Exception e) {}
                    driver.switchTo().defaultContent();
                }
            }

            if (foundData) {
                return "Success! Data extracted.";
            } else {
                return "Error: No data found.";
            }

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private boolean extractViaJS(JavascriptExecutor js) {
        try {
            String script =
                    "var results = [];" +
                    "var rows = document.querySelectorAll('.oi-compare-row');" +
                    "for(var row of rows){" +
                    "  var nameEl = row.querySelector('.oi-compare-company span');" +
                    "  var name = nameEl ? nameEl.innerText.trim() : '';" +
                    "  var text = row.innerText || '';" +
                    "  if(name && text.includes('лв')){" +
                    "    results.push(name + '||' + text.replace(/\\n/g, ' '));" +
                    "  }" +
                    "}" +
                    "return results;";

            List<String> rawData = (List<String>) js.executeScript(script);
            if (rawData == null || rawData.isEmpty()) return false;

            try (PrintWriter writer = new PrintWriter(new FileWriter("GO_Results.csv"))) {
                writer.write('\ufeff');
                writer.println("Offer Data");
                int savedCount = 0;
                for (String line : rawData) {
                    String clean = line.replace(";", ",").replaceAll("\\s{2,}", " ").trim();
                    if (clean.contains("лв") || clean.contains("€")) {
                        writer.println(clean);
                        savedCount++;
                    }
                }
                return savedCount > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private void saveFile(String fileName, List<String> data) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.write('\ufeff');
            writer.println("Offer Data");
            for (String line : data) {
                String clean = line.replace(";", ",").replace("\n", " ").trim();
                if (clean.matches(".*\\d+.*") && clean.contains("лв")) {
                    writer.println(clean);
                }
            }
        }
    }
}